package jenkins.plugins.office365connector;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.model.Macro;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DecisionMakerTest extends AbstractTest {

    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setup() {
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("test");

        Jenkins mockJenkins = mock(Jenkins.class);
        staticJenkins = mockStatic(Jenkins.class);
        staticJenkins.when(Jenkins::get).thenReturn(mockJenkins);
        when(mockJenkins.getDescriptorOrDie(any())).thenReturn(mockDescriptor);
    }

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    @Test
    void DecisionMaker_OnEmptyPreviousBuild_StoresParameters() {

        // given
        AbstractBuild run = mock(AbstractBuild.class);
        TaskListener taskListener = mockListener();

        // when
        DecisionMaker decisionMaker = new DecisionMaker(run, taskListener);

        // then
        assertThat(ReflectionHelper.getField(decisionMaker,"run"), sameInstance(run));
        assertThat(ReflectionHelper.getField(decisionMaker, "taskListener"), sameInstance(taskListener));
        assertThat(ReflectionHelper.getField(decisionMaker, "previousResult"), equalTo(Result.SUCCESS));
    }

    @Test
    void DecisionMaker_OnPreviousBuild_StoresParameters() {

        // given
        AbstractBuild run = mock(AbstractBuild.class);
        AbstractBuild previousRun = mock(AbstractBuild.class);
        Result previousResult = Result.ABORTED;
        when(run.getPreviousBuild()).thenReturn(previousRun);
        when(previousRun.getResult()).thenReturn(previousResult);
        TaskListener taskListener = AbstractTest.mockListener();

        // when
        DecisionMaker decisionMaker = new DecisionMaker(run, taskListener);

        // then
        assertThat(ReflectionHelper.getField(decisionMaker,"run"), sameInstance(run));
        assertThat(ReflectionHelper.getField(decisionMaker, "taskListener"), sameInstance(taskListener));
        assertThat(ReflectionHelper.getField(decisionMaker, "previousResult"), equalTo(previousResult));
    }

    @Test
    void isAtLeastOneRuleMatched_OnEmptyMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched, is(true));
    }

    @Test
    void isAtLeastOneRuleMatched_OnMatchedMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        String template = "one";
        String value = template;
        Macro macro = new Macro(template, value);
        webhook.setMacros(List.of(macro));

        mockTokenMacro(value);

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched, is(true));
    }

    @Test
    void isAtLeastOneRuleMatched_OnMismatchedMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        String template = "one";
        String value = "two";
        Macro macro = new Macro(template, value);
        webhook.setMacros(List.of(macro));

        mockTokenMacro(template);

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyAndResultFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnNotifyFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultFailureAndPreviousFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultRepeatedFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnOnNotifyAndResultRepeatedFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyRepeatedFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultRepeatedFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultSuccess_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousUnstable_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousNotBuild_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousAborted_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultSuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifySuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultSuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnNotifyAndResultUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(true));
    }

    @Test
    void isStatusMatched_OnNotifyUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void isStatusMatched_OnResultUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }


    @Test
    void isStatusMatched_OnUndefinedResult_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched, is(false));
    }

    @Test
    void evaluateMacro_OnInvalidMacro_ThrowsException() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();

        try (MockedStatic<TokenMacro> tokenMacroStatic = mockStatic(TokenMacro.class); MockedStatic<FilePath> filePathStatic = mockStatic(FilePath.class)) {
            tokenMacroStatic.when(() -> TokenMacro.expandAll(any(), any(), any(), any())).thenThrow(new MacroEvaluationException("ups!"));

            // when & then
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> ReflectionHelper.invokeMethod(decisionMaker,"evaluateMacro", "anyTemplate"));
            assertThat(ex.getCause(), instanceOf(MacroEvaluationException.class));
        }
    }

    private static DecisionMaker buildSampleDecisionMaker() {
        return buildSampleDecisionMaker(null);
    }

    private static DecisionMaker buildSampleDecisionMaker(Result result) {
        return buildSampleDecisionMaker(result, null);
    }

    private static DecisionMaker buildSampleDecisionMaker(Result result, Result previousResult) {

        Run run = mock(Run.class);
        if (result != null) {
            when(run.getResult()).thenReturn(result);
        }
        if (previousResult != null) {
            Run previousRun = mock(Run.class);
            when(previousRun.getResult()).thenReturn(previousResult);
            when(run.getPreviousBuild()).thenReturn(previousRun);
        }
        File workspace = mock(File.class);
        when(run.getRootDir()).thenReturn(workspace);

        TaskListener taskListener = AbstractTest.mockListener();

        return new DecisionMaker(run, taskListener);
    }

}
