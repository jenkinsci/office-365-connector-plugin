package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.Section;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import jenkins.plugins.office365connector.workflow.StepParameters;
import mockit.internal.reflection.MethodReflection;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class CardBuilderMessageCardTest extends AbstractTest {

    private static final String JOB_DISPLAY_NAME = "myJobDisplayName";
    private static final int BUILD_NUMBER = 7;

    private CardBuilder cardBuilder;

    @Before
    public void setUp() throws Exception {
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
    public void createStartedCard_ReturnsCard() {

        // given
        // from @Before

        // when
        Card card = cardBuilder.createStartedCard(Collections.emptyList());

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo("3479BF");
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Notification from " + JOB_DISPLAY_NAME + ": Started");
    }

    @Test
    public void createCompletedCard_OnAborted_ReturnsCard() {

        // given
        String status = "Aborted";
        Result result = Result.fromString(status);
        when(run.getResult()).thenReturn(result);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER + " " + status);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(result.color.getHtmlBaseColor());
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Notification from " + JOB_DISPLAY_NAME + ": Build Aborted");
    }

    @Test
    public void createCompletedCard_OnFirstFailure_ReturnsCard() {

        // given
        Result result = Result.FAILURE;
        when(run.getResult()).thenReturn(result);

        // when
        Card card = cardBuilder.createCompletedCard(Collections.emptyList());

        // then
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(result.color.getHtmlBaseColor());
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Notification from " + JOB_DISPLAY_NAME + ": Build Failed");
    }

    @Test
    public void createCompletedCard_OnSecondFailure_AddsFailingSinceFact() {

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
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(result.color.getHtmlBaseColor());
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Notification from " + JOB_DISPLAY_NAME + ": Repeated Failure");
        FactAssertion.assertThatLast(section.getFacts(), 2)
                .hasName(FactsBuilder.NAME_FAILING_SINCE_BUILD)
                .hasValue("build #" + previousNotFailedBuildNumber);
    }

    @Test
    public void createCompletedCard_OnFirstFailure_SkipsFailingSinceFact() {

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
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(result.color.getHtmlBaseColor());
        Section section = card.getSections().get(0);
        assertThat(section.getActivityTitle()).isEqualTo("Notification from " + JOB_DISPLAY_NAME + ": Build Failed");
        FactAssertion.assertThatLast(section.getFacts(), 1);
        FactAssertion.assertThat(section.getFacts())
                .hasName(FactsBuilder.NAME_STATUS)
                .hasValue("Build Failed");
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


    @Test
    public void getCompletedResult_ReturnsSuccess() throws Throwable {

        // given
        final Result result = Result.UNSTABLE;
        Run run = mock(Run.class);
        when(run.getResult()).thenReturn(result);

        // when
        Result completedResult = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCompletedResult", new Class[]{Run.class}, run);

        // then
        assertThat(completedResult).isEqualTo(result);
    }

    @Test
    public void getCompletedResult_OnNullRun_ReturnsSuccess() throws Throwable {

        // given
        Run run = mock(Run.class);

        // when
        Result completedResult = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCompletedResult", new Class[]{Run.class}, run);

        // then
        assertThat(completedResult).isEqualTo(Result.SUCCESS);
    }


    @Test
    public void createBuildMessageCard_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = Result.SUCCESS.toString();
        String color = "blue";

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(color);
        FactAssertion.assertThat(card.getSections().get(0).getFacts())
                .hasName(FactsBuilder.NAME_STATUS).hasValue(status);
    }

    @Test
    public void createBuildMessageCard_OnMissingStatus_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = null;
        String color = "blue";

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo(color);
        assertThat(card.getSections().get(0).getFacts()).isEmpty();
    }

    @Test
    public void createBuildMessageCard_OnMissingColor_ReturnsCard() {

        // given
        String message = "myMessage";
        String webhookUrl = "myHookUrl";
        String status = Result.ABORTED.toString();
        String color = null;

        StepParameters stepParameters = new StepParameters(message, webhookUrl, status, Collections.emptyList(), color, false);

        // then
        Card card = cardBuilder.createBuildMessageCard(stepParameters);

        // then
        assertThat(card.getSummary()).isEqualTo(JOB_DISPLAY_NAME + ": Build #" + BUILD_NUMBER);
        assertThat(card.getSections()).hasSize(1);
        assertThat(card.getThemeColor()).isEqualTo("3479BF");
        FactAssertion.assertThat(card.getSections().get(0).getFacts())
                .hasName(FactsBuilder.NAME_STATUS).hasValue(status);
    }

    @Test
    public void getEscapedDisplayName_OnNameWithSpecialCharacters_EscapesSpecialCharacters() throws Throwable {

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
        String displayName = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getEscapedDisplayName", new Class[]{});

        // then
        assertThat(displayName).isEqualTo("this\\_is\\_my\\-very\\#special \\*job\\*");
    }

    @Test
    public void getCardThemeColor_OnSuccessResult_ReturnsGreen() throws Throwable {
        // given
        Result successResult = Result.SUCCESS;
        String greenColorString = "#00FF00";

        // when
        String themeColor = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCardThemeColor", new Class[]{Result.class}, successResult);

        // then
        assertThat(themeColor).isEqualToIgnoringCase(greenColorString);
    }

    @Test
    public void getCardThemeColor_OnAbortedResult_ReturnsBallColor() throws Throwable {
        // given
        Result abortedResult = Result.ABORTED;
        String ballColorString = Result.ABORTED.color.getHtmlBaseColor();

        // when
        String themeColor = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCardThemeColor", new Class[]{Result.class}, abortedResult);

        // then
        assertThat(themeColor).isEqualToIgnoringCase(ballColorString);
    }

    @Test
    public void getCardThemeColor_OnFailureResult_ReturnsBallColor() throws Throwable {
        // given
        Result failureResult = Result.FAILURE;
        String ballColorString = Result.FAILURE.color.getHtmlBaseColor();

        // when
        String themeColor = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCardThemeColor", new Class[]{Result.class}, failureResult);

        // then
        assertThat(themeColor).isEqualToIgnoringCase(ballColorString);
    }

    @Test
    public void getCardThemeColor_OnNotBuiltResult_ReturnsBallColor() throws Throwable {
        // given
        Result notBuiltResult = Result.NOT_BUILT;
        String ballColorString = Result.NOT_BUILT.color.getHtmlBaseColor();

        // when
        String themeColor = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCardThemeColor", new Class[]{Result.class}, notBuiltResult);

        // then
        assertThat(themeColor).isEqualToIgnoringCase(ballColorString);
    }

    @Test
    public void getCardThemeColor_OnUnstableResult_ReturnsBallColor() throws Throwable {
        // given
        Result unstableResult = Result.UNSTABLE;
        String ballColorString = Result.UNSTABLE.color.getHtmlBaseColor();

        // when
        String themeColor = MethodReflection.invokeWithCheckedThrows(cardBuilder.getClass(), cardBuilder, "getCardThemeColor", new Class[]{Result.class}, unstableResult);

        // then
        assertThat(themeColor).isEqualToIgnoringCase(ballColorString);
    }
}
