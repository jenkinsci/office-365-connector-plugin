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

import java.util.List;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.model.adaptivecard.AdaptiveCard;
import jenkins.plugins.office365connector.model.messagecard.MessageCard;
import jenkins.plugins.office365connector.workflow.StepParameters;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class CardBuilder {

    private final Run run;

    private final FactsBuilder factsBuilder;
    private final ActionableBuilder potentialActionBuilder;
    private final boolean isAdaptiveCards;
    private final boolean mentionCommitters;
    private final boolean mentionDevelopers;
    private final boolean mentionOnFailure;

// Old constructor kept for backward compatibility
public CardBuilder(Run run, TaskListener taskListener, boolean isAdaptiveCards) {
    this(run, taskListener, isAdaptiveCards, false, false, true); // sensible defaults
}

// Main constructor with all options
public CardBuilder(Run run, TaskListener taskListener, boolean isAdaptiveCards,
                   boolean mentionCommitters, boolean mentionDevelopers,
                   boolean mentionOnFailure) {
    this.run = run;
    this.isAdaptiveCards = isAdaptiveCards;
    this.mentionCommitters = mentionCommitters;
    this.mentionDevelopers = mentionDevelopers;
    this.mentionOnFailure = mentionOnFailure;

    factsBuilder = new FactsBuilder(run, taskListener);
    potentialActionBuilder = new ActionableBuilder(run, factsBuilder, isAdaptiveCards);
}

    public Card createStartedCard(List<FactDefinition> factDefinitions) {
        final String statusName = "Started";
        factsBuilder.addStatus(statusName);
        factsBuilder.addRemarks();
        factsBuilder.addCommitters();
        factsBuilder.addDevelopers();
        factsBuilder.addUserFacts(factDefinitions);

        Section section = buildSection(statusName);

        String summary = getDisplayName() + ": Build " + getRunName();
        Card card = isAdaptiveCards ? new AdaptiveCard(summary, section, getCompletedResult(run)) : new MessageCard(summary, section);
        card.setAction(potentialActionBuilder.buildActionable());

        return card;
    }

    public Card createCompletedCard(List<FactDefinition> factDefinitions) {
        // result might be null only for ongoing job - check documentation of Run.getCompletedResult()
        // but based on issue #133 it may happen that result for completed job is null
        Result lastResult = getCompletedResult(run);

        Run previousBuild = run.getPreviousBuild();
        Result previousResult = previousBuild != null ? previousBuild.getResult() : Result.SUCCESS;
        Run lastNotFailedBuild = run.getPreviousNotFailedBuild();

        boolean isRepeatedFailure = isRepeatedFailure(previousResult, lastNotFailedBuild);

        boolean shouldMention = (lastResult == Result.FAILURE || lastResult == Result.UNSTABLE || isRepeatedFailure) && mentionOnFailure;

        String summary = String.format("%s: Build %s %s", getDisplayName(), getRunName(),
                calculateSummary(lastResult, previousResult, isRepeatedFailure));
        String status = calculateStatus(lastResult, previousResult, isRepeatedFailure);

        if (lastResult == Result.FAILURE) {
            Run failingSinceBuild = getFailingSinceBuild(lastNotFailedBuild);

            if (failingSinceBuild != null && previousResult == Result.FAILURE) {
                factsBuilder.addFailingSinceBuild(failingSinceBuild.getNumber());
            }
        }
        factsBuilder.addStatus(status);
        factsBuilder.addRemarks();
        factsBuilder.addCommitters(mentionCommitters && shouldMention);
        factsBuilder.addDevelopers(mentionDevelopers && shouldMention);
        factsBuilder.addUserFacts(factDefinitions);

        Section section = buildSection(status);

        Card card = isAdaptiveCards ? new AdaptiveCard(summary, section, getCompletedResult(run)) : new MessageCard(summary, section);
        card.setThemeColor(getCardThemeColor(lastResult));
        if (run.getResult() != Result.SUCCESS) {
            card.setAction(potentialActionBuilder.buildActionable());
        }

        return card;
    }

    private static String getCardThemeColor(Result result) {
        if (result == Result.SUCCESS) {
            // Return green for success
            return "#00FF00";
        } else {
            return result.color.getHtmlBaseColor();
        }
    }

    private Section buildSection(String status) {
        String activityTitle = "Notification from " + getEscapedDisplayName() + ": " + status;
        String activitySubtitle = "Latest status of build " + getRunName();
        return new Section(activityTitle, activitySubtitle, factsBuilder.collect());
    }

    private boolean isRepeatedFailure(Result previousResult, Run lastNotFailedBuild) {
        Run failingSinceRun = getFailingSinceBuild(lastNotFailedBuild);

        return failingSinceRun != null && previousResult == Result.FAILURE;
    }

    private Run getFailingSinceBuild(Run lastNotFailedBuild) {
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
        return run.getResult() == null ? Result.SUCCESS : run.getResult();
    }

    public Card createBuildMessageCard(StepParameters stepParameters) {
        if (stepParameters.getStatus() != null) {
            factsBuilder.addStatus(stepParameters.getStatus());
        }
        factsBuilder.addUserFacts(stepParameters.getFactDefinitions());

        String activityTitle = "Notification from " + getEscapedDisplayName();
        Section section = new Section(activityTitle, stepParameters.getMessage(), factsBuilder.collect());

        String summary = getDisplayName() + ": Build " + getRunName();
        Card card = isAdaptiveCards ? new AdaptiveCard(summary, section, getCompletedResult(run)) : new MessageCard(summary, section);

        if (stepParameters.getColor() != null) {
            card.setThemeColor(stepParameters.getColor());
        }

        card.setAction(potentialActionBuilder.buildActionable());

        return card;
    }

    /**
     * Returns escaped name of the job presented as display name with parent name such as folder.
     * Parent is needed for multi-branch pipelines and for cases when job
     */
    private String getEscapedDisplayName() {
        String displayName = getDisplayName();
        // escape special characters so the summary is not formatted
        // when the build name contains special characters
        // https://www.markdownguide.org/basic-syntax#characters-you-can-escape
        return displayName.replaceAll("([*_#-])", "\\\\$1");
    }

    /**
     * Returns name of the project.
     */
    private String getDisplayName() {
        return run.getParent().getFullDisplayName();
    }

    private String getRunName() {
        // TODO: test case when the build number is changed to custom name
        return run.hasCustomDisplayName() ? run.getDisplayName() : "#" + run.getNumber();
    }
}
