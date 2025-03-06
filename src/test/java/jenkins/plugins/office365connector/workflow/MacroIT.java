package jenkins.plugins.office365connector.workflow;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class MacroIT extends AbstractTest {

    private static final String JOB_NAME = "simple job";
    private static final int BUILD_NUMBER = 1;

    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);

        mockListener();

        run = mockRun();

        mockDisplayURLProvider(JOB_NAME, BUILD_NUMBER);
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();
        mockTokenMacro(String.valueOf(BUILD_NUMBER));

        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
    }

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);

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
    void validateStartedRequest_WithMismatchedMacros_CreatesNoRequest() {

        // given
        mockPropertyWithMismatchedMacros(1);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertEquals(0, workerConstruction.constructed().size());
    }

    @Test
    void validateStartedRequest_WithMatchedMacros_CreatesRequests() {

        // given
        int repeated = 15;
        mockPropertyWithMatchedMacros(repeated);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertEquals(repeated, workerConstruction.constructed().size());
    }

    @Test
    void validateStartedRequest_WithDifferentMacros_CreatesRequests() {

        // given
        int repeated = 15;
        mockPropertyWithDifferentMacros(repeated);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertEquals(repeated, workerConstruction.constructed().size());
    }
}
