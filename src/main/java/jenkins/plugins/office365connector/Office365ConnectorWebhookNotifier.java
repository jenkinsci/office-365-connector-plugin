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
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import jenkins.plugins.office365connector.model.Facts;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.plugins.office365connector.model.Sections;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author srhebbar
 */
public final class Office365ConnectorWebhookNotifier {
    
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    
    public static void sendBuildStaredNotification(Run run, TaskListener listener, boolean isFromPrebuild)
    {
        Card card = null;
        if ((run instanceof AbstractBuild<?,?> && isFromPrebuild) ||
                (!(run instanceof AbstractBuild<?,?>) && !isFromPrebuild)) {
            card = getCard(run, listener, 1);
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
                    HttpWorker worker = new HttpWorker(webhook.getUrl(), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
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
    
    private static Card getCard(Run run, TaskListener listener, int cardType)
    {
        return getCard(run, listener, cardType, null);
    }
    
    private static Card getCard(Run run, TaskListener listener, int cardType, StepParameters stepParameters)
    {
        if(listener == null) return null;
        if(run == null) {
            listener.getLogger().println("Run is null!");
            return null;
        }
        
        switch (cardType) {
            case 1: return createJobStartedCard(run, listener);
            case 2: return createJobCompletedCard(run, listener);
            case 3: return createBuildMessageCard(run, listener, stepParameters);
            default: listener.getLogger().println("Default case! Not supposed to come here!");
        }
        
        return null;
    }
    
    public static void sendBuildCompleteNotification(Run run, TaskListener listener)
    {
        Card card = getCard(run, listener, 2);
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
            if (shouldSendNotification(webhook, run)) {
    //            listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                try {
                    HttpWorker worker = new HttpWorker(webhook.getUrl(), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
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
    
    public static void sendBuildMessage(Run run, TaskListener listener, StepParameters stepParameters)
    {
        Card card;
        if (StringUtils.isNotBlank(stepParameters.getMessage())) {
            card = getCard(run, listener, 3, stepParameters);
        } else if (StringUtils.equalsIgnoreCase(stepParameters.getStatus(), "started")) {
            card = getCard(run, listener, 1);
        } else {
            card = getCard(run, listener, 2);
        }
        if (card == null) {
            listener.getLogger().println(String.format("Build message card not generated."));
            return;
        }
        
        String webhookUrl = stepParameters.getWebhookUrl();
        try {
            if (webhookUrl != null) {
 //               listener.getLogger().println(String.format("Notifying webhook '%s'", webhookUrl));
                HttpWorker worker = new HttpWorker(webhookUrl, gson.toJson(card), 30000, 3, listener.getLogger());
                executorService.submit(worker);
            } else {
                WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
                if (property == null) {
                    return;
                }
                for (Webhook webhook : property.getWebhooks()) {
                    webhookUrl = webhook.getUrl();
    //                listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                    HttpWorker worker = new HttpWorker(webhookUrl, gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                    executorService.submit(worker);
                }
            }
        } catch (Throwable error) {
            error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhookUrl)));
            listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhookUrl, error.getClass().getName(), error.getMessage()));
        }
    }
    
    private static Card createJobStartedCard(Run run, TaskListener listener) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        List<Facts> factsList = new ArrayList<>();
        factsList.add(new Facts("Status", "Build Started"));
        factsList.add(new Facts("Start Time", sdf.format(run.getStartTimeInMillis())));

        addCauses(run, listener, factsList);
        
        String activityTitle = "Update from build " + run.getParent().getName() + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Sections section = new Sections(activityTitle, activitySubtitle, factsList);

        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        
        String summary = run.getParent().getName() + ": Build #" + run.getNumber() + " Started";
        Card card = new Card(summary, sectionList);
        addPotentialAction(run, card);

        return card;
    }
    
    private static Card createJobCompletedCard(Run run, TaskListener listener) {
        List<Facts> factsList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        String summary = run.getParent().getName() + ": Build #" + run.getNumber();
        
        Facts event = new Facts("Status");
        factsList.add(event);
        factsList.add(new Facts("Start Time", sdf.format(run.getStartTimeInMillis())));
             
        Result result = run.getResult();
        if (result != null) {
            long currentBuildCompletionTime = run.getStartTimeInMillis() + run.getDuration();
            factsList.add(new Facts("Completion Time", sdf.format(currentBuildCompletionTime)));

            AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
            if (action != null) {
                factsList.add(new Facts("Total Tests", action.getTotalCount()));
                factsList.add(new Facts("Total Passed Tests", action.getTotalCount() - action.getFailCount() - action.getSkipCount()));
                factsList.add(new Facts("Total Failed Tests", action.getFailCount()));
                factsList.add(new Facts("Total Skipped Tests", action.getSkipCount()));
            } else {
                factsList.add(new Facts("Tests", "No tests found"));
            }

            String status = null;
            Run previousBuild = run.getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            AbstractBuild failingSinceRun = null;
            Run rt;
            rt = run.getPreviousNotFailedBuild();
            try {
                if(rt != null) {
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
                    long diffInSeconds = (currentBuildCompletionTime / 1000) - (failingSinceRun.getStartTimeInMillis() / 1000);
                    long diff[] = new long[] { 0, 0, 0, 0 };
                    /* sec */diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
                    /* min */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
                    /* hours */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
                    /* days */diff[0] = (diffInSeconds = (diffInSeconds / 24));
                    String backToNormalTimeValue = String.format(
                                                    "%d day%s, %d hour%s, %d minute%s, %d second%s",
                                                    diff[0],
                                                    diff[0] > 1 ? "s" : "",
                                                    diff[1],
                                                    diff[1] > 1 ? "s" : "",
                                                    diff[2],
                                                    diff[2] > 1 ? "s" : "",
                                                    diff[3],
                                                    diff[3] > 1 ? "s" : "");
                    factsList.add(new Facts("Back To Normal Time", backToNormalTimeValue));
                }
            } else if (result == Result.FAILURE && failingSinceRun != null) {
                if (previousResult == Result.FAILURE) {
                    status = "Repeated Failure";
                    summary += " Repeated Failure";

                    factsList.add(new Facts("Failing since build", failingSinceRun.number));
                    factsList.add(new Facts("Failing since time", sdf.format(failingSinceRun.getStartTimeInMillis() + failingSinceRun.getDuration())));
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
            
        addCauses(run, listener, factsList);
        
        String activityTitle = "Update from build " + run.getParent().getName() + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Sections section = new Sections(activityTitle, activitySubtitle, factsList);

        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        
        Card card = new Card(summary, sectionList);
        addPotentialAction(run, card);

        return card;
    }

    private static Card createBuildMessageCard(Run run, TaskListener listener, StepParameters stepParameters) {
        List<Facts> factsList = new ArrayList<>();
        if (stepParameters.getStatus() != null) {
            factsList.add(new Facts("Status", stepParameters.getStatus()));
        } else {
            factsList.add(new Facts("Status", "Running"));
        }
        
        String activityTitle = "Update from build " + run.getParent().getName() + "(" + run.getNumber() + ")";
        Sections section = new Sections(activityTitle, stepParameters.getMessage(), factsList);
        
        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        
        String summary = run.getParent().getName() + ": Build #" + run.getNumber() + " Status";
        Card card = new Card(summary, sectionList);
        addPotentialAction(run, card);

        return card;
    }
    
    private static boolean shouldSendNotification(Webhook webhook, Run run) {
    Result result = run.getResult();
    Run previousBuild = run.getPreviousBuild();
    Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
    return ((result == Result.ABORTED && webhook.isNotifyAborted())
        || (result == Result.FAILURE && (webhook.isNotifyFailure()))
        || (result == Result.FAILURE && previousResult == Result.FAILURE && (webhook.isNotifyRepeatedFailure()))
        || (result == Result.NOT_BUILT && webhook.isNotifyNotBuilt())
        || (result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) && webhook.isNotifyBackToNormal())
        || (result == Result.SUCCESS && webhook.isNotifySuccess()) 
        || (result == Result.UNSTABLE && webhook.isNotifyUnstable()));
    }

    private static void addScmDetails(Run run, TaskListener listener, List<Facts> factsList) {
        try {
            if (run instanceof AbstractBuild) {
                AbstractBuild build = (AbstractBuild) run;
                Set<User> users = build.getCulprits();
                if (users != null ) {
                    Set<String> culprits = new HashSet<>();
                    for (User user : users) {
                        culprits.add(user.getFullName());
                    }
                    factsList.add(new Facts("Culprits", StringUtils.join(culprits, ", ")));
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

                    factsList.add(new Facts("Developers", StringUtils.join(authors, ", ")));
                    
                    if (!files.isEmpty()) {
                        factsList.add(new Facts("Number Of Files Changed", files.size()));
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
                        if (runResult != null && runResult.isWorseThan(Result.SUCCESS)) {
                            factsList.add(new Facts("Culprits", StringUtils.join(authors, ", ")));
                        }
                        
                        factsList.add(new Facts("Developers", StringUtils.join(authors, ", ")));
                        
                        if (!files.isEmpty()) {
                            factsList.add(new Facts("Number of Files Changed", files.size()));
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

    private static void addPotentialAction(Run run, Card card) {
        String rootUrl = null;
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
        if(rootUrl != null) {
            List<String> url;
            url = new ArrayList<>();
            url.add(rootUrl + run.getUrl());
            
            PotentialAction pa = new PotentialAction(url);
            List<PotentialAction> paList = new ArrayList<>();
            paList.add(pa);
            card.setPotentialAction(paList);
        }
    }

    private static void addCauses(Run run, TaskListener listener, List<Facts> factsList) {
        List<Cause> causes = run.getCauses();
        if (causes != null) {
           StringBuilder causesStr = new StringBuilder();
                for (Cause cause : causes) {
                    causesStr.append(cause.getShortDescription()).append(". ");
                }
            String cause = causesStr.toString();
            if (cause.contains("Branch indexing")) cause = cause.replace("Branch indexing", "SCM change");
            factsList.add(new Facts("Remarks", cause));

            if (cause.contains("SCM change")) {
                addScmDetails(run, listener, factsList);
            }
        }
    }
}
