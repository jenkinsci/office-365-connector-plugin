package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.helpers.ChangeLogSetBuilder;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DisplayURLProvider.class, Office365ConnectorWebhookNotifier.class, Run.class})
public class Office365ConnectorWebhookNotifierTestInt {

    public static final String JOB_NAME = "myFirstJob";
    private static final int BUILD_NUMBER = 167;
    private static final long START_TIME = 1508617305000L;

    private AbstractBuild run;
    private HttpWorkerAnswer workerAnswer;

    @Before
    public void setUp() {
        mockListener();

        run = mockRun();
        when(run.getChangeSet()).thenReturn(new ChangeLogSetBuilder(run));

        mockDisplayURLProvider();
        mockEnvironment();
        mockHttpWorker();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        PowerMockito.when(run.getStartTimeInMillis()).thenReturn(START_TIME);

        Job job = mockJob();
        when(run.getParent()).thenReturn(job);

        // getProperty
        WebhookJobProperty property = new WebhookJobProperty(WebhookBuilder.sampleWebhookWithAllStatuses());
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);

        return run;
    }

    private Job mockJob() {
        Job job = mock(Job.class);

        when(job.getDisplayName()).thenReturn(JOB_NAME);

        return job;
    }

    private TaskListener mockListener() {
        TaskListener listener = mock(TaskListener.class);

        PrintStream stream = mock(PrintStream.class);
        when(listener.getLogger()).thenReturn(stream);

        return listener;
    }

    private void mockDisplayURLProvider() {
        mockStatic(DisplayURLProvider.class);
        when(DisplayURLProvider.get()).thenReturn(new ClassicDisplayURLProviderBuilder());
    }

    private void mockEnvironment() {
        EnvVars envVars = mock(EnvVars.class);
        try {
            TaskListener taskListener = Matchers.any();
            when(run.getEnvironment(taskListener)).thenReturn(envVars);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
        when(envVars.expand(ClassicDisplayURLProviderBuilder.URL)).thenReturn(ClassicDisplayURLProviderBuilder.URL);
    }

    private void mockHttpWorker() {
        workerAnswer = new HttpWorkerAnswer();
        try {
            whenNew(HttpWorker.class).withAnyArguments().thenAnswer(workerAnswer);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Test
    public void validateRequest_OnStart() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerAnswer.getData()).isEqualTo(pathToSampleFile("requests/started.json"));
    }

    protected static String pathToSampleFile(String fileName) {
        try {
            URL url = Office365ConnectorWebhookNotifierTestInt.class.getClassLoader().getResource(fileName);
            return IOUtils.toString(url.toURI(), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
