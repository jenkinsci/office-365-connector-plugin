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
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.scm.RunWithSCM;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Collects helper methods that create instance of {@link jenkins.plugins.office365connector.model.Fact Fact} class.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactsBuilder {

    public final static String NAME_STATUS = "Status";
    private final static String NAME_REMARKS = "Remarks";
    final static String CULPRITS = "Culprits";
    private final static String NAME_DEVELOPERS = "Developers";

    final static String NAME_FAILING_SINCE_BUILD = "Failing since build";

    final static String VALUE_STATUS_STARTED = "Started";
    final static String VALUE_STATUS_RUNNING = "Running";

    private final List<Fact> facts = new ArrayList<>();
    private final Run run;

    public FactsBuilder(Run run) {
        this.run = run;
    }

    public void addStatus(String status) {
        addFact(NAME_STATUS, status);
    }

    public void addStatusStarted() {
        addFact(NAME_STATUS, VALUE_STATUS_STARTED);
    }

    public void addStatusRunning() {
        addFact(NAME_STATUS, VALUE_STATUS_RUNNING);
    }

    public void addFailingSinceBuild(int buildNumber) {
        addFact(NAME_FAILING_SINCE_BUILD, "#" + String.valueOf(buildNumber));
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

    public void addCulprits() {
        if (!(run instanceof RunWithSCM)) {
            return;
        }
        RunWithSCM runWithSCM = (RunWithSCM) run;
        Set<User> authors = runWithSCM.getCulprits();
        if (CollectionUtils.isEmpty(authors)) {
            return;
        }

        List<String> culprits = authors.stream().map(User::getFullName).collect(Collectors.toList());
        if (!culprits.isEmpty()) {
            addFact(CULPRITS, StringUtils.join(culprits, ", "));
        }
    }

    public void addDevelopers() {
        if (!(run instanceof RunWithSCM)) {
            return;
        }
        RunWithSCM runWithSCM = (RunWithSCM) run;

        List<ChangeLogSet<ChangeLogSet.Entry>> sets = runWithSCM.getChangeSets();
        if (sets.isEmpty()) {
            return;
        }

        List<User> authors = new ArrayList<>();
        sets.stream()
                .filter(set -> set instanceof ChangeLogSet)
                .forEach(set -> set
                        .forEach(entry -> authors.add(entry.getAuthor())));

        if (CollectionUtils.isNotEmpty(authors)) {
            addFact(NAME_DEVELOPERS, StringUtils.join(authors, ", "));
        }
    }

    public void addFact(String name, String value) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
            return;
        }

        addFact(new Fact(name, value));
    }

    public void addFact(Fact fact) {
        // build status should be always at first position
        if (fact.getName().equals(NAME_STATUS)) {
            facts.add(0, fact);
        } else {
            facts.add(fact);
        }
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
