package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.PrintStream;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.HttpWorker;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.mockito.Matchers;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class AbstractIntegrationTest {

    protected AbstractBuild run;
    protected HttpWorkerAnswer workerAnswer;

    protected void mockResult(Result result) {
        when(run.getResult()).thenReturn(result);

        Run previousBuild = mock(Run.class);
        if (result == Result.FAILURE) {
            when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
        }
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
