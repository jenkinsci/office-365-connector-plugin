package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.HttpWorker;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.After;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public abstract class AbstractTest {

    protected AbstractBuild run;

    protected MockedConstruction<HttpWorker> workerConstruction;
    protected List<String> workerData;
    private MockedStatic<DisplayURLProvider> displayUrlProviderStatic;
    private MockedStatic<TokenMacro> tokenMacroStatic;
    private MockedStatic<FilePath> filePathStatic;

    @After
    public void closeMocks() {
        if (workerConstruction != null) {
            workerConstruction.close();
        }
        if (displayUrlProviderStatic != null) {
            displayUrlProviderStatic.close();
        }
        if (tokenMacroStatic != null) {
            tokenMacroStatic.close();
        }
        if (filePathStatic != null) {
            filePathStatic.close();
        }
    }

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
        mockDisplayURLProviderImpl(new ClassicDisplayURLProviderBuilder(jobName, jobNumber));
    }

    protected void mockDisplayURLProvider(String jobName, int jobNumber, String urlTemplate) {
        mockDisplayURLProviderImpl(new ClassicDisplayURLProviderBuilder(jobName, jobNumber, urlTemplate));
    }

    private void mockDisplayURLProviderImpl(ClassicDisplayURLProviderBuilder value) {
        if (displayUrlProviderStatic != null) {
            throw new IllegalStateException("Can only mock the display URL provider once per test");
        }
        displayUrlProviderStatic = mockStatic(DisplayURLProvider.class);
        displayUrlProviderStatic.when(DisplayURLProvider::get).thenReturn(value);
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
        AbstractProject job = mock(AbstractProject.class);
        Jenkins jenkinsMock = mock(Jenkins.class);
        when(job.getParent()).thenReturn(jenkinsMock);
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

    protected void mockTokenMacro(String evaluatedValue) {
        if (tokenMacroStatic != null || filePathStatic != null) {
            throw new IllegalStateException("Can only mock token macro once per test");
        }

        tokenMacroStatic = mockStatic(TokenMacro.class);
        filePathStatic = mockStatic(FilePath.class);

        tokenMacroStatic.when(() -> TokenMacro.expandAll(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(evaluatedValue);
    }

    protected void mockHttpWorker() {
        if (workerConstruction != null || workerData != null) {
            throw new IllegalStateException("Can only mock worker construction once per test");
        }
        workerData = new ArrayList<>();
        workerConstruction = mockConstruction(HttpWorker.class, (mock, context) -> workerData.add(context.arguments().get(1).toString()));
    }

    // compares files without worrying about EOL
    protected void assertHasSameContent(String value, String expected) {
        assertThat(StringUtils.normalizeSpace(value)).isEqualTo(StringUtils.normalizeSpace(expected));
    }
}
