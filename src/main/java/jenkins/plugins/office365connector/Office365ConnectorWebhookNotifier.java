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
import java.util.ArrayList;
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
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import java.util.Iterator;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang.StringUtils;
import jenkins.plugins.office365connector.utils.Parser;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

/**
 * @author srhebbar
 */
public final class Office365ConnectorWebhookNotifier {

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    private final FactsBuilder factsBuilder;
    private final DecisionMaker decisionMaker;
    private final Run run;
    private final TaskListener listener;
    private final ActionableBuilder potentialActionBuilder;    
        
    Set<ChangeLogSet.AffectedFile> affectedFiles;    
    private String changesSingleString;

    public Office365ConnectorWebhookNotifier(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;
        this.factsBuilder = new FactsBuilder(run);
        this.decisionMaker = new DecisionMaker(run, listener);
        potentialActionBuilder = new ActionableBuilder(run, factsBuilder);
    }

    public void sendBuildStartedNotification(boolean isFromPreBuild) {
        Card card = null;

        boolean isBuild = run instanceof AbstractBuild<?, ?>;
        if ((isBuild && isFromPreBuild) || (!isBuild && !isFromPreBuild)) {
            card = createJobStartedCard();
        }

        if (card == null) {
            listener.getLogger().println(String.format("Build started card not generated."));
            return;
        }

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            //     listener.getLogger().println(String.format("No webhooks to notify"));
            return;
        }


        for (Webhook webhook : property.getWebhooks()) {
            if (decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                if (webhook.isStartNotification()) {
                    executeWorker(webhook, card);
                }
            }
        }
    }

    private boolean isCompactNotification() {

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            return false;
        }
        List<Webhook> webhooks = property.getWebhooks();
        for (Webhook webhook : webhooks) {
            return webhook.isCompactNotification();
        }
        return false;         
    }


    public void sendBuildCompleteNotification() {
        Card card = createJobCompletedCard();

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            //           listener.getLogger().println(String.format("No webhooks to notify"));
            return;
        }

        for (Webhook webhook : property.getWebhooks()) {
            if (decisionMaker.isStatusMatched(webhook) && decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                executeWorker(webhook, card);
            }
        }
    }

    public void sendBuildMessage(StepParameters stepParameters) {
        Card card;
        if (StringUtils.isNotBlank(stepParameters.getMessage())) {
            card = createBuildMessageCard(stepParameters);
        } else if (StringUtils.equalsIgnoreCase(stepParameters.getStatus(), "started")) {
            card = createJobStartedCard();
        } else {
            card = createJobCompletedCard();
        }

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
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
        String jobName = run.getParent().getDisplayName();
        String activityTitle = "";
        String activitySubtitle = "";
        Section section;

        if (isCompactNotification()) {
            Parser parser = new Parser();
            processScmDetails(false);
            int fileCount = 0;
            if (affectedFiles != null){
                fileCount = affectedFiles.size();
            }
            String uri = DisplayURLProvider.get().getRunURL(run);
            activityTitle = jobName + " - <a href=\"" + uri + "\">#" + run.getNumber() + "</a> Started";
            activitySubtitle = "by " + parser.getAuthor(run.getCauses()) + " (" + fileCount + " file(s) changed)";
        } else {
            factsBuilder.addStatusStarted();
            factsBuilder.addStartTime();
            factsBuilder.addRemarks();
            processScmDetails(true);
            activityTitle = "Update from " + jobName + ".";
            activitySubtitle = "Latest status of build #" + run.getNumber();
        }
        section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());
        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Started";
        Card card = new Card(summary, sectionList);
        card.setPotentialAction(potentialActionBuilder.buildActionable(isCompactNotification()));
        return card;
    }


    private Card createJobCompletedCard() {
        String jobName = run.getParent().getDisplayName();
        String summary = jobName + ": Build #" + run.getNumber();

        Fact statusFact = FactsBuilder.buildStatus();
        factsBuilder.addFact(statusFact);
        factsBuilder.addStartTime();

        String status = "";
        // Result is only set to a worse status in pipeline
        Result result = run.getResult() == null ? Result.SUCCESS : run.getResult();
        if (result != null) {
            factsBuilder.addCompletionTime();
            factsBuilder.addTests();
            Run previousBuild = run.getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            Run rt = run.getPreviousNotFailedBuild();
            Run failingSinceRun;
            if (rt != null) {
                failingSinceRun = rt.getNextBuild();
            } else {
                failingSinceRun = run.getParent().getFirstBuild();
            }

            if (result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)) {
                status = "Back to Normal";
                summary += " Back to Normal";

                if (failingSinceRun != null) {
                    factsBuilder.addBackToNormalTime(failingSinceRun.getStartTimeInMillis());
                }
            } else if (result == Result.FAILURE && failingSinceRun != null) {
                if (previousResult == Result.FAILURE) {
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
            } else if (result == Result.SUCCESS) {
                status = "Build Success";
                summary += " Success";
            } else if (result == Result.NOT_BUILT) {
                status = "Not Built";
                summary += " Not Built";
            } else {
                status = result.toString();
                summary += " " + status;
            }
            statusFact.setValue(status);
        } else {
            statusFact.setValue(" Completed");
            summary += " Completed";
        }

        factsBuilder.addRemarks();
        processScmDetails(true);

        String activityTitle = "";
        String activitySubtitle = "";
        Section section = null;

        if (isCompactNotification()) {
            String uri = DisplayURLProvider.get().getRunURL(run);
            activityTitle = jobName + " - <a href=\"" + uri + "\">#" + run.getNumber() + "</a> " + status + " after "+ factsBuilder.getBuildDuration();
            activitySubtitle = changesSingleString;
            factsBuilder.addTestsCompact();
            section = new Section(activityTitle, activitySubtitle, factsBuilder.collectCompact());            
        } else {
            activityTitle = "Update from " + jobName + ".";
            activitySubtitle = "Latest status of build #" + run.getNumber();
            section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());
        }
        
        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);
        Card card = new Card(summary, sectionList);
        if (result == Result.SUCCESS) {
            card.setThemeColor("96CEB4");
        } else if (result == Result.FAILURE) {
            card.setThemeColor("FF6F69");
        } else {
            card.setThemeColor("FFCC5C");
        }
        card.setPotentialAction(potentialActionBuilder.buildActionable(isCompactNotification()));

        return card;
    }
    
    private Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = run.getParent().getDisplayName();
        if (stepParameters.getStatus() != null) {
            Fact fact = FactsBuilder.buildStatus();
            fact.setValue(stepParameters.getStatus());
            factsBuilder.addFact(fact);
        } else {
            factsBuilder.addStatusRunning();
        }

        String activityTitle = "Message from " + jobName + ", Build #" + run.getNumber() + "";
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsBuilder.collect());

        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Status";
        Card card = new Card(summary, sectionList);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        card.setPotentialAction(potentialActionBuilder.buildActionable(isCompactNotification()));

        return card;
    }

    private void executeWorker(Webhook webhook, Card card) {
        try {
            HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhook.getUrl()), gson.toJson(card),
                    webhook.getTimeout(), listener.getLogger());
            worker.submit();
        } catch (IOException | InterruptedException | RejectedExecutionException e) {
            listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook,
                    e.getClass().getName(), e.getMessage()));
        }
    }

    private void processScmDetails(boolean addToFactsBuilder) {
        Set<User> users;
        List<ChangeLogSet<ChangeLogSet.Entry>> sets;

        try {
            users = (Set<User>) run.getClass().getMethod("getCulprits").invoke(run);
            sets = (List<ChangeLogSet<ChangeLogSet.Entry>>) run.getClass().getMethod("getChangeSets").invoke(run);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            users = Collections.emptySet();
            sets = Collections.emptyList();
        }

        if (addToFactsBuilder)
            factsBuilder.addCulprits(users);
        
        if (!sets.isEmpty()) {
            Set<User> authors;
            ArrayList<String> changes;
            authors = new HashSet<>();
            affectedFiles = new HashSet<>();
            changes = new ArrayList<>();


            if (Iterables.all(sets, Predicates.instanceOf(ChangeLogSet.class))) {
                for (ChangeLogSet<ChangeLogSet.Entry> set : sets) {
                    for (ChangeLogSet.Entry entry : set) {
                        changes.add(entry.getMsg() + " [" + entry.getAuthor().getDisplayName() + "]");
                        authors.add(entry.getAuthor());
                        affectedFiles.addAll(getAffectedFiles(entry));
                    }
                }
            }
            changesSingleString = getChangesAsOneString(changes);
            
            if (addToFactsBuilder)
            {
                factsBuilder.addDevelopers(authors);
                factsBuilder.addNumberOfFilesChanged(affectedFiles.size());    
            }
        }
        if (StringUtils.isEmpty(changesSingleString))
            changesSingleString = getChangesAsOneString(null);
    }

    private String getAuthorsString(Set<User> authors) {
        if (authors != null) {
            StringBuilder auhtorString = new StringBuilder();
            Iterator<User> iterator = authors.iterator();
            while (iterator.hasNext()) {
                auhtorString.append(iterator.next().getDisplayName() + " ");
            }
            return auhtorString.toString();
        } else return "";
    }

    private String getChangesAsOneString(ArrayList<String> changes) {       

        if (changes != null) {
            if (changes.size() > 0)
            {
                StringBuilder changedFiles = new StringBuilder();
                changedFiles.append("<ul>");
                for (String change : changes) {
                    changedFiles.append("<li>");
                    changedFiles.append(change);
                    changedFiles.append("</li>");
                }
                changedFiles.append("</ul>");
                return changedFiles.toString();    
            }
        } 
        return "No changes";
    }

    private Collection<? extends ChangeLogSet.AffectedFile> getAffectedFiles(ChangeLogSet.Entry entry) {
        try {
            return entry.getAffectedFiles();
        } catch (UnsupportedOperationException e) {
            listener.getLogger().println(e.getMessage());
            return Collections.emptyList();
        }
    }
}
