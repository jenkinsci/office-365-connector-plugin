package jenkins.plugins.office365connector.workflow;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.CardBuilder;
import jenkins.plugins.office365connector.DecisionMaker;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class Office365ConnectorWebhookNotifierTest extends AbstractTest {

    private TaskListener taskListener;
    private Job job;
    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setUp() {
        taskListener = mockListener();

        run = mock(AbstractBuild.class);
        when(run.getResult()).thenReturn(Result.SUCCESS);

        job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        mockEnvironment();

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("test");

        Jenkins jenkins = mock(Jenkins.class);
        staticJenkins = mockStatic(Jenkins.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(jenkins.getDescriptorOrDie(any())).thenReturn(mockDescriptor);
    }

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    @Test
    void Office365ConnectorWebhookNotifier_InitializeJob() {

        // given
        // from @Before

        // when
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // then
        assertThat(ReflectionHelper.getField(notifier, "job"), sameInstance(job));
    }

    @Test
    void sendBuildStartedNotification_OnEmptyWebhooks_SkipsProcessing() throws Exception {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        injectFakeDecisionMaker(notifier);
        setWebhookToJob(Collections.emptyList());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        // no exception thrown
    }

    @Test
    void sendBuildStartedNotification_OnNoBuild_SkipsProcessing() throws Exception {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        injectFakeDecisionMaker(notifier);
        setWebhookToJob(WebhookBuilder.sampleWebhookWithAllStatuses());

        // when
        notifier.sendBuildStartedNotification(false);

        // then
        // no exception thrown
    }

    @Test
    void sendBuildStartedNotification_OnWebhook_SendsNotification() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        setWebhookToJob(WebhookBuilder.sampleWebhookWithAllStatuses());

        try (MockedConstruction<CardBuilder> cardBuilderConstruction = mockConstruction(CardBuilder.class)) {
            // when
            notifier.sendBuildStartedNotification(true);

            // then
            assertEquals(1, cardBuilderConstruction.constructed().size());
        }
    }

    @Test
    void sendBuildCompletedNotification_OnEmptyWebhooks_SkipsProcessing() throws Exception {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        injectFakeDecisionMaker(notifier);
        setWebhookToJob(Collections.emptyList());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        // no exception thrown
    }

    @Test
    void sendBuildCompletedNotification_OnWebhook_SendsNotification() {
        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        setWebhookToJob(WebhookBuilder.sampleWebhookWithAllStatuses());

        try (MockedConstruction<CardBuilder> cardBuilderConstruction = mockConstruction(CardBuilder.class)) {
            // when
            notifier.sendBuildCompletedNotification();

            // then
            assertEquals(1, cardBuilderConstruction.constructed().size());
        }
    }

    private void injectFakeDecisionMaker(Office365ConnectorWebhookNotifier notifier) {
        DecisionMaker decisionMaker = mock(DecisionMaker.class);
        when(decisionMaker.isAtLeastOneRuleMatched(any())).thenThrow(new IllegalStateException());
        ReflectionHelper.setField(notifier, "decisionMaker", decisionMaker);
    }

    private void setWebhookToJob(List<Webhook> webhooks) {
        WebhookJobProperty property = new WebhookJobProperty(webhooks);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    @Test
    void extractWebhooks_OnMissingProperty_ReturnsEmptyWebhooks() {

        // given
        Job job = mock(Job.class);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(null);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = ReflectionHelper.invokeMethod(notifier,"extractWebhooks", job);

        // then
        assertThat(webhooks, empty());
    }

    @Test
    void extractWebhooks_OnMissingWebhooks_ReturnsEmptyWebhooks() {

        // given
        Job job = mock(Job.class);
        WebhookJobProperty property = mock(WebhookJobProperty.class);
        when(property.getWebhooks()).thenReturn(null);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = ReflectionHelper.invokeMethod(notifier,"extractWebhooks", job);

        // then
        assertThat(webhooks, empty());
    }
}
