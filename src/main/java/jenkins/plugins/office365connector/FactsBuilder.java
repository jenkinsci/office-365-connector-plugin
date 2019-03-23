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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.User;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.utils.TimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Collects helper methods that create instance of {@link jenkins.plugins.office365connector.model.Fact Fact} class.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactsBuilder {

    final static String NAME_STATUS = "Status";
    final static String NAME_REMARKS = "Remarks";
    final static String NAME_CULPRITS = "Culprits";
    final static String NAME_DEVELOPERS = "Developers";
    final static String NAME_NUMBER_OF_CHANGED_FILES = "Number of files changed";

    final static String NAME_START_TIME = "Start time";
    final static String NAME_COMPLETION_TIME = "Completion time";
    final static String NAME_BACK_TO_NORMAL_TIME = "Back to normal time";
    final static String NAME_FAILING_SINCE_TIME = "Failing since time";
    final static String NAME_FAILING_SINCE_BUILD = "Failing since build";

    final static String NAME_TOTAL_TESTS = "Total tests";
    final static String NAME_FAILED_TESTS = "Failed tests";
    final static String NAME_SKIPPED_TESTS = "Skipped tests";
    final static String NAME_PASSED_TESTS = "Passed tests";

    private final List<Fact> facts = new ArrayList<>();
    private final Run run;

    public FactsBuilder(Run run) {
        this.run = run;
    }

    public void addStatusStarted() {
        addFact(NAME_STATUS, "Started");
    }

    public void addStatusRunning() {
        addFact(NAME_STATUS, "Running");
    }

    public static Fact buildStatus() {
        return new Fact(NAME_STATUS);
    }

    public void addStartTime() {
        addFact(NAME_START_TIME, TimeUtils.dateToString(run.getStartTimeInMillis()));
    }

    public void addBackToNormalTime(long duration) {
        addFact(NAME_BACK_TO_NORMAL_TIME, TimeUtils.durationToString(duration));
    }

    public void addCompletionTime() {
        long completionTime = TimeUtils.countCompletionTime(run.getStartTimeInMillis(), run.getDuration());
        addFact(NAME_COMPLETION_TIME, TimeUtils.dateToString(completionTime));
    }

    public void addFailingSinceTime(long duration) {
        addFact(NAME_FAILING_SINCE_TIME, TimeUtils.dateToString(duration));
    }

    public void addFailingSinceBuild(int buildNumber) {
        addFact(NAME_FAILING_SINCE_BUILD, String.valueOf(buildNumber));
    }

    public void addRemarks() {
        List<Cause> causes = run.getCauses();
        if (CollectionUtils.isEmpty(causes)) {
            return;
        }

        StringBuilder causesStr = new StringBuilder();
        for (Cause cause : causes) {
            causesStr.append(cause.getShortDescription()).append(". ");
        }
        addFact(NAME_REMARKS, causesStr.toString());
    }

    public void addCulprits(Set<User> authors) {
        if (CollectionUtils.isEmpty(authors)) {
            return;
        }

        List<String> culprits = authors.stream().map(User::getFullName).collect(Collectors.toList());
        if (!culprits.isEmpty()) {
            addFact(NAME_CULPRITS, StringUtils.join(culprits, ", "));
        }
    }

    public void addDevelopers(Set<User> authors) {
        if (CollectionUtils.isEmpty(authors)) {
            return;
        }
        addFact(NAME_DEVELOPERS, StringUtils.join(authors, ", "));
    }

    public void addNumberOfFilesChanged(int files) {
        if (files == 0) {
            return;
        }
        addFact(NAME_NUMBER_OF_CHANGED_FILES, files);
    }

    public void addTests() {
        AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
        if (action == null) {
            return;
        }

        addFact(NAME_TOTAL_TESTS, action.getTotalCount());
        addFact(NAME_PASSED_TESTS, action.getTotalCount() - action.getFailCount() - action.getSkipCount());
        addFact(NAME_FAILED_TESTS, action.getFailCount());
        addFact(NAME_SKIPPED_TESTS, action.getSkipCount());
    }

    public void addFact(String name, int value) {
        if (value != 0) {
            addFact(name, String.valueOf(value));
        }
    }

    public void addFact(String name, String value) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
            facts.add(new Fact(name, value));
        }
    }

    public void addFact(Fact fact) {
        facts.add(fact);
    }

    /**
     * Returns collected facts.
     *
     * @return collected facts
     */
    public List<Fact> collect() {
        return facts;
    }
}
