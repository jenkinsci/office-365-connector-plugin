package jenkins.plugins.office365connector;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardBuilderMessageCardTest extends AbstractTest {

    private static final String JOB_DISPLAY_NAME = "myJobDisplayName";
    private static final int BUILD_NUMBER = 7;

    private CardBuilder cardBuilder;

    @BeforeEach
    void setUp() {
        Jenkins jenkinsMock = mock(Jenkins.class);
        when(jenkinsMock.getFullDisplayName()).thenReturn(StringUtils.EMPTY);

        AbstractProject job = mock(AbstractProject.class);
        when(job.getDisplayName()).thenReturn(JOB_DISPLAY_NAME);
        when(job.getFullDisplayName()).thenCallRealMethod();
        when(job.getParent()).thenReturn(jenkinsMock);

        run = mock(AbstractBuild.class);
        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        when(run.getParent()).thenReturn(job);

        mockDisplayURLProvider(JOB_DISPLAY_NAME, BUILD_NUMBER);
        TaskListener taskListener = mock(TaskListener.class);
        cardBuilder = new CardBuilder(run, taskListener, false);
    }


    @Test
    void createStartedCard_ReturnsCard() {

        // given
        // from @Before

        // when
        Card card = cardBuilder.createStartedCard(Collections.emptyList());

        // then
        assertThat(card.getSummary(), equalTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER));
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo("3479BF"));
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle(), equalTo("Notification from " + JOB_DISPLAY_NAME + ": Started"));
    }

    @Test
    void createCompletedCard_OnAborted_ReturnsCard() {

        // given
        String status = "Aborted";
        Result result = Result.fromString(status);
        when(run.getResult()).thenReturn(result);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSummary(), equalTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER + " " + status));
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(result.color.getHtmlBaseColor()));
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle(), equalTo("Notification from " + JOB_DISPLAY_NAME + ": Build Aborted"));
    }

    @Test
    void createCompletedCard_OnFirstFailure_ReturnsCard() {

        // given
        Result result = Result.FAILURE;
        when(run.getResult()).thenReturn(result);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(result.color.getHtmlBaseColor()));
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle(), equalTo("Notification from " + JOB_DISPLAY_NAME + ": Build Failed"));
    }

    @Test
    void createCompletedCard_OnSecondFailure_AddsFailingSinceFact() {

        // given
        Result result = Result.FAILURE;
        when(run.getResult()).thenReturn(result);

        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);
        when(run.getPreviousBuild()).thenReturn(previousBuild);

        AbstractBuild previousNotFailedBuild = mock(AbstractBuild.class);
        int previousNotFailedBuildNumber = BUILD_NUMBER - 3;
        when(previousNotFailedBuild.getNumber()).thenReturn(previousNotFailedBuildNumber);
        when(previousNotFailedBuild.getNextBuild()).thenReturn(previousNotFailedBuild);
        when(run.getPreviousNotFailedBuild()).thenReturn(previousNotFailedBuild);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(result.color.getHtmlBaseColor()));
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle(), equalTo("Notification from " + JOB_DISPLAY_NAME + ": Repeated Failure"));
        FactAssertion.assertThatLast(section.getFacts(), 2)
                .hasName(FactsBuilder.NAME_FAILING_SINCE_BUILD)
                .hasValue("build #" + previousNotFailedBuildNumber);
    }

    @Test
    void createCompletedCard_OnFirstFailure_SkipsFailingSinceFact() {

        // given
        Result result = Result.FAILURE;
        when(run.getResult()).thenReturn(result);

        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(previousBuild.getResult()).thenReturn(Result.ABORTED);
        when(run.getPreviousBuild()).thenReturn(previousBuild);

        AbstractBuild previousNotFailedBuild = mock(AbstractBuild.class);
        int previousNotFailedBuildNumber = BUILD_NUMBER - 3;
        when(previousNotFailedBuild.getNumber()).thenReturn(previousNotFailedBuildNumber);
        when(previousNotFailedBuild.getNextBuild()).thenReturn(previousNotFailedBuild);
        when(run.getPreviousNotFailedBuild()).thenReturn(previousNotFailedBuild);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(result.color.getHtmlBaseColor()));
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle(), equalTo("Notification from " + JOB_DISPLAY_NAME + ": Build Failed"));
        FactAssertion.assertThatLast(section.getFacts(), 1);
        FactAssertion.assertThat(section.getFacts())
                .hasName(FactsBuilder.NAME_STATUS)
                .hasValue("Build Failed");
    }


    @Test
    void calculateStatus_OnSuccess_ReturnsBackToNormal() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.FAILURE, Result.UNSTABLE};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status, equalTo("Back to Normal"));
        }
    }

    @Test
    void calculateStatus_OnSuccess_ReturnsSuccess() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.SUCCESS, Result.NOT_BUILT, Result.ABORTED};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status, equalTo("Build Success"));
        }
    }

    @Test
    void calculateStatus_OnFailure_ReturnsBuildFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = false;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Build Failed"));
    }

    @Test
    void calculateStatus_OnSuccessWithRepeatedFailure_ReturnsRepeatedFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Repeated Failure"));
    }

    @Test
    void calculateStatus_OnAborted_ReturnsAborted() {

        // given
        Result lastResult = Result.ABORTED;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Build Aborted"));
    }

    @Test
    void calculateStatus_OnUnstable_ReturnsUnstable() {

        // given
        Result lastResult = Result.UNSTABLE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Build Unstable"));
    }

    @Test
    void calculateStatus_OnUnsupportedResult_ReturnsResultName() {

        // given
        Result lastResult = Result.NOT_BUILT;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateStatus(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo(lastResult.toString()));
    }


    @Test
    void calculateSummary_OnSuccess_ReturnsBackToNormal() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.FAILURE, Result.UNSTABLE};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status, equalTo("Back to Normal"));
        }
    }

    @Test
    void calculateSummary_OnSuccess_ReturnsSuccess() {

        // given
        Result lastResult = Result.SUCCESS;
        boolean isRepeatedFailure = false;
        Result[] previousResults = {Result.SUCCESS, Result.NOT_BUILT, Result.ABORTED};

        for (Result previousResult : previousResults) {
            // when
            String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

            // then
            assertThat(status, equalTo("Success"));
        }
    }

    @Test
    void calculateSummary_OnFailure_ReturnsBuildFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = false;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Failed"));
    }

    @Test
    void calculateSummary_OnSuccessWithRepeatedFailure_ReturnsRepeatedFailure() {

        // given
        Result lastResult = Result.FAILURE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Repeated Failure"));
    }

    @Test
    void calculateSummary_OnAborted_ReturnsAborted() {

        // given
        Result lastResult = Result.ABORTED;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Aborted"));
    }

    @Test
    void calculateSummary_OnUnstable_ReturnsUnstable() {

        // given
        Result lastResult = Result.UNSTABLE;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo("Unstable"));
    }

    @Test
    void calculateSummary_OnUnsupportedResult_ReturnsResultName() {

        // given
        Result lastResult = Result.NOT_BUILT;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status, equalTo(lastResult.toString()));
    }


    @Test
    void getCompletedResult_ReturnsSuccess() {

        // given
        final Result result = Result.UNSTABLE;
        Run run = mock(Run.class);
        when(run.getResult()).thenReturn(result);

        // when
        Result completedResult = ReflectionHelper.invokeMethod(cardBuilder,"getCompletedResult", run);

        // then
        assertThat(completedResult, equalTo(result));
    }

    @Test
    void getCompletedResult_OnNullRun_ReturnsSuccess() {

        // given
        Run run = mock(Run.class);

        // when
        Result completedResult = ReflectionHelper.invokeMethod(cardBuilder,"getCompletedResult", run);

        // then
        assertThat(completedResult, equalTo(Result.SUCCESS));
    }


    @Test
    void createBuildMessageCard_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = Result.SUCCESS.toString();
        String color = "blue";

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary(), equalTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER));
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(color));
        FactAssertion.assertThat(card.getSections().get(0).getFacts())
                .hasName(FactsBuilder.NAME_STATUS).hasValue(status);
    }

    @Test
    void createBuildMessageCard_OnMissingStatus_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = null;
        String color = "blue";

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary(), equalTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER));
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo(color));
        assertThat(card.getSections().get(0).getFacts(), empty());
    }

    @Test
    void createBuildMessageCard_OnMissingColor_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = Result.ABORTED.toString();
        String color = null;

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary(), equalTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER));
        assertThat(card.getSections(), hasSize(1));
        assertThat(card.getThemeColor(), equalTo("3479BF"));
        FactAssertion.assertThat(card.getSections().get(0).getFacts())
                .hasName(FactsBuilder.NAME_STATUS).hasValue(status);
    }

    @Test
    void getEscapedDisplayName_OnNameWithSpecialCharacters_EscapesSpecialCharacters() {

        // given
        final String specialDisplayName = "this_is_my-very#special *job*";
        Jenkins jenkinsMock = mock(Jenkins.class);
        when(jenkinsMock.getFullDisplayName()).thenReturn(StringUtils.EMPTY);

        AbstractProject job = mock(AbstractProject.class);
        when(job.getDisplayName()).thenReturn(specialDisplayName);
        when(job.getFullDisplayName()).thenCallRealMethod();
        when(job.getParent()).thenReturn(jenkinsMock);

        run = mock(AbstractBuild.class);
        when(run.getParent()).thenReturn(job);
        TaskListener taskListener = mock(TaskListener.class);

        cardBuilder = new CardBuilder(run, taskListener, false);

        // when
        String displayName = ReflectionHelper.invokeMethod(cardBuilder,"getEscapedDisplayName");

        // then
        assertThat(displayName, equalTo("this\\_is\\_my\\-very\\#special \\*job\\*"));
    }

    @Test
    void getCardThemeColor_OnSuccessResult_ReturnsGreen() {
        // given
        Result successResult = Result.SUCCESS;
        String greenColorString = "#00FF00";

        // when
        String themeColor = ReflectionHelper.invokeMethod(cardBuilder,"getCardThemeColor", successResult);

        // then
        assertThat(themeColor, equalToIgnoringCase(greenColorString));
    }

    @Test
    void getCardThemeColor_OnAbortedResult_ReturnsBallColor() {
        // given
        Result abortedResult = Result.ABORTED;
        String ballColorString = Result.ABORTED.color.getHtmlBaseColor();

        // when
        String themeColor = ReflectionHelper.invokeMethod(cardBuilder,"getCardThemeColor", abortedResult);

        // then
        assertThat(themeColor, equalToIgnoringCase(ballColorString));
    }

    @Test
    void getCardThemeColor_OnFailureResult_ReturnsBallColor() {
        // given
        Result failureResult = Result.FAILURE;
        String ballColorString = Result.FAILURE.color.getHtmlBaseColor();

        // when
        String themeColor = ReflectionHelper.invokeMethod(cardBuilder,"getCardThemeColor", failureResult);

        // then
        assertThat(themeColor, equalToIgnoringCase(ballColorString));
    }

    @Test
    void getCardThemeColor_OnNotBuiltResult_ReturnsBallColor() {
        // given
        Result notBuiltResult = Result.NOT_BUILT;
        String ballColorString = Result.NOT_BUILT.color.getHtmlBaseColor();

        // when
        String themeColor = ReflectionHelper.invokeMethod(cardBuilder,"getCardThemeColor", notBuiltResult);

        // then
        assertThat(themeColor, equalToIgnoringCase(ballColorString));
    }

    @Test
    void getCardThemeColor_OnUnstableResult_ReturnsBallColor() {
        // given
        Result unstableResult = Result.UNSTABLE;
        String ballColorString = Result.UNSTABLE.color.getHtmlBaseColor();

        // when
        String themeColor = ReflectionHelper.invokeMethod(cardBuilder,"getCardThemeColor", unstableResult);

        // then
        assertThat(themeColor, equalToIgnoringCase(ballColorString));
    }
}
