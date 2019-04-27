/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.StepParameters;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class CardBuilder {

    private final Run run;
    private final TaskListener listener;

    private final FactsBuilder factsBuilder;
    private final ActionableBuilder potentialActionBuilder;

    public CardBuilder(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;

        this.factsBuilder = new FactsBuilder(run);
        potentialActionBuilder = new ActionableBuilder(run, factsBuilder);
    }

    public Card createStartedCard() {
        factsBuilder.addStatusStarted();
        factsBuilder.addRemarks();
        factsBuilder.addCulprits();
        factsBuilder.addDevelopers();

        String jobName = getDisplayName();
        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build " + getRunName();
        Section section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());

        String summary = jobName + ": Build " + getRunName() + " Started";
        Card card = new Card(summary, section);
        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    public Card createCompletedCard() {
        String jobName = getDisplayName();
        // not available @ Microsoft Teams but probably is for other Office365 clients
        String summary = String.format("%s: Build %s ", jobName, getRunName());

        // result might be null for ongoing job - check documentation of Result.getResult()
        Result result = getResult(run);

        String status;
        Run previousBuild = run.getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        Run rt = run.getPreviousNotFailedBuild();
        Run failingSinceRun;
        if (rt != null) {
            failingSinceRun = rt.getNextBuild();
        } else {
            failingSinceRun = run.getParent().getFirstBuild();
        }

        if (result == Result.SUCCESS) {
            // back to normal
            if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                status = "Back to Normal";
                summary += " Back to Normal";
            }
            // still success
            else {
                status = "Build Success";
                summary += "Success";
            }
        } else if (result == Result.FAILURE) {
            if (failingSinceRun != null && previousResult == Result.FAILURE) {
                status = "Repeated Failure";
                summary += "Repeated Failure";

                factsBuilder.addFailingSinceBuild(failingSinceRun.number);
            } else {
                status = "Build Failed";
                summary += "Failed";
            }
        } else if (result == Result.ABORTED) {
            status = "Build Aborted";
            summary += "Aborted";
        } else if (result == Result.UNSTABLE) {
            status = "Build Unstable";
            summary += "Unstable";
        } else if (result == Result.NOT_BUILT) {
            status = "Not Built";
            summary += "Not Built";
        } else {
            // if we are here it means that something went wrong in logic above
            // and we are facing unsupported status or case
            log("Unknown result: " + result);
            status = result.toString();
            summary += status;
        }

        factsBuilder.addStatus(status);

        factsBuilder.addRemarks();
        factsBuilder.addCulprits();
        factsBuilder.addDevelopers();

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

    // this is tricky way to avoid findBugs NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
    // which is not true in that case
    private Result getResult(Run run) {
        return run.getResult();
    }

    public Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = getDisplayName();
        if (stepParameters.getStatus() != null) {
            factsBuilder.addStatus(stepParameters.getStatus());
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

    private String getDisplayName() {
        return run.getParent().getFullDisplayName();
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
