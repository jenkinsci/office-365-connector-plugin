package jenkins.plugins.office365connector;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.Macro;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import mockit.Deencapsulation;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TokenMacro.class, FilePath.class, Jenkins.class})
public class DecisionMakerTest extends AbstractTest {

    @Before
    public void setup() throws Exception {
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("test");

        Jenkins mockJenkins = mock(Jenkins.class);
        mockStatic(Jenkins.class);
        Mockito.when(Jenkins.getInstance()).thenReturn(mockJenkins);
        Mockito.when(mockJenkins.getDescriptorOrDie(anyObject())).thenReturn(mockDescriptor);
    }

    @Test
    public void DecisionMaker_OnEmptyPreviousBuild_StoresParameters() {

        // given
        Run run = mock(Run.class);
        TaskListener taskListener = mockListener();

        // when
        DecisionMaker decisionMaker = new DecisionMaker(run, taskListener);

        // then
        assertThat((Run) Deencapsulation.getField(decisionMaker, "run")).isSameAs(run);
        assertThat((TaskListener) Deencapsulation.getField(decisionMaker, "taskListener")).isSameAs(taskListener);
        assertThat((Result) Deencapsulation.getField(decisionMaker, "previousResult")).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void DecisionMaker_OnPreviousBuild_StoresParameters() {

        // given
        Run run = mock(Run.class);
        Run previousRun = mock(Run.class);
        Result previousResult = Result.ABORTED;
        when(run.getPreviousBuild()).thenReturn(previousRun);
        when(previousRun.getResult()).thenReturn(previousResult);
        TaskListener taskListener = AbstractTest.mockListener();

        // when
        DecisionMaker decisionMaker = new DecisionMaker(run, taskListener);

        // then
        assertThat((Run) Deencapsulation.getField(decisionMaker, "run")).isSameAs(run);
        assertThat((TaskListener) Deencapsulation.getField(decisionMaker, "taskListener")).isSameAs(taskListener);
        assertThat((Result) Deencapsulation.getField(decisionMaker, "previousResult")).isEqualTo(previousResult);
    }

    @Test
    public void isAtLeastOneRuleMatched_OnEmptyMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched).isTrue();
    }

    @Test
    public void isAtLeastOneRuleMatched_OnMatchedMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        String template = "one";
        String value = template;
        Macro macro = new Macro(template, value);
        webhook.setMacros(Arrays.asList(macro));

        mockTokenMacro(value);

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched).isTrue();
    }

    @Test
    public void isAtLeastOneRuleMatched_OnMismatchedMacro_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        String template = "one";
        String value = "two";
        Macro macro = new Macro(template, value);
        webhook.setMacros(Arrays.asList(macro));

        mockTokenMacro(template);

        // when
        boolean matched = decisionMaker.isAtLeastOneRuleMatched(webhook);

        // then
        assertThat(matched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultAborted_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyAndResultFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnNotifyFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultFailureAndPreviousFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultRepeatedFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnOnNotifyAndResultRepeatedFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyRepeatedFailure_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultRepeatedFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultNotBuild_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultSuccess_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousFailure_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.FAILURE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousUnstable_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousNotBuild_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.NOT_BUILT);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnNotifyBackToNormalAndResultFailureAndPreviousAborted_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS, Result.ABORTED);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultSuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifySuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultSuccess_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.SUCCESS);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnNotifyAndResultUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isTrue();
    }

    @Test
    public void isStatusMatched_OnNotifyUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void isStatusMatched_OnResultUnstable_ReturnsTrue() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker(Result.UNSTABLE);
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }


    @Test
    public void isStatusMatched_OnUndefinedResult_ReturnsFalse() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();
        Webhook webhook = new Webhook("someUrl");

        // when
        boolean statusMatched = decisionMaker.isStatusMatched(webhook);

        // then
        assertThat(statusMatched).isFalse();
    }

    @Test
    public void evaluateMacro_OnInvalidMacro_ThrowsException() {

        // given
        DecisionMaker decisionMaker = buildSampleDecisionMaker();

        mockStatic(TokenMacro.class);
        mockStatic(FilePath.class);

        try {
            when(TokenMacro.expandAll(any(), any(), any(), any()))
                    .thenThrow(new MacroEvaluationException("ups!"));
        } catch (MacroEvaluationException | IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }

        // when & then
        assertThatThrownBy(() -> Deencapsulation.invoke(decisionMaker, "evaluateMacro", "anyTemplate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseExactlyInstanceOf(MacroEvaluationException.class);
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
