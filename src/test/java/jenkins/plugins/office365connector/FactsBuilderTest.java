package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.Run;
import jenkins.plugins.office365connector.model.Fact;
import jenkins.plugins.office365connector.utils.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Test {@link FactsBuilder} methods.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TimeUtils.class})
public class FactsBuilderTest {

    private Run run;

    @Before
    public void setUp() {
        run = mock(Run.class);
    }

    @Test
    public void addBackToNormalTime() {
        long backToNormalDuration = 1000L;
        String durationString = "16 minutes, 40 seconds";

        PowerMockito.mockStatic(TimeUtils.class);
        BDDMockito.given(TimeUtils.durationToString(backToNormalDuration)).willReturn(durationString);

        FactsBuilder factBuilder = new FactsBuilder(run);
        factBuilder.addBackToNormalTime(backToNormalDuration);

        assertThat(factBuilder.collect()).hasSize(1);

        Fact actualFact = factBuilder.collect().get(0);
        assertThat(actualFact.getName()).isEqualTo(FactsBuilder.NAME_BACK_TO_NORMAL_TIME);
        assertThat(actualFact.getValue()).isEqualTo(durationString);
    }
}