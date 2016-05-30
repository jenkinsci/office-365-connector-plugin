/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import hudson.Util;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jenkins.model.Jenkins;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.tasks.test.AbstractTestResultAction;
import java.text.SimpleDateFormat;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Facts;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.plugins.office365connector.model.Sections;

@SuppressWarnings({ "unchecked", "rawtypes" })
public enum Phase {
	STARTED, COMPLETED;

	private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.create();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	public void handle(AbstractBuild build, TaskListener listener) {

		WebhookJobProperty property = (WebhookJobProperty) build.getParent().getProperty(WebhookJobProperty.class);
		if (property == null) {
			return;
		}
                
                Card card = null;
		try {
                    card = buildJobState(build.getParent(), build, listener);
		} catch (Throwable e) {
			e.printStackTrace(listener.error(String.format("Unable to build the json object")));
			listener.getLogger().println(
					String.format("Unable to build the json object - %s: %s", e.getClass().getName(),
							e.getMessage()));
		}
		if(card != null) {
			for (Webhook target : property.getWebhooks()) {
				if (isRun(target, build)) {
					listener.getLogger().println(String.format("Notifying webhook '%s'", target));
					try {
						
						HttpWorker worker = new HttpWorker(target.getUrl(), gson.toJson(card), target.getTimeout(), 3,
								listener.getLogger());
						executorService.submit(worker);
					} catch (Throwable error) {
						error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", target)));
						listener.getLogger().println(
								String.format("Failed to notify webhook '%s' - %s: %s", target, error.getClass().getName(),
										error.getMessage()));
					}
				}
			}
		}
	}

	/**
	 * Determines if the webhook specified should be notified at the current job
	 * phase.
	 */
	private boolean isRun(Webhook webhook, AbstractBuild build) {
		if (this.equals(STARTED) && webhook.isStartNotification()) {
			return true;
		} else if (this.equals(COMPLETED)) {
			Result result = build.getResult();
			Run previousBuild = build.getPreviousBuild();
			Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
			return ((result == Result.ABORTED && webhook.isNotifyAborted())
					|| (result == Result.FAILURE && webhook.isNotifyFailure())
					|| (result == Result.NOT_BUILT && webhook.isNotifyNotBuilt())
					|| (result == Result.SUCCESS && previousResult == Result.FAILURE && webhook.isNotifyBackToNormal())
					|| (result == Result.SUCCESS && webhook.isNotifySuccess()) || (result == Result.UNSTABLE && webhook
					.isNotifyUnstable()));

		} else {
			return false;
		}
	}

	/**
	 * Creates an object that is sent as the post data.
	 * 
	 * @param job
	 * @param run
	 * @param listener
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private Card buildJobState(Job job, AbstractBuild run, TaskListener listener) throws IOException,
			InterruptedException {

            if(job == null) return null;
            if(run == null) return null;
            if(listener == null) return null;
            
            Card card = new Card();
            
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) return null;
            
            String rootUrl = jenkins.getRootUrl();
            long currentBuildCompletionTime = run.getStartTimeInMillis() + run.getDuration();
                
            Sections section = new Sections();
            section.setMarkdown(true);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            
            Facts event = new Facts();
            event.setName("Status");
            
            String summary = job.getName() + ": Build #" + run.getNumber();
            Result result = run.getResult();
            
            Facts startTime = new Facts();
            startTime.setName("Start Time");
            startTime.setValue(sdf.format(run.getStartTimeInMillis()));

            List<Facts> factsList = new ArrayList<Facts>();
            factsList.add(event);
            factsList.add(startTime);
            
            if (this.equals(STARTED)) {
                summary += " started.";
                event.setValue("Build Started");
            } else if (this.equals(COMPLETED)){
                if (result != null) {
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
                    if(rt != null) {
			failingSinceRun = (AbstractBuild) (rt.getNextBuild());
                    } else {
			failingSinceRun = run.getProject().getFirstBuild();
                    }
                    if (result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)) {
			status = "Back to Normal";
                        summary += " Bank to Normal.";
                                        
                        Facts backToNormalTime = new Facts();
                        backToNormalTime.setName("Back To Normal Time");
                        backToNormalTime.setValue(sdf.format(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis()));
                        factsList.add(backToNormalTime);
                    } else if (result == Result.FAILURE && failingSinceRun != null) {
                        if (previousResult == Result.FAILURE) {
                            status = "Repeated Failure";
                            summary += " Repeat Failure";
                            
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
                    }
                    
                    event.setValue(status);
                }
            }
            
            List<Cause> causes = run.getCauses();
            Facts causeField = new Facts();
            if (causes != null) {
               StringBuffer causesStr = new StringBuffer();
                    for (Cause cause : causes) {
                        causesStr.append(cause.getShortDescription() + ". ");
                    }
                    causeField.setName("Remarks");
                    causeField.setValue(causesStr.toString());
            }
            factsList.add(causeField);
            
            card.setSummary(summary);
            card.setTheme("#3479BF");
            
            section.setFacts(factsList);
            section.setActivityTitle("Update from build " + job.getName() + ".");
            section.setActivitySubtitle("Latest status of build #" + run.getNumber());
            
            List<Sections> sectionList = new ArrayList<Sections>();
            sectionList.add(section);
            card.setSections(sectionList);
            
            if(rootUrl != null) {
                PotentialAction pa = new PotentialAction();
                pa.setContext("http://schema.org");
                pa.setType("ViewAction");
                pa.setName("View Build");
                List<String> url;
                url = new ArrayList<String>();

                url.add(rootUrl + run.getUrl());
                pa.setTarget(url);
                List<PotentialAction> paList = new ArrayList<PotentialAction>();
                paList.add(pa);
                card.setPotentialAction(paList);
            }
            
            return card;
          }
 
}