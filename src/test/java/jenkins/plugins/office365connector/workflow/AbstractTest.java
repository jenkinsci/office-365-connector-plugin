package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.HttpWorker;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import jenkins.plugins.office365connector.helpers.MockHelper;
import jenkins.plugins.office365connector.helpers.Office365ConnectorWebhookNotifierAnswer;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PrepareForTest(DisplayURLProvider.class)
public abstract class AbstractTest {

    private static final String PARENT_JOB_NAME = "Parent project";

    protected AbstractBuild run;
    protected HttpWorkerAnswer workerAnswer;
    protected Office365ConnectorWebhookNotifierAnswer notifierAnswer;

    protected void mockResult(Result lastResult) {
        when(run.getResult()).thenReturn(lastResult);

        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(run.getPreviousBuild()).thenReturn(previousBuild);
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);

        AbstractBuild failingSinceBuild = mock(AbstractBuild.class);
        when(failingSinceBuild.getNumber()).thenReturn(10);

        AbstractBuild lastNotFailedBuild = mock(AbstractBuild.class);
        when(lastNotFailedBuild.getNextBuild()).thenReturn(failingSinceBuild);
        when(run.getPreviousNotFailedBuild()).thenReturn(lastNotFailedBuild);
    }

    public static BuildListener mockListener() {
        BuildListener listener = mock(BuildListener.class);

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
            TaskListener taskListener = ArgumentMatchers.any();
            when(run.getEnvironment(taskListener)).thenReturn(envVars);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
        when(envVars.expand(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE)).thenReturn(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);
    }

    protected AbstractProject mockJob(String jobName) {
        return mockJob(jobName, PARENT_JOB_NAME);
    }

    protected AbstractProject mockJob(String jobName, String parentJobName) {
        AbstractProject job = mock(AbstractProject.class);
        Jenkins jenkinsMock = mock(Jenkins.class);
        when(jenkinsMock.getFullDisplayName()).thenReturn(parentJobName);
        doReturn(jenkinsMock).when(job).getParent();
        when(job.getFullDisplayName()).thenReturn(jobName);

        return job;
    }

    protected void mockCause(String causeMessage) {
        Cause cause = mock(Cause.class);
        when(cause.getShortDescription()).thenReturn(causeMessage);
        when(run.getCauses()).thenReturn(Arrays.asList(cause));
    }

    protected void mockProperty(Job job) {
        mockProperty(job, WebhookBuilder.sampleWebhookWithAllStatuses());
    }

    protected void mockProperty(Job job, List<Webhook> webhooks) {
        WebhookJobProperty property = new WebhookJobProperty(webhooks);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    public static void mockTokenMacro(String evaluatedValue) {
        mockStatic(FilePath.class);
        mockStatic(TokenMacro.class);
        try {
            when(TokenMacro.expandAll(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(evaluatedValue);
        } catch (MacroEvaluationException | IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void mockHttpWorker() {
        workerAnswer = new HttpWorkerAnswer();
        MockHelper.mockNew(HttpWorker.class, workerAnswer);
    }

    protected void mockOffice365ConnectorWebhookNotifier() {
        notifierAnswer = new Office365ConnectorWebhookNotifierAnswer();
        MockHelper.mockNew(Office365ConnectorWebhookNotifier.class, notifierAnswer);
    }

    // compares files without worrying about EOL
    protected void assertHasSameContent(String value, String expected) {
        assertThat(StringUtils.normalizeSpace(value)).isEqualTo(StringUtils.normalizeSpace(expected));
    }
}
