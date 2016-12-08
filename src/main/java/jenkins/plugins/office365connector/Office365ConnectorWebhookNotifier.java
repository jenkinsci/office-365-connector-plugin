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
    
    private static Card createJobStartedCard(Run run, TaskListener listener, boolean isFromPrebuild) {
        if(run == null) return null;
        if(listener == null) return null;
  
        Card card = new Card();
        Sections section = new Sections();
        section.setMarkdown(true);

        String rootUrl = null;
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        Facts event = new Facts();
        event.setName("Status");

        String summary = run.getParent().getName() + ": Build #" + run.getNumber();

        Facts startTime = new Facts();
        startTime.setName("Start Time");
        startTime.setValue(sdf.format(run.getStartTimeInMillis()));

        List<Facts> factsList = new ArrayList<>();
        factsList.add(event);
        factsList.add(startTime);

        summary += " Started.";
        event.setValue("Build Started");

        List<Cause> causes = run.getCauses();
        Facts causeField = new Facts();
        if (causes != null) {
           StringBuilder causesStr = new StringBuilder();
                for (Cause cause : causes) {
                    causesStr.append(cause.getShortDescription()).append(". ");
                }
                causeField.setName("Remarks");
                causeField.setValue(causesStr.toString());
        }
        factsList.add(causeField);
        
        addScmDetails(run, listener, factsList);
        
        card.setSummary(summary);
        card.setTheme("#3479BF");

        section.setFacts(factsList);
        section.setActivityTitle("Update from build " + run.getParent().getName() + ".");
        section.setActivitySubtitle("Latest status of build #" + run.getNumber());

        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        card.setSections(sectionList);

        if(rootUrl != null) {
            PotentialAction pa = new PotentialAction();
            pa.setContext("http://schema.org");
            pa.setType("ViewAction");
            pa.setName("View Build");
            List<String> url;
            url = new ArrayList<>();

            url.add(rootUrl + run.getUrl());
            pa.setTarget(url);
            List<PotentialAction> paList = new ArrayList<>();
            paList.add(pa);
            card.setPotentialAction(paList);
        }

        return card;
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
    
    private static Card createJobCompletedCard(Run run, TaskListener listener) {
        if(run == null) return null;
        if(listener == null) return null;
        
        Card card = new Card();
 
        String rootUrl = null;
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
                
        Sections section = new Sections();
        section.setMarkdown(true);
            
        Facts event = new Facts();
        event.setName("Status");
            
        String summary = run.getParent().getName() + ": Build #" + run.getNumber();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        Facts startTime = new Facts();
        startTime.setName("Start Time");
        startTime.setValue(sdf.format(run.getStartTimeInMillis()));
        List<Facts> factsList = new ArrayList<>();
        factsList.add(event);
        factsList.add(startTime);
            
        Result result = run.getResult();
        if (result != null) {
            long currentBuildCompletionTime = run.getStartTimeInMillis() + run.getDuration();
            Facts buildCompletion = new Facts();
            buildCompletion.setName("Completion Time");
            buildCompletion.setValue(sdf.format(currentBuildCompletionTime));
            factsList.add(buildCompletion);

            AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
            if (action != null) {
                Facts totalTests = new Facts();
                totalTests.setName("Total Tests");

                Facts totalPassed = new Facts();
                totalPassed.setName("Total Passed Tests");

                Facts totalFailed = new Facts();
                totalFailed.setName("Total Failed Tests");

                Facts totalSkipped = new Facts();
                totalSkipped.setName("Total Skipped Tests");

                totalTests.setValue(action.getTotalCount());
                totalPassed.setValue(action.getTotalCount() - action.getFailCount() - action.getSkipCount());
                totalFailed.setValue(action.getFailCount());
                totalSkipped.setValue(action.getSkipCount());
                factsList.add(totalTests);
                factsList.add(totalPassed);
                factsList.add(totalFailed);
                factsList.add(totalSkipped);
            } else {
                Facts tests = new Facts();
                tests.setName("Tests");
                tests.setValue("No tests found");
                factsList.add(tests);
            }

            String status = result.toString();
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
                summary += " Back to Normal.";

                if (failingSinceRun != null) {
                    Facts backToNormalTime = new Facts();
                    backToNormalTime.setName("Back To Normal Time");
                    backToNormalTime.setValue(sdf.format(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis()));
                    factsList.add(backToNormalTime);
                }
            } else if (result == Result.FAILURE && failingSinceRun != null) {
                if (previousResult == Result.FAILURE) {
                    status = "Repeated Failure";
                    summary += " Repeated Failure";

                    Facts failingSinceBuild = new Facts();
                    failingSinceBuild.setName("Failing since build");
                    failingSinceBuild.setValue(failingSinceRun.number);
                    factsList.add(failingSinceBuild);

                    Facts failingSinceTime = new Facts();
                    failingSinceTime.setName("Failing since time");
                    failingSinceTime.setValue(sdf.format(failingSinceRun.getStartTimeInMillis() + failingSinceRun.getDuration()));
                    factsList.add(failingSinceTime);
                } else {
                    status = "Build Failed";
                    summary += " Failed.";
                }
            } else if (result == Result.ABORTED) {
                status = "Build Aborted";
                summary += " Aborted.";
            } else if (result == Result.UNSTABLE) {
                status = "Build Unstable";
                summary += " Unstable.";
            } else if (result == Result.SUCCESS) {
                status = "Build Success";
                summary += " Success.";
            } else if (result == Result.NOT_BUILT) {
                status = "Not Built";
                summary += " Not Built.";
            }

            event.setValue(status);
        }
            
        List<Cause> causes = run.getCauses();
        Facts causeField = new Facts();
        if (causes != null) {
            StringBuilder causesStr = new StringBuilder();
            for (Cause cause : causes) {
                causesStr.append(cause.getShortDescription()).append(". ");
            }
            causeField.setName("Remarks");
            causeField.setValue(causesStr.toString());
        }
        factsList.add(causeField);
        
        addScmDetails(run, listener, factsList);
        
        card.setSummary(summary);
        card.setTheme("#3479BF");

        section.setFacts(factsList);
        section.setActivityTitle("Update from build " + run.getParent().getName() + ".");
        section.setActivitySubtitle("Latest status of build #" + run.getNumber());

        List<Sections> sectionList = new ArrayList<>();
        sectionList.add(section);
        card.setSections(sectionList);

        if(rootUrl != null) {
            PotentialAction pa = new PotentialAction();
            pa.setContext("http://schema.org");
            pa.setType("ViewAction");
            pa.setName("View Build");
            List<String> url;
            url = new ArrayList<>();

            url.add(rootUrl + run.getUrl());
            pa.setTarget(url);
            List<PotentialAction> paList = new ArrayList<>();
            paList.add(pa);
            card.setPotentialAction(paList);
        }

        return card;
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
                    Facts authorList = new Facts();
                    authorList.setName("Authors");
                    authorList.setValue(StringUtils.join(authors, ", "));
                    factsList.add(authorList);
                    
                    Facts noOffiles = new Facts();
                    noOffiles.setName("Number Of Files Changed");
                    noOffiles.setValue(files.size());
                    factsList.add(noOffiles);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(listener.error(String.format("Unable to cast run to abstract build")));
        }
    }
}
