/*
 * Copyright 2016 srhebbar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.utils.TimeUtils;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang.StringUtils;

/**
 * @author srhebbar
 */
public final class Office365ConnectorWebhookNotifier {

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting().create();

    private final FactsBuilder factsBuilder;
    private final DecisionMaker decisionMaker;

    private final Run run;
    private final Job job;
    private final TaskListener listener;

    private final ActionableBuilder potentialActionBuilder;

    public Office365ConnectorWebhookNotifier(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;
        this.factsBuilder = new FactsBuilder(run);
        this.decisionMaker = new DecisionMaker(run, listener);
        this.job = this.run.getParent();
        potentialActionBuilder = new ActionableBuilder(run, factsBuilder);
    }

    public void sendBuildStartedNotification(boolean isFromPreBuild) {
        List<Webhook> webhooks = extractWebhooks(job);
        if (webhooks.isEmpty()) {
            return;
        }

        Card card = null;

        boolean isBuild = run instanceof AbstractBuild<?, ?>;
        if ((isBuild && isFromPreBuild) || (!isBuild && !isFromPreBuild)) {
            card = createJobStartedCard();
        }

        for (Webhook webhook : webhooks) {
            if (decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                if (webhook.isStartNotification()) {
                    executeWorker(webhook, card);
                }
            }
        }
    }

    public void sendBuildCompleteNotification() {
        List<Webhook> webhooks = extractWebhooks(job);
        if (webhooks.isEmpty()) {
            return;
        }

        Card card = createJobCompletedCard();

        for (Webhook webhook : webhooks) {
            if (decisionMaker.isStatusMatched(webhook) && decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                executeWorker(webhook, card);
            }
        }
    }

    private static List<Webhook> extractWebhooks(Job job) {
        WebhookJobProperty property = (WebhookJobProperty) job.getProperty(WebhookJobProperty.class);
        if (property != null && property.getWebhooks() != null) {
            return property.getWebhooks();
        }
        return Collections.emptyList();
    }

    public void sendBuildNotification(StepParameters stepParameters) {
        Card card;
        if (StringUtils.isNotBlank(stepParameters.getMessage())) {
            card = createBuildMessageCard(stepParameters);
        } else if (StringUtils.equalsIgnoreCase(stepParameters.getStatus(), "started")) {
            card = createJobStartedCard();
        } else {
            card = createJobCompletedCard();
        }

        WebhookJobProperty property = (WebhookJobProperty) job.getProperty(WebhookJobProperty.class);
        if (property == null) {
            Webhook webhook = new Webhook(stepParameters.getWebhookUrl());
            executeWorker(webhook, card);
            return;
        }

        for (Webhook webhook : property.getWebhooks()) {
            executeWorker(webhook, card);
        }
    }

    private Card createJobStartedCard() {
        factsBuilder.addStatusStarted();
        factsBuilder.addStartTime();
        factsBuilder.addRemarks();
        addScmDetails();

        String jobName = getDisplayName();
        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build " + getRunName();
        Section section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());

        String summary = jobName + ": Build " + getRunName() + " Started";
        Card card = new Card(summary, section);
        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    private Card createJobCompletedCard() {
        String jobName = getDisplayName();
        String summary = jobName + ": Build " + getRunName();

        Fact statusFact = FactsBuilder.buildStatus();
        factsBuilder.addFact(statusFact);
        factsBuilder.addStartTime();

        // Result is only set to a worse status in pipeline
        Result result = run.getResult() == null ? Result.SUCCESS : run.getResult();
        if (result != null) {

            factsBuilder.addCompletionTime();
            factsBuilder.addTests();

            String status = null;
            Run previousBuild = run.getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            Run rt = run.getPreviousNotFailedBuild();
            Run failingSinceRun;
            if (rt != null) {
                failingSinceRun = rt.getNextBuild();
            } else {
                failingSinceRun = job.getFirstBuild();
            }

            if (result == Result.SUCCESS) {
                // back to normal
                if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                    status = "Back to Normal";
                    summary += " Back to Normal";

                    if (failingSinceRun != null) {
                        long currentBuildCompletionTime = TimeUtils.countCompletionTime(run.getStartTimeInMillis(), run.getDuration());
                        factsBuilder.addBackToNormalTime(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis());
                    }
                }
                // still success
                else {
                    status = "Build Success";
                    summary += " Success";
                }
            } else if (result == Result.FAILURE) {
                if (failingSinceRun != null && previousResult == Result.FAILURE) {
                    status = "Repeated Failure";
                    summary += " Repeated Failure";

                    factsBuilder.addFailingSinceBuild(failingSinceRun.number);
                    factsBuilder.addFailingSinceTime(failingSinceRun.getStartTimeInMillis() + failingSinceRun.getDuration());
                } else {
                    status = "Build Failed";
                    summary += " Failed";
                }
            } else if (result == Result.ABORTED) {
                status = "Build Aborted";
                summary += " Aborted";
            } else if (result == Result.UNSTABLE) {
                status = "Build Unstable";
                summary += " Unstable";
            } else if (result == Result.NOT_BUILT) {
                status = "Not Built";
                summary += " Not Built";
            } else {
                // if we are here it means that something went wrong in logic above
                // and we are facing unsupported status or case
                log("Unsupported result: " + result);
                status = result.toString();
                summary += " " + status;
            }

            statusFact.setValue(status);
        } else {
            statusFact.setValue(" Completed");
            summary += " Completed";
        }

        factsBuilder.addRemarks();
        addScmDetails();

        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build " + getRunName();
        Section section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());

        Card card = new Card(summary, section);
        if (result == Result.SUCCESS) {
            card.setThemeColor("96CEB4");
        } else if (result == Result.FAILURE) {
            card.setThemeColor("FF6F69");
        } else if (result == Result.ABORTED) {
            card.setThemeColor("7F7F7F");
        } else {
            card.setThemeColor("FFCC5C");
        }
        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    private Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = getDisplayName();
        if (stepParameters.getStatus() != null) {
            Fact fact = FactsBuilder.buildStatus();
            fact.setValue(stepParameters.getStatus());
            factsBuilder.addFact(fact);
        } else {
            factsBuilder.addStatusRunning();
        }

        String activityTitle = "Message from " + jobName + ", Build " + getRunName() + "";
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsBuilder.collect());

        String summary = jobName + ": Build " + getRunName() + " Status";
        Card card = new Card(summary, section);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    private void executeWorker(Webhook webhook, Card card) {
        try {
            HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhook.getUrl()), gson.toJson(card),
                    webhook.getTimeout(), listener.getLogger());
            worker.submit();
        } catch (IOException | InterruptedException | RejectedExecutionException e) {
            log(String.format("Failed to notify webhook: %s", webhook.getName()));
            e.printStackTrace(listener.getLogger());
        }
    }

    private void addScmDetails() {
        Set<User> users;
        List<ChangeLogSet<ChangeLogSet.Entry>> sets;

        try {
            users = (Set<User>) run.getClass().getMethod("getCulprits").invoke(run);
            sets = (List<ChangeLogSet<ChangeLogSet.Entry>>) run.getClass().getMethod("getChangeSets").invoke(run);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            users = Collections.emptySet();
            sets = Collections.emptyList();
        }

        factsBuilder.addCulprits(users);

        if (!sets.isEmpty()) {
            Set<User> authors = new HashSet<>();
            int filesCounter = 0;
            if (Iterables.all(sets, Predicates.instanceOf(ChangeLogSet.class))) {
                for (ChangeLogSet<ChangeLogSet.Entry> set : sets) {
                    for (ChangeLogSet.Entry entry : set) {
                        authors.add(entry.getAuthor());
                        filesCounter += getAffectedFiles(entry).size();
                    }
                }
            }
            factsBuilder.addDevelopers(authors);
            factsBuilder.addNumberOfFilesChanged(filesCounter);
        }
    }

    private Collection<? extends ChangeLogSet.AffectedFile> getAffectedFiles(ChangeLogSet.Entry entry) {
        try {
            return entry.getAffectedFiles();
        } catch (UnsupportedOperationException e) {
            log(e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getDisplayName() {
        return job.getFullDisplayName();
    }

    private String getRunName() {
        return run.hasCustomDisplayName() ? run.getDisplayName() : "#" + run.getNumber();
    }
  
    /**
     * Helper method for logging.
     */
    private void log(String message) {
        listener.getLogger().println("[Office365connector] " + message);
    }
}
