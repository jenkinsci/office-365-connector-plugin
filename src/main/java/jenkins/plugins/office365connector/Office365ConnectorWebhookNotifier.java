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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
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
        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            return;
        }

        Card card = null;
        for (Webhook webhook : property.getWebhooks()) {
            if (webhook.isStartNotification()) {
                try {
                    AbstractBuild build = (AbstractBuild) run;
                    if(isFromPrebuild) {
                        card = createJobStartedCard(run, listener, isFromPrebuild);
                    }
                } catch (Throwable e) {
                    if(!isFromPrebuild) {
                        card = createJobStartedCard(run, listener, isFromPrebuild);
                    }
		}
                listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                if (card != null ) {
                    try {
                        HttpWorker worker = new HttpWorker(webhook.getUrl(), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                        executorService.submit(worker);
                    } catch (Throwable error) {
                        error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhook)));
                        listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook, error.getClass().getName(), error.getMessage()));
                    }
                }
            }
        }
    }
    
    public static void sendBuildCompleteNotification(Run run, TaskListener listener)
    {
        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            return;
        }

        Card card = null;
        for (Webhook webhook : property.getWebhooks()) {
            if (shouldSendNotification(webhook, run)) {
                try {
                    card = createJobCompletedCard(run, listener);
                } catch (Throwable e) {
                    e.printStackTrace(listener.error(String.format("Unable to build the json object")));
                    listener.getLogger().println(String.format("Unable to build the json object - %s: %s", e.getClass().getName(), e.getMessage()));
		}
                listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
                if (card != null ) {
                    try {
                        HttpWorker worker = new HttpWorker(webhook.getUrl(), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                        executorService.submit(worker);
                    } catch (Throwable error) {
                        error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhook)));
                        listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook, error.getClass().getName(), error.getMessage()));
                    }
                }
            }
        }
    }
    
    public static void sendBuildMessage(Run run, TaskListener listener, String message)
    {
        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            return;
        }

        Card card = null;
        for (Webhook webhook : property.getWebhooks()) {
            card = createBuildMessageCard(run, listener, message);
            listener.getLogger().println(String.format("Notifying webhook '%s'", webhook));
            if (card != null ) {
                try {
                    HttpWorker worker = new HttpWorker(webhook.getUrl(), gson.toJson(card), webhook.getTimeout(), 3, listener.getLogger());
                    executorService.submit(worker);
                } catch (Throwable error) {
                    error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", webhook)));
                    listener.getLogger().println(String.format("Failed to notify webhook '%s' - %s: %s", webhook, error.getClass().getName(), error.getMessage()));
                }
            }
        }
    }
    
    private static Card createJobStartedCard(Run run, TaskListener listener, boolean isFromPrebuild) {
        if(run == null) return null;
        if(listener == null) return null;
  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        List<Facts> factsList = new ArrayList<>();
        factsList.add(new Facts("Status", "Build Started"));
        factsList.add(new Facts("Start Time", sdf.format(run.getStartTimeInMillis())));

        addCauses(run, factsList);

        addScmDetails(run, listener, factsList);
        
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
        if(run == null) return null;
        if(listener == null) return null;
        
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
                e.printStackTrace(listener.error(String.format("Unable to cast run to abstract build")));
            }
            
            if (result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)) {
                status = "Back to Normal";
                summary += " Back to Normal";

                if (failingSinceRun != null) {
                    factsList.add(new Facts("Back To Normal Time", sdf.format(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis())));
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
            
        addCauses(run, factsList);
        
        addScmDetails(run, listener, factsList);
        
        String activityTitle = "Update from build " + run.getParent().getName() + ".";
        String activitySubtitle = "Latest status of build #" + run.getNumber();
        Sections section = new Sections(activityTitle, activitySubtitle, factsList);

        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        
        Card card = new Card(summary, sectionList);
        addPotentialAction(run, card);

        return card;
    }

    private static Card createBuildMessageCard(Run run, TaskListener listener, String message) {
        if(run == null) return null;
        if(listener == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        
        
        List<Facts> factsList = new ArrayList<>();
        factsList.add(new Facts("Status", "Running"));
        factsList.add(new Facts("Start Time", sdf.format(run.getStartTimeInMillis())));
            
        String activityTitle = "Update from build " + run.getParent().getName() + "(" + run.getNumber() + ")";
        Sections section = new Sections(activityTitle, message, factsList);
        
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
            AbstractBuild build = (AbstractBuild) run;
            if (build.hasChangeSetComputed()) {
                ChangeLogSet changeSet = build.getChangeSet();
                List<ChangeLogSet.Entry> entries = new LinkedList<>();
                Set<ChangeLogSet.AffectedFile> files = new HashSet<>();
                for (Object o : changeSet.getItems()) {
                    ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
                    entries.add(entry);
                    files.addAll(entry.getAffectedFiles());
                }
                if (!entries.isEmpty()) {
                    Set<String> authors = new HashSet<>();
                    for (ChangeLogSet.Entry entry : entries) {
                        authors.add(entry.getAuthor().getDisplayName());
                    }
                    
                    factsList.add(new Facts("Authors", StringUtils.join(authors, ", ")));
                    factsList.add(new Facts("Number Of Files Changed", files.size()));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(listener.error(String.format("Unable to cast run to abstract build")));
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

    private static void addCauses(Run run, List<Facts> factsList) {
        List<Cause> causes = run.getCauses();
        if (causes != null) {
           StringBuilder causesStr = new StringBuilder();
                for (Cause cause : causes) {
                    causesStr.append(cause.getShortDescription()).append(". ");
                }
            factsList.add(new Facts("Remarks", causesStr.toString()));
        }
    }
}
