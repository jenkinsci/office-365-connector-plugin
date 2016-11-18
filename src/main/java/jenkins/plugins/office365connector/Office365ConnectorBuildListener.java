package jenkins.plugins.office365connector;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.test.AbstractTestResultAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Facts;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.plugins.office365connector.model.Sections;

/**
 * Office365ConnectorBuildListener {@link RunListener}.
 *
 * <p>When a build starts, the {@link #onStarted(Run, TaskListener)} method will be invoked. And
 * when a build finishes, the {@link #onCompleted(Run, TaskListener)} method will be invoked.
 *
 * @author Srivardhan Hebbar
 */

@Extension
public class Office365ConnectorBuildListener extends RunListener<Run> {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

    /**
     * Runs when the {@link Office365ConnectorBuildListener} class is created.
     */
    public Office365ConnectorBuildListener() { }
 
    /**
     * Called when a build is first started.
     *
     * @param run - A Run object representing a particular execution of Job.
     * @param listener - A TaskListener object which receives events that happen during some
     *                   operation.
     */
    @Override
    public final void onStarted(final Run run, final TaskListener listener) {
        WebhookJobProperty property = (WebhookJobProperty) run.getParent().getProperty(WebhookJobProperty.class);
        if (property == null) {
            return;
        }

        Card card = null;
        for (Webhook webhook : property.getWebhooks()) {
            if (webhook.isStartNotification()) {
                try {
                    card = createJobStartedCard(run, listener);
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

    /**
     * Called when a build is completed.
     *
     * @param run - A Run object representing a particular execution of Job.
     * @param listener - A TaskListener object which receives events that happen during some
     *                   operation.
     */
   @Override
    public final void onCompleted(final Run run, @Nonnull final TaskListener listener) {
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

    private Card createJobStartedCard(Run run, TaskListener listener) {
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

    private boolean shouldSendNotification(Webhook webhook, Run run) {
        Result result = run.getResult();
        Run previousBuild = run.getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        return ((result == Result.ABORTED && webhook.isNotifyAborted())
            || (result == Result.FAILURE && (webhook.isNotifyFailure()))
            || (result == Result.FAILURE && previousResult == Result.FAILURE && (webhook.isNotifyRepeatedFailure()))
            || (result == Result.NOT_BUILT && webhook.isNotifyNotBuilt())
            || (result == Result.SUCCESS && previousResult == Result.FAILURE && webhook.isNotifyBackToNormal())
            || (result == Result.SUCCESS && webhook.isNotifySuccess()) 
            || (result == Result.UNSTABLE && webhook.isNotifyUnstable()));
    }

    private Card createJobCompletedCard(Run run, TaskListener listener) {
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
}

