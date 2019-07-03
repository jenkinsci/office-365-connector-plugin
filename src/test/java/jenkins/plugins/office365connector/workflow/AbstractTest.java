package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.PrintStream;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.HttpWorker;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PrepareForTest({DisplayURLProvider.class})
public abstract class AbstractTest {

    protected AbstractBuild run;
    protected HttpWorkerAnswer workerAnswer;

    protected void mockFailedResult() {
        Result lastResult = Result.FAILURE;
        when(run.getResult()).thenReturn(lastResult);

        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(run.getPreviousBuild()).thenReturn(previousBuild);
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);

        Run failingSinceBuild = mock(Run.class);
        when(failingSinceBuild.getNumber()).thenReturn(10);

        Run lastNotFailedBuild = mock(Run.class);
        when(lastNotFailedBuild.getNextBuild()).thenReturn(failingSinceBuild);
        when(run.getPreviousNotFailedBuild()).thenReturn(lastNotFailedBuild);
    }

    protected TaskListener mockListener() {
        TaskListener listener = mock(TaskListener.class);

        PrintStream stream = mock(PrintStream.class);
        when(listener.getLogger()).thenReturn(stream);

        return listener;
    }

    protected void mockDisplayURLProvider(String jobName, int jobNumber) {
        mockStatic(DisplayURLProvider.class);
        when(DisplayURLProvider.get()).thenReturn(new ClassicDisplayURLProviderBuilder(jobName, jobNumber));
    }

    protected void mockEnvironment() {
        EnvVars envVars = mock(EnvVars.class);
        try {
            TaskListener taskListener = Matchers.any();
            when(run.getEnvironment(taskListener)).thenReturn(envVars);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
        when(envVars.expand(ClassicDisplayURLProviderBuilder.URL_TEMPLATE)).thenReturn(ClassicDisplayURLProviderBuilder.URL_TEMPLATE);
    }

    protected Job mockJob(String jobName) {
        Job job = mock(Job.class);
        ItemGroup itemGroup = mock(ItemGroup.class);
        when(itemGroup.getFullDisplayName()).thenReturn("Parent project");
        when(job.getParent()).thenReturn(itemGroup);
        when(job.getFullDisplayName()).thenReturn(jobName);

        return job;
    }

    protected void mockTokenMacro(String evaluatedValue) {
        mockStatic(FilePath.class);
        mockStatic(TokenMacro.class);
        try {
            when(TokenMacro.expandAll(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(evaluatedValue);
        } catch (MacroEvaluationException | IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void mockHttpWorker() {
        workerAnswer = new HttpWorkerAnswer();
        try {
            whenNew(HttpWorker.class).withAnyArguments().thenAnswer(workerAnswer);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    // compares files without worrying about EOL
    protected void assertHasSameContent(String value, String expected) {
        assertThat(StringUtils.normalizeSpace(value)).isEqualTo(StringUtils.normalizeSpace(expected));
    }
}
