package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.util.List;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import jenkins.plugins.office365connector.utils.TimeUtils;
import jenkins.plugins.office365connector.utils.TimeUtilsTest;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DisplayURLProvider.class, Office365ConnectorWebhookNotifier.class, Run.class, TimeUtils.class, TokenMacro.class, FilePath.class})
public class MacroIT extends AbstractIT {

    private static final String JOB_NAME = "simple job";
    private static final int BUILD_NUMBER = 1;
    private static final long START_TIME = 1508617305000L;

    private static final String FORMATTED_START_TIME;

    static {
        TimeUtilsTest.setupTimeZoneAndLocale();
        FORMATTED_START_TIME = TimeUtils.dateToString(START_TIME);
    }

    @Before
    public void setUp() {
        mockListener();

        run = mockRun();

        mockDisplayURLProvider(JOB_NAME, BUILD_NUMBER);
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();
        mockTokenMacro(String.valueOf(BUILD_NUMBER));
        mockTimeUtils();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        when(run.getStartTimeInMillis()).thenReturn(START_TIME);

        Job job = mockJob(JOB_NAME);
        when(run.getParent()).thenReturn(job);

        File rootDir = mock(File.class);
        when(run.getRootDir()).thenReturn(rootDir);

        return run;
    }

    private void mockGetChangeSets() {
        List<ChangeLogSet> files = new AffectedFileBuilder().sampleChangeLogs(run);
        when(run.getChangeSets()).thenReturn(files);
    }

    private void mockTimeUtils() {
        mockStatic(TimeUtils.class);
        when(TimeUtils.dateToString(START_TIME)).thenReturn(FORMATTED_START_TIME);
    }

    private void mockPropertyWithMatchedMacros(int repeated) {
        WebhookJobProperty property = new WebhookJobProperty(
                WebhookBuilder.sampleWebhookWithMacro("${BUILD_NUMBER}", String.valueOf(BUILD_NUMBER), repeated));
        when(run.getParent().getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    private void mockPropertyWithMismatchedMacros(int repeated) {
        WebhookJobProperty property = new WebhookJobProperty(
                WebhookBuilder.sampleWebhookWithMacro("one", "two", repeated));
        when(run.getParent().getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    private void mockPropertyWithDifferentMacros(int repeated) {
        List<Webhook> webhooks = WebhookBuilder.sampleWebhookWithMacro("${BUILD_NUMBER}", String.valueOf(BUILD_NUMBER), repeated);
        webhooks.addAll(WebhookBuilder.sampleWebhookWithMacro("one", "two", repeated));

        WebhookJobProperty property = new WebhookJobProperty(webhooks);
        when(run.getParent().getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    @Test
    public void validateStartedRequest_WithMismatchedMacros_CreatesNoRequest() {

        // given
        mockPropertyWithMismatchedMacros(1);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerAnswer.getTimes()).isZero();
    }

    @Test
    public void validateStartedRequest_WithMatchedMacros_CreatesRequests() {

        // given
        int repeated = 15;
        mockPropertyWithMatchedMacros(repeated);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerAnswer.getTimes()).isEqualTo(repeated);
    }

    @Test
    public void validateStartedRequest_WithDifferentMacros_CreatesRequests() {

        // given
        int repeated = 15;
        mockPropertyWithDifferentMacros(repeated);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerAnswer.getTimes()).isEqualTo(repeated);
    }
}
