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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.utils.TimeUtils;
import jenkins.plugins.office365connector.workflow.StepParameters;
import jenkins.scm.RunWithSCM;

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

    public Card createJobStartedCard() {
        factsBuilder.addStatusStarted();
        factsBuilder.addStartTime();
        factsBuilder.addRemarks();
        addScmDetails();

        String jobName = getDisplayName();
        String activityTitle = "Update from " + jobName + ".";
        String activitySubtitle = "Latest status of build " + getRunName();
        Section section = new Section(activityTitle, activitySubtitle, factsBuilder.collect());

        String summary = jobName + ": Build " + getRunName() + " Started";
        Card card = new Card(summary, section);
        card.setPotentialAction(potentialActionBuilder.buildActionable());

        return card;
    }

    public Card createJobCompletedCard() {
        String jobName = getDisplayName();
        String summary = String.format("%s: Build %s ", jobName, getRunName());

        Fact statusFact = FactsBuilder.buildStatus();
        factsBuilder.addFact(statusFact);
        factsBuilder.addStartTime();

        // Result is only set to a worse status in pipeline
        Result result = run.getResult() == null ? Result.SUCCESS : run.getResult();
        if (result != null) {

            factsBuilder.addCompletionTime();
            factsBuilder.addTests();

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

            if (result == Result.SUCCESS) {
                // back to normal
                if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                    status = "Back to Normal";
                    summary += " Back to Normal";

                    if (failingSinceRun != null) {
                        long currentBuildCompletionTime = TimeUtils.countCompletionTime(run.getStartTimeInMillis(), run.getDuration());
                        factsBuilder.addBackToNormalTime(currentBuildCompletionTime - failingSinceRun.getStartTimeInMillis());
                    }
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
                    factsBuilder.addFailingSinceTime(failingSinceRun.getStartTimeInMillis() + failingSinceRun.getDuration());
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
                log("Unsupported result: " + result);
                status = result.toString();
                summary += status;
            }

            statusFact.setValue(status);
        } else {
            statusFact.setValue(" Completed");
            summary += "Completed";
        }

        factsBuilder.addRemarks();
        addScmDetails();

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

    public Card createBuildMessageCard(StepParameters stepParameters) {
        String jobName = getDisplayName();
        if (stepParameters.getStatus() != null) {
            Fact fact = FactsBuilder.buildStatus();
            fact.setValue(stepParameters.getStatus());
            factsBuilder.addFact(fact);
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

    private void addScmDetails() {
        if (!(run instanceof RunWithSCM)) {
            return;
        }
        RunWithSCM runWithSCM = (RunWithSCM) run;
        factsBuilder.addCulprits(runWithSCM.getCulprits());

        List<ChangeLogSet<ChangeLogSet.Entry>> sets = runWithSCM.getChangeSets();
        if (sets.isEmpty()) {
            return;
        }
        Set<User> authors = new HashSet<>();
        int filesCounter = 0;
        if (Iterables.all(sets, Predicates.instanceOf(ChangeLogSet.class))) {
            for (ChangeLogSet<ChangeLogSet.Entry> set : sets) {
                for (ChangeLogSet.Entry entry : set) {
                    authors.add(entry.getAuthor());
                    filesCounter += countAffectedFiles(entry);
                }
            }
        }
        factsBuilder.addDevelopers(authors);
        factsBuilder.addNumberOfFilesChanged(filesCounter);
    }

    private int countAffectedFiles(ChangeLogSet.Entry entry) {
        try {
            return entry.getAffectedFiles().size();
        } catch (UnsupportedOperationException e) {
            // countAffectedFiles() is not implemented by this scm
            log(e.getMessage());
            return 0;
        }
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
