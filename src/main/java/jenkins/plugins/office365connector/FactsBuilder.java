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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    final static String NAME_TESTS = "Tests";
    final static String NAME_CHANGES = "Changes";


    private final List<Fact> facts = new ArrayList<>();
    private final List<Fact> comapactFacts = new ArrayList<>();
    private final Run run;

    public FactsBuilder(Run run) {
        this.run = run;
    }

    public void addTestsCompact() {
        
        AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
        if (action == null) {
            return;
        }
        
        int failedTests = action.getFailCount();
        int skippedTests = action.getSkipCount();
        int passedTests = action.getTotalCount() - action.getFailCount() - action.getSkipCount();

        StringBuilder testStatus = new StringBuilder();
        if (action.getTotalCount() == 0)
            testStatus.append("No tests found");
        else 
        {
            testStatus.append("Passed: ");
            testStatus.append(passedTests);
            testStatus.append(", Failed: ");
            testStatus.append(failedTests);
            testStatus.append(", Skipped: ");
            testStatus.append(skippedTests);            
        }
        
        comapactFacts.add(new Fact(NAME_TESTS, testStatus.toString()));
    }

    public void addChanges(String changes) {
        comapactFacts.add(new Fact(NAME_CHANGES, changes));
    }


    public void addStatusStarted() {
        facts.add(new Fact(NAME_STATUS, "Started"));
    }

    public void addStatusRunning() {
        facts.add(new Fact(NAME_STATUS, "Running"));
    }

    public static Fact buildStatus() {
        return new Fact(NAME_STATUS);
    }

    public void addStartTime() {
        facts.add(new Fact(NAME_START_TIME, TimeUtils.dateToString(run.getStartTimeInMillis())));
    }

    public String getBuildDuration() {
        return TimeUtils.durationToString(getRunDuration() / 1000);
    }

    public void addBackToNormalTime(long duration) {
        facts.add(new Fact(NAME_BACK_TO_NORMAL_TIME, TimeUtils.dateToString(duration)));
    }

    public void addCompletionTime() {
        facts.add(new Fact(NAME_COMPLETION_TIME, TimeUtils.dateToString(countCompletionTime())));
    }

    private long getRunDuration()
    {
        long duration;
        if (run.getDuration() == 0L)
            duration = System.currentTimeMillis() - run.getStartTimeInMillis();
        else 
            duration = run.getDuration();
        return duration;
    }
    
    private long countCompletionTime() 
    {        
        return run.getStartTimeInMillis() + getRunDuration();
    }

    public void addFailingSinceTime(long duration) {
        facts.add(new Fact(NAME_FAILING_SINCE_TIME, TimeUtils.dateToString(duration)));
    }

    public void addFailingSinceBuild(int buildNumber) {
        facts.add(new Fact(NAME_FAILING_SINCE_BUILD, buildNumber));
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
        facts.add(new Fact(NAME_REMARKS, causesStr.toString()));
    }

    public void addCulprits(Set<User> authors) {
        if (CollectionUtils.isEmpty(authors)) {
            return;
        }

        Set<String> culprits = new HashSet<>();
        for (User user : authors) {
            culprits.add(user.getFullName());
        }
        if (!culprits.isEmpty()) {
            facts.add(new Fact(NAME_CULPRITS, StringUtils.join(culprits, ", ")));
        }
    }

    public void addDevelopers(Set<User> authors) {
        if (CollectionUtils.isEmpty(authors)) {
            return;
        }
        facts.add(new Fact(NAME_DEVELOPERS, StringUtils.join(authors, ", ")));
    }

    public void addNumberOfFilesChanged(int files) {
        if (files == 0) {
            return;
        }
        facts.add(new Fact(NAME_NUMBER_OF_CHANGED_FILES, files));
    }

    public void addTests() {
        AbstractTestResultAction<?> action = run.getAction(AbstractTestResultAction.class);
        if (action == null) {
            return;
        }

        facts.add(new Fact(NAME_TOTAL_TESTS, action.getTotalCount()));
        facts.add(new Fact(NAME_PASSED_TESTS, action.getTotalCount() - action.getFailCount() - action.getSkipCount()));
        facts.add(new Fact(NAME_FAILED_TESTS, action.getFailCount()));
        facts.add(new Fact(NAME_SKIPPED_TESTS, action.getSkipCount()));
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

    public List<Fact> collectCompact() {
        return comapactFacts;
    }
}
