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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.FilePath;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.scm.RunWithSCM;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import jenkins.plugins.office365connector.utils.TeamsMentionUtils;

/**
 * Collects helper methods that create instance of {@link jenkins.plugins.office365connector.model.Fact Fact} class.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactsBuilder {

    public final static String NAME_STATUS = "Status";
    public final static String NAME_REMARKS = "Remarks";
    final static String COMMITTERS = "Committers";
    public final static String NAME_DEVELOPERS = "Developers";

    final static String NAME_FAILING_SINCE_BUILD = "Failing since";

    private final List<Fact> facts = new ArrayList<>();

    private final Run run;
    private final TaskListener taskListener;

    public FactsBuilder(Run run, TaskListener listener) {
        this.run = run;
        this.taskListener = listener;
    }

    public void addStatus(String status) {
        addFact(NAME_STATUS, status);
    }

    public void addFailingSinceBuild(int buildNumber) {
        addFact(NAME_FAILING_SINCE_BUILD, "build #" + buildNumber);
    }

    public void addRemarks() {
        List<Cause> causes = run.getCauses();

        String joinedCauses = causes.stream()
                // TODO: for single cause skip this dot as the separator
                .map(cause -> cause.getShortDescription().concat("."))
                .collect(Collectors.joining(" "));
        addFact(NAME_REMARKS, joinedCauses);
    }

    public void addCommitters(boolean mentionCommitters) {
        if (!(run instanceof RunWithSCM)) {
            return;
        }
        RunWithSCM runWithSCM = (RunWithSCM) run;
        Set<User> authors = runWithSCM.getCulprits();

        String joinedCommitters = authors.stream()
            .sorted(Comparator.comparing(User::getFullName))
            .map(user -> mentionCommitters ? TeamsMentionUtils.mentionUserOrEmail(user) : user.getFullName())
            .filter(StringUtils::isNotBlank) // remove nulls or empty strings
            .collect(Collectors.joining(", "));
        addFact(COMMITTERS, joinedCommitters);
    }

    // Overload to preserve old behavior
    public void addCommitters() {
        addCommitters(false);
    }

    public void addDevelopers() {
        if (!(run instanceof RunWithSCM)) {
            return;
        }
        RunWithSCM runWithSCM = (RunWithSCM) run;

        List<ChangeLogSet<ChangeLogSet.Entry>> sets = runWithSCM.getChangeSets();

        Set<User> authors = new HashSet<>();
        sets.stream()
                .filter(set -> set instanceof ChangeLogSet)
                .forEach(set -> set
                        .forEach(entry -> authors.add(entry.getAuthor())));

        addFact(NAME_DEVELOPERS, StringUtils.join(sortUsers(authors), ", "));
    }
    
    /**
     * Users should be stored in set to eliminate duplicates and sorted so the results
     * are presented same and deterministic way.
     */
    private List<User> sortUsers(Set<User> authors) {
        return authors.stream()
            .sorted(Comparator.comparing(User::getFullName))
            .collect(Collectors.toList());
    }


    public void addUserFacts(List<FactDefinition> factDefinitions) {
        if (factDefinitions != null && !factDefinitions.isEmpty()) {
            for (FactDefinition factDefinition : factDefinitions) {
                addFact(factDefinition.getName(), evaluateMacro(factDefinition.getTemplate()));
            }
        }
    }

    // ToDo: this code is redundant and should be merged with DecisionMaker#evaluateMacro
    private String evaluateMacro(String template) {
        try {
            File workspace = run.getRootDir();
            return TokenMacro.expandAll(run, new FilePath(workspace), taskListener, template);
        } catch (InterruptedException | IOException | MacroEvaluationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addFact(String name, String value) {
        addFact(new Fact(name, value));
    }

    private void addFact(Fact fact) {
        if (StringUtils.isBlank(fact.getName()) || StringUtils.isBlank(fact.getValue())) {
            return;
        }

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
