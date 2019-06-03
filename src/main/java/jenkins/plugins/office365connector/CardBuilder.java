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
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.StepParameters;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class CardBuilder {

    private final Run run;

    private final FactsBuilder factsBuilder;
    private final ActionableBuilder potentialActionBuilder;

    public CardBuilder(Run run) {
        this.run = run;

        factsBuilder = new FactsBuilder(run);
        potentialActionBuilder = new ActionableBuilder(run, factsBuilder);
    }

    public Card createStartedCard() {
        factsBuilder.addStatusStarted();
        factsBuilder.addRemarks();
        factsBuilder.addCulprits();
        factsBuilder.addDevelopers();

        String jobName = getDisplayName();
        // TODO: dot in the message with single sentence should be removed
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
        // result might be null only for ongoing job - check documentation of Result.getCompletedResult()
        Result lastResult = getCompletedResult(run);

        Run previousBuild = run.getPreviousBuild();
        Result previousResult = previousBuild != null ? previousBuild.getResult() : Result.SUCCESS;
        Run lastNotFailedBuild = run.getPreviousNotFailedBuild();

        boolean isRepeatedFailure = isRepeatedFailure(previousResult, lastNotFailedBuild);
        String summary = String.format("%s: Build %s %s", jobName, getRunName(),
                calculateSummary(lastResult, previousResult, isRepeatedFailure));

        if (lastResult == Result.FAILURE) {
            Run failingSinceRun = getFailingSince(lastNotFailedBuild);

            if (failingSinceRun != null && previousResult == Result.FAILURE) {
                factsBuilder.addFailingSinceBuild(failingSinceRun.number);
            }
        }
        factsBuilder.addStatus(calculateStatus(lastResult, previousResult, isRepeatedFailure));
        factsBuilder.addRemarks();
        factsBuilder.addCulprits();
        factsBuilder.addDevelopers();

        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build " + getRunName();
        Section section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());

        Card card = new Card(summary, section);
        card.setThemeColor(lastResult.color.getHtmlBaseColor());
        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    private boolean isRepeatedFailure(Result previousResult, Run lastNotFailedBuild) {
        Run failingSinceRun = getFailingSince(lastNotFailedBuild);

        return failingSinceRun != null && previousResult == Result.FAILURE;
    }

    private Run getFailingSince(Run lastNotFailedBuild) {
        return lastNotFailedBuild != null
                ? lastNotFailedBuild.getNextBuild() : run.getParent().getFirstBuild();
    }

    String calculateStatus(Result lastResult, Result previousResult, boolean isRepeatedFailure) {
        if (lastResult == Result.SUCCESS) {
            // back to normal
            if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                return "Back to Normal";
            }
            // success remains
            return "Build Success";
        }
        if (lastResult == Result.FAILURE) {
            if (isRepeatedFailure) {
                return "Repeated Failure";
            }
            return "Build Failed";
        }
        if (lastResult == Result.ABORTED) {
            return "Build Aborted";
        }
        if (lastResult == Result.UNSTABLE) {
            return "Build Unstable";
        }

        return lastResult.toString();
    }

    String calculateSummary(Result completedResult, Result previousResult, boolean isRepeatedFailure) {

        if (completedResult == Result.SUCCESS) {
            // back to normal
            if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                return "Back to Normal";
            }
            // success remains
            return "Success";
        }
        if (completedResult == Result.FAILURE) {
            if (isRepeatedFailure) {
                return "Repeated Failure";
            }
            return "Failed";
        }
        if (completedResult == Result.ABORTED) {
            return "Aborted";
        }
        if (completedResult == Result.UNSTABLE) {
            return "Unstable";
        }

        return completedResult.toString();
    }

    // this is tricky way to avoid findBugs NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
    // which is not true in that case
    private Result getCompletedResult(Run run) {
        return run.getResult();
    }

    public Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = getDisplayName();
        if (stepParameters.getStatus() != null) {
            factsBuilder.addStatus(stepParameters.getStatus());
        } else {
            factsBuilder.addStatusRunning();
        }

        String activityTitle = "Message from " + jobName + ", Build " + getRunName();
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsBuilder.collect());

        String summary = jobName + ": Build " + getRunName() + " Status";
        Card card = new Card(summary, section);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    /**
     * Returns name of the job presented as display name without parent name such as folder.
     */
    private String getDisplayName() {
        return run.getParent().getDisplayName();
    }

    private String getRunName() {
        // TODO: This is probably not needed as mostly/always getNumber() is called
        return run.hasCustomDisplayName() ? run.getDisplayName() : "#" + run.getNumber();
    }
}
