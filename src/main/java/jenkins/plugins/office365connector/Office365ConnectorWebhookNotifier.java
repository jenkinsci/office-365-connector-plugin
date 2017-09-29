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


import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

/**
 * @author srhebbar
 */
public final class Office365ConnectorWebhookNotifier {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

    private final Run run;
    private final TaskListener listener;

    public Office365ConnectorWebhookNotifier(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;
    }

    public void sendBuildStaredNotification(boolean isFromPrebuild) {
        Card card = null;
        if ((run instanceof AbstractBuild<?, ?> && isFromPrebuild) ||
                (run instanceof AbstractBuild<?, ?> || isFromPrebuild)) {
            card = getCard(1);
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
            if (webhook.isStartNotification()) {
                //         listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                try {
                    HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhook.getUrl()), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                    executorService.submit(worker);
                } catch (Throwable error) {
                    error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhook)));
                    listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook, error.getClass().getName(), error.getMessage()));
                }
            } else {
                //    listener.getLogger().println(String.format("No need to notify webhook '%s'", webhook));
            }
        }
    }

    public void sendBuildCompleteNotification() {
        Card card = getCard(2);
        if (card == null) {
            listener.getLogger().println(String.format("Build completed card not generated."));
            return;
        }

        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            //           listener.getLogger().println(String.format("No webhooks to notify"));
            return;
        }

        for (Webhook webhook : property.getWebhooks()) {
            if (shouldSendNotification(webhook)) {
                //            listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                try {
                    HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhook.getUrl()), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                    executorService.submit(worker);
                } catch (Throwable error) {
                    error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhook)));
                    listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook, error.getClass().getName(), error.getMessage()));
                }
            } else {
                //            listener.getLogger().println(String.format("No need to notify webhook '%s'", webhook));
            }
        }
    }

    public void sendBuildMessage(StepParameters stepParameters) {
        Card card;
        if (StringUtils.isNotBlank(stepParameters.getMessage())) {
            card = getCard(3, stepParameters);
        } else if (StringUtils.equalsIgnoreCase(stepParameters.getStatus(), "started")) {
            card = getCard(1);
        } else {
            card = getCard(2);
        }
        if (card == null) {
            listener.getLogger().println(String.format("Build message card not generated."));
            return;
        }

        String webhookUrl = stepParameters.getWebhookUrl();
        try {
            if (webhookUrl != null) {
                //               listener.getLogger().println(String.format("Notifying webhook '%s'", webhookUrl));
                HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhookUrl), gson.toJson(card), 30000, 3, listener.getLogger());
                executorService.submit(worker);
            } else {
                WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
                if (property == null) {
                    return;
                }
                for (Webhook webhook : property.getWebhooks()) {
                    webhookUrl = webhook.getUrl();
                    //                listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                    HttpWorker worker = new HttpWorker(run.getEnvironment(listener).expand(webhookUrl), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                    executorService.submit(worker);
                }
            }
        } catch (Throwable error) {
            error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhookUrl)));
            listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhookUrl, error.getClass().getName(), error.getMessage()));
        }
    }

    private Card getCard(int cardType) {
        return getCard(cardType, null);
    }

    private Card getCard(int cardType, StepParameters stepParameters) {
        if (listener == null) {
            return null;
        }
        if (run == null) {
            listener.getLogger().println("Run is null!");
            return null;
        }

        switch (cardType) {
            case 1:
                return createJobStartedCard();
            case 2:
                return createJobCompletedCard();
            case 3:
                return createBuildMessageCard(stepParameters);
            default:
                throw new IllegalArgumentException("Unsupported card type: " + cardType);
        }
    }

    private Card createJobStartedCard() {
        String jobName = decodeURIComponent(run.getParent().getDisplayName());

        List<Fact> factsList = new ArrayList<>();
        factsList.add(new Fact("Status", "Build Started"));
        factsList.add(buildStartTimeFact());

        addCauses(factsList);

        String activityTitle = "Update from build " + jobName + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Section section = new Section(activityTitle, activitySubtitle, factsList);

        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Started";
        Card card = new Card(summary, sectionList);
        addPotentialAction(card);

        return card;
    }

    private Card createJobCompletedCard() {
        List<Fact> factsList = new ArrayList<>();
        String jobName = decodeURIComponent(run.getParent().getDisplayName());
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
            AbstractBuild failingSinceRun = null;
            Run rt;
            rt = run.getPreviousNotFailedBuild();
            try {
                if (rt != null) {
                    failingSinceRun = (AbstractBuild) rt.getNextBuild();
                } else {
                    failingSinceRun = (AbstractBuild) run.getParent().getFirstBuild();
                }
            } catch (Throwable e) {
                //listener.getLogger().println(e.getMessage());
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

        String activityTitle = "Update from build " + jobName + ".";
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
        addPotentialAction(card);

        return card;
    }

    private Fact buildStartTimeFact() {
        return new Fact("Start Time", TimeUtils.dateToString(run.getStartTimeInMillis()));
    }

    private Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = decodeURIComponent(run.getParent().getName());
        List<Fact> factsList = new ArrayList<>();
        if (stepParameters.getStatus() != null) {
            factsList.add(new Fact("Status", stepParameters.getStatus()));
        } else {
            factsList.add(new Fact("Status", "Running"));
        }

        String activityTitle = "Update from build " + jobName + " (#" + run.getNumber() + ")";
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsList);

        List<Section> sectionList = new ArrayList<>();
        sectionList.add(section);

        String summary = jobName + ": Build #" + run.getNumber() + " Status";
        Card card = new Card(summary, sectionList);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        addPotentialAction(card);

        return card;
    }

    private boolean shouldSendNotification(Webhook webhook) {
        if (hasBuildMatched(webhook)) {
            if (webhook.getMacros().isEmpty()) {
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
        return false;
    }

    private boolean hasBuildMatched(Webhook webhook) {
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
                    try {
                        files.addAll(entry.getAffectedFiles());
                    } catch (Throwable e) {
                        listener.getLogger().println(e.getMessage());
                    }

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
                                    try {
                                        files.addAll(entry.getAffectedFiles());
                                    } catch (Throwable e) {
                                        listener.getLogger().println(e.getMessage());
                                    }
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

    private void addPotentialAction(Card card) {
        String rootUrl = null;
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
        if (rootUrl != null) {
            List<String> url;
            url = new ArrayList<>();
            url.add(rootUrl + run.getUrl());

            PotentialAction pa = new PotentialAction(url);
            List<PotentialAction> paList = new ArrayList<>();
            paList.add(pa);
            card.setPotentialAction(paList);
        }
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

    private static String decodeURIComponent(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected encoding error, expected UTF-8 encoding.", e);
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
}
