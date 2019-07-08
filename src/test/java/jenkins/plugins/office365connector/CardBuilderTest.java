package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CardBuilder.class)
public class CardBuilderTest extends AbstractTest {

    private static final String JOB_DISPLAY_NAME = "myJobDisplayName";
    private static final int BUILD_NUMBER = 7;

    private CardBuilder cardBuilder;

    @Before
    public void setUp() throws Exception {
        ItemGroup itemGroup = mock(ItemGroup.class);
        when(itemGroup.getFullDisplayName()).thenReturn(StringUtils.EMPTY);

        Job job = mock(Job.class);
        when(job.getDisplayName()).thenReturn(JOB_DISPLAY_NAME);
        when(job.getParent()).thenReturn(itemGroup);

        run = mock(AbstractBuild.class);
        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        when(run.getParent()).thenReturn(job);

        mockDisplayURLProvider(JOB_DISPLAY_NAME, BUILD_NUMBER);

        ActionableBuilder mockActionableBuilder = mock(ActionableBuilder.class);
        whenNew(ActionableBuilder.class).withAnyArguments().thenReturn(mockActionableBuilder);
        FactsBuilder mockFactsBuilder = mock(FactsBuilder.class);
        whenNew(FactsBuilder.class).withAnyArguments().thenReturn(mockFactsBuilder);
        cardBuilder = new CardBuilder(run, mockListener());
    }


    @Test
    public void createStartedCard_ReturnsCard() {

        // given
        // from @Before

        // when
        Card card = cardBuilder.createStartedCard();

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER + " Started");
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo("3479BF");
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Update from " + JOB_DISPLAY_NAME + ".");
    }

    @Test
    public void createCompletedCard_OnAborted_ReturnsCard() {

        // given
        String status = "Aborted";
        Result result = Result.fromString(status);
        when(run.getResult()).thenReturn(result);

        // when
        Card card = cardBuilder.createCompletedCard();

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER + " " + status);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(result.color.getHtmlBaseColor());
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Update from " + JOB_DISPLAY_NAME + ".");
    }

    @Test
    public void calculateStatus_OnSuccess_ReturnsBackToNormal() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.FAILURE, Result.UNSTABLE};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status).isEqualTo("Back to Normal");
        }
    }

    @Test
    public void calculateStatus_OnSuccess_ReturnsSuccess() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.SUCCESS, Result.NOT_BUILT, Result.ABORTED};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status).isEqualTo("Build Success");
        }
    }

    @Test
    public void calculateStatus_OnFailure_ReturnsBuildFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = false;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Build Failed");
    }

    @Test
    public void calculateStatus_OnSuccessWithRepeatedFailure_ReturnsRepeatedFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Repeated Failure");
    }

    @Test
    public void calculateStatus_OnAborted_ReturnsAborted() {

        // given
        Result lastResult = Result.ABORTED;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Build Aborted");
    }

    @Test
    public void calculateStatus_OnUnstable_ReturnsUnstable() {

        // given
        Result lastResult = Result.UNSTABLE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Build Unstable");
    }

    @Test
    public void calculateStatus_OnUnsupportedResult_ReturnsResultName() {

        // given
        Result lastResult = Result.NOT_BUILT;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo(lastResult.toString());
    }


    @Test
    public void calculateSummary_OnSuccess_ReturnsBackToNormal() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.FAILURE, Result.UNSTABLE};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status).isEqualTo("Back to Normal");
        }
    }

    @Test
    public void calculateSummary_OnSuccess_ReturnsSuccess() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.SUCCESS, Result.NOT_BUILT, Result.ABORTED};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status).isEqualTo("Success");
        }
    }

    @Test
    public void calculateSummary_OnFailure_ReturnsBuildFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = false;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Failed");
    }

    @Test
    public void calculateSummary_OnSuccessWithRepeatedFailure_ReturnsRepeatedFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Repeated Failure");
    }


    @Test
    public void calculateSummary_OnAborted_ReturnsAborted() {

        // given
        Result lastResult = Result.ABORTED;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Aborted");
    }

    @Test
    public void calculateSummary_OnUnstable_ReturnsUnstable() {

        // given
        Result lastResult = Result.UNSTABLE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo("Unstable");
    }

    @Test
    public void calculateSummary_OnUnsupportedResult_ReturnsResultName() {

        // given
        Result lastResult = Result.NOT_BUILT;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo(lastResult.toString());
    }
}
