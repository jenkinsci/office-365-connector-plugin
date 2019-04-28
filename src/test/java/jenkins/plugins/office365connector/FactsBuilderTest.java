package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.model.AbstractBuild;
import hudson.model.User;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.utils.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TimeUtils.class})
public class FactsBuilderTest {

    private AbstractBuild run;

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);
    }

    @Test
    public void addStatusStarted_AddsFact() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);

        // when
        factBuilder.addStatusStarted();

        // then
        FactAssertion.assertThat(factBuilder.collect())
                .hasName(FactsBuilder.NAME_STATUS)
                .hasValue(FactsBuilder.VALUE_STATUS_STARTED);
    }

    @Test
    public void addStatusRunning_AddsFact() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);

        // when
        factBuilder.addStatusRunning();

        // then
        FactAssertion.assertThat(factBuilder.collect())
                .hasName(FactsBuilder.NAME_STATUS)
                .hasValue(FactsBuilder.VALUE_STATUS_RUNNING);
    }


    @Test
    public void addFailingSinceBuild_AddsFact() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);
        int buildNumber = 123;

        // when
        factBuilder.addFailingSinceBuild(buildNumber);

        // then
        FactAssertion.assertThat(factBuilder.collect())
                .hasName(FactsBuilder.NAME_FAILING_SINCE_BUILD)
                .hasValue("#" + buildNumber);
    }

    @Test
    public void addCulprits_AddsFact() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);
        User one = createUser("damian");
        User two = createUser("365");
        Set<User> users = new HashSet<>();
        users.add(one);
        users.add(two);
        when(run.getCulprits()).thenReturn(users);

        // when
        factBuilder.addCulprits();

        // then
        List<Fact> facts = factBuilder.collect();
        assertThat(facts).hasSize(1);

        Fact fact = facts.get(0);
        assertThat(fact.getName()).isEqualTo(FactsBuilder.CULPRITS);
        assertThat(fact.getValue())
                .hasSize(one.getFullName().length() + two.getFullName().length() + 2)
                // depends on JVM implementation 'one' could be listed on the first or last position
                .contains(one.getFullName())
                .contains(two.getFullName());
    }

    @Test
    public void addCulprits_OnNoUser_AddsNoFact() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);

        // when
        factBuilder.addCulprits();

        // then
        assertThat(factBuilder.collect()).isEmpty();
    }

    @Test
    public void addFact_AddStatusAtTheFirstPosition() {

        // given
        FactsBuilder factBuilder = new FactsBuilder(run);
        factBuilder.addFact("some name", "some value");

        // when
        factBuilder.addStatus("Ahoy");

        // then
        assertThat(factBuilder.collect()).hasSize(2);
        assertThat(factBuilder.collect().get(0).getName()).isEqualTo(FactsBuilder.NAME_STATUS);
    }

    private static User createUser(String fullName) {
        User user = mock(User.class);
        when(user.getFullName()).thenReturn(fullName);
        return user;
    }
}
