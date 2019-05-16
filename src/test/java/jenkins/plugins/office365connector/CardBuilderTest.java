package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import mockit.Mocked;
import org.junit.Test;

public class CardBuilderTest {

    @Mocked
    public TaskListener taskListener;

    @Mocked
    private Run run;


    @Test
    public void calculateStatus_OnSuccess_ReturnsBackToNormal() {

        // given
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
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
        CardBuilder cardBuilder = new CardBuilder(run, taskListener);
        Result lastResult = Result.NOT_BUILT;
        boolean isRepeatedFailure = true;
        Result previousResult = null;

        // when
        String status = cardBuilder.calculateSummary(lastResult, previousResult, isRepeatedFailure);

        // then
        assertThat(status).isEqualTo(lastResult.toString());
    }
}
