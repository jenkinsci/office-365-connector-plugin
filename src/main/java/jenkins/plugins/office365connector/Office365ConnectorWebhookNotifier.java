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


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.branch.Branch;
import jenkins.branch.BranchProjectFactory;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.StepParameters;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

/**
 * @author srhebbar
 */
public final class Office365ConnectorWebhookNotifier {

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

    private final Run run;
    private final TaskListener listener;

    public Office365ConnectorWebhookNotifier(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;
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
            if (isAtLeastOneRuleMatched(webhook)) {
                if (webhook.isStartNotification()) {
                    executeWorker(webhook, card);
                }
            }
        }
    }

    public void sendBuildCompleteNotification() {
        Card card = createJobCompletedCard();

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            //           listener.getLogger().println(String.format("No webhooks to notify"));
            return;
        }

        for (Webhook webhook : property.getWebhooks()) {
            if (isStatusMatched(webhook) && isAtLeastOneRuleMatched(webhook)) {
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
            String webhookUrl = stepParameters.getWebhookUrl();
            if (StringUtils.isBlank(webhookUrl)) {
                listener.getLogger().println("No URL provided");
                return;
            }
            try {
                new URL(webhookUrl);
                Webhook webhook = new Webhook(webhookUrl);
                executeWorker(webhook, card);
            } catch (MalformedURLException e) {
                listener.getLogger().println("Malformed URL provided");
                return;
            }
            return;
        }

        for (Webhook webhook : property.getWebhooks()) {
            executeWorker(webhook, card);
        }
    }

    private Card createJobStartedCard() {
        String jobName = run.getParent().getDisplayName();

        List<Fact> factsList = new ArrayList<>();
        factsList.add(new Fact("Status", "Build Started"));
        factsList.add(buildStartTimeFact());

        addCauses(factsList);

        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Section section = new Section(activityTitle, activitySubtitle, factsList);

        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Started";
        Card card = new Card(summary, sectionList);
        addPotentialAction(card, factsList);

        return card;
    }

    private Card createJobCompletedCard() {
        List<Fact> factsList = new ArrayList<>();
        String jobName = run.getParent().getDisplayName();
        String summary = jobName + ": Build #" + run.getNumber();

        Fact event = new Fact("Status");
        factsList.add(event);
        factsList.add(buildStartTimeFact());

        // Result is only set to a worse status in pipeline
        Result result = run.getResult() == null ? Result.SUCCESS : run.getResult();
        if (result != null) {
            long duration = run.getDuration() == 0L
                    ? System.currentTimeMillis() - run.getStartTimeInMillis()
                    : run.getDuration();
            long currentBuildCompletionTime = run.getStartTimeInMillis() + duration;

            factsList.add(new Fact("Completion Time", TimeUtils.dateToString(currentBuildCompletionTime)));

            AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
            if (action != null) {
                factsList.add(new Fact("Total Tests", action.getTotalCount()));
                factsList.add(new Fact("Total Passed Tests", action.getTotalCount() - action.getFailCount() - action.getSkipCount()));
                factsList.add(new Fact("Total Failed Tests", action.getFailCount()));
                factsList.add(new Fact("Total Skipped Tests", action.getSkipCount()));
            }

            String status = null;
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
                    factsList.add(new Fact("Back To Normal Time", TimeUtils.durationToString(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis())));
                }
            } else if (result == Result.FAILURE && failingSinceRun != null) {
                if (previousResult == Result.FAILURE) {
                    status = "Repeated Failure";
                    summary += " Repeated Failure";

                    factsList.add(new Fact("Failing since build", failingSinceRun.number));
                    factsList.add(new Fact("Failing since time",
                            TimeUtils.dateToString(failingSinceRun.getStartTimeInMillis() + failingSinceRun.getDuration())));
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

            event.setValue(status);
        } else {
            event.setValue(" Completed");
            summary += " Completed";
        }

        addCauses(factsList);

        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Section section = new Section(activityTitle, activitySubtitle, factsList);

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
        addPotentialAction(card, factsList);

        return card;
    }

    private Fact buildStartTimeFact() {
        return new Fact("Start Time", TimeUtils.dateToString(run.getStartTimeInMillis()));
    }

    private Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = run.getParent().getDisplayName();
        List<Fact> factsList = new ArrayList<>();
        if (stepParameters.getStatus() != null) {
            factsList.add(new Fact("Status", stepParameters.getStatus()));
        } else {
            factsList.add(new Fact("Status", "Running"));
        }

        String activityTitle = "Message from " + jobName + ", Build #" + run.getNumber() + "";
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsList);

        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Status";
        Card card = new Card(summary, sectionList);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        addPotentialAction(card, factsList);

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

    /**
     * Iterates over each macro for passed webhook and checks if at least one template matches to expected value.
     *
     * @param webhook webhook that should be examined
     * @return <code>true</code> if at least one macro has matched, <code>false</code> otherwise
     */
    private boolean isAtLeastOneRuleMatched(Webhook webhook) {
        if (CollectionUtils.isEmpty(webhook.getMacros())) {
            return true;
        } else {
            for (Macro macro : webhook.getMacros()) {
                String evaluated = evaluateMacro(macro.getTemplate());
                if (evaluated.equals(macro.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Checks if notification should be passed by comparing current status and webhook configuration
     *
     * @param webhook webhook that will be verified
     * @return <code>true</code> if current status matches to webhook configuration
     */
    private boolean isStatusMatched(Webhook webhook) {
        Result result = run.getResult();
        Run previousBuild = run.getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;

        return ((result == Result.ABORTED && webhook.isNotifyAborted())
                || (result == Result.FAILURE && previousResult != Result.FAILURE && (webhook.isNotifyFailure()))
                || (result == Result.FAILURE && previousResult == Result.FAILURE && (webhook.isNotifyRepeatedFailure()))
                || (result == Result.NOT_BUILT && webhook.isNotifyNotBuilt())
                || (result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) && webhook.isNotifyBackToNormal())
                || (result == Result.SUCCESS && webhook.isNotifySuccess())
                || (result == Result.UNSTABLE && webhook.isNotifyUnstable()));
    }

    private void addScmDetails(List<Fact> factsList) {
        try {
            if (run instanceof AbstractBuild) {
                AbstractBuild build = (AbstractBuild) run;
                Set<User> users = build.getCulprits();
                if (users != null) {
                    Set<String> culprits = new HashSet<>();
                    for (User user : users) {
                        culprits.add(user.getFullName());
                    }
                    factsList.add(new Fact("Culprits", StringUtils.join(culprits, ", ")));
                }

                ChangeLogSet changeSet = build.getChangeSet();
                List<ChangeLogSet.Entry> entries = new LinkedList<>();
                Set<ChangeLogSet.AffectedFile> files = new HashSet<>();
                for (Object o : changeSet.getItems()) {
                    ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
                    entries.add(entry);
                    files.addAll(getAffectedFiles(entry));
                }
                if (!entries.isEmpty()) {
                    Set<String> authors = new HashSet<>();
                    for (ChangeLogSet.Entry entry : entries) {
                        authors.add(entry.getAuthor().getFullName());
                    }

                    if (!authors.isEmpty()) {
                        factsList.add(new Fact("Developers", StringUtils.join(authors, ", ")));
                    }

                    if (!files.isEmpty()) {
                        factsList.add(new Fact("Number Of Files Changed", files.size()));
                    }
                }
            } else {
                try {
                    Method getChangeSets = run.getClass().getMethod("getChangeSets");
                    if (List.class.isAssignableFrom(getChangeSets.getReturnType())) {
                        @SuppressWarnings("unchecked")
                        List<ChangeLogSet<ChangeLogSet.Entry>> sets = (List<ChangeLogSet<ChangeLogSet.Entry>>) getChangeSets.invoke(run);
                        Set<String> authors = new HashSet<>();
                        Set<ChangeLogSet.AffectedFile> files = new HashSet<>();
                        if (Iterables.all(sets, Predicates.instanceOf(ChangeLogSet.class))) {
                            for (ChangeLogSet<ChangeLogSet.Entry> set : sets) {
                                for (ChangeLogSet.Entry entry : set) {
                                    authors.add(entry.getAuthor().getFullName());
                                    files.addAll(getAffectedFiles(entry));
                                }
                            }
                        }
                        Result runResult = run.getResult();

                        if (!authors.isEmpty()) {
                            if (runResult != null && runResult.isWorseThan(Result.SUCCESS)) {
                                factsList.add(new Fact("Culprits", StringUtils.join(authors, ", ")));
                            }

                            factsList.add(new Fact("Developers", StringUtils.join(authors, ", ")));
                        }

                        if (!files.isEmpty()) {
                            factsList.add(new Fact("Number of Files Changed", files.size()));
                        }
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace(listener.error(String.format("Exception getting changesets for %s: %s", run, e)));
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            e.printStackTrace(listener.error(String.format("Unable to cast run to abstract build. %s", e)));
        }
    }

    private Collection<? extends ChangeLogSet.AffectedFile> getAffectedFiles(ChangeLogSet.Entry entry) {
        try {
            return entry.getAffectedFiles();
        } catch (UnsupportedOperationException e) {
            listener.getLogger().println(e.getMessage());
            return Collections.emptyList();
        }
    }

    private void addPotentialAction(Card card, List<Fact> factsList) {
        String urlString = DisplayURLProvider.get().getRunURL(run);
        PotentialAction viewBuildPotentialAction = new PotentialAction("View Build", urlString);
        List<PotentialAction> paList = new ArrayList<>();
        paList.add(viewBuildPotentialAction);
        card.setPotentialAction(paList);
        pullRequestActionable(paList, factsList);
    }

    private void addCauses(List<Fact> factsList) {
        List<Cause> causes = run.getCauses();
        if (causes != null) {
            StringBuilder causesStr = new StringBuilder();
            for (Cause cause : causes) {
                causesStr.append(cause.getShortDescription()).append(". ");
            }
            String cause = causesStr.toString();
            factsList.add(new Fact("Remarks", cause));
            addScmDetails(factsList);
        }
    }

    private String evaluateMacro(String template) {
        try {
            File workspace = run.getRootDir();
            return TokenMacro.expandAll(run, new FilePath(workspace), listener, template);
        } catch (InterruptedException | IOException | MacroEvaluationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void pullRequestActionable(List<PotentialAction> paList, List<Fact> factsList) {
        Job job = run.getParent();
        ItemGroup parent = job.getParent();
        if (parent instanceof MultiBranchProject) {
            BranchProjectFactory projectFactory = ((MultiBranchProject) parent).getProjectFactory();
            if (projectFactory.isProject(job)) {
                Branch branch = projectFactory.getBranch(job);
                SCMHead head = branch.getHead();

                if (head instanceof ChangeRequestSCMHead) {
                    String pronoun = StringUtils.defaultIfBlank(head.getPronoun(), "Change Request");
                    String viewName = String.format("View %s", pronoun);
                    String titleName = String.format("%s Title", pronoun);
                    String authorName = String.format("%s Author", pronoun);

                    ObjectMetadataAction oma = branch.getAction(ObjectMetadataAction.class);
                    if (oma != null) {
                        String urlString = oma.getObjectUrl();
                        PotentialAction viewPRPotentialAction = new PotentialAction(viewName, urlString);
                        paList.add(viewPRPotentialAction);
                        factsList.add(new Fact(titleName, oma.getObjectDisplayName()));
                    }
                    ContributorMetadataAction cma = branch.getAction(ContributorMetadataAction.class);
                    if (cma != null) {
                        String contributor = cma.getContributor();
                        String contributorDisplayName = cma.getContributorDisplayName();
                        String author = StringUtils.defaultIfBlank(cma.getContributor(), cma.getContributorDisplayName());
                        if (StringUtils.isNotBlank(contributor) && StringUtils.isNotBlank(contributorDisplayName))
                            author = String.format("%s (%s)", cma.getContributor(), cma.getContributorDisplayName());

                        if (StringUtils.isNotBlank(author))
                            factsList.add(new Fact(authorName, author));
                    }
                }
            }
        }
    }
}
