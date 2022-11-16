package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

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
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import mockit.internal.reflection.FieldReflection;
import mockit.internal.reflection.MethodReflection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Office365ConnectorWebhookNotifierTest extends AbstractTest {

    private TaskListener taskListener;
    private Job job;
    private MockedStatic<Jenkins> staticJenkins;

    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() {
        staticJenkins.close();
    }

    @Test
    public void Office365ConnectorWebhookNotifier_InitializeJob() throws Exception {

        // given
        // from @Before

        // when
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // then
        assertThat((Job) FieldReflection.getFieldValue(notifier.getClass().getDeclaredField("job"), notifier)).isSameAs(job);
    }

    @Test
    public void sendBuildStartedNotification_OnEmptyWebhooks_SkipsProcessing() throws Exception {

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
    public void sendBuildStartedNotification_OnNoBuild_SkipsProcessing() throws Exception {

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
    public void sendBuildStartedNotification_OnWebhook_SendsNotification() {

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
    public void sendBuildCompletedNotification_OnEmptyWebhooks_SkipsProcessing() throws Exception {

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
    public void sendBuildCompletedNotification_OnWebhook_SendsNotification() {
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

    private void injectFakeDecisionMaker(Office365ConnectorWebhookNotifier notifier) throws NoSuchFieldException {
        DecisionMaker decisionMaker = mock(DecisionMaker.class);
        when(decisionMaker.isAtLeastOneRuleMatched(any())).thenThrow(new IllegalStateException());
        FieldReflection.setFieldValue(notifier.getClass().getDeclaredField("decisionMaker"), notifier, decisionMaker);
    }

    private void setWebhookToJob(List<Webhook> webhooks) {
        WebhookJobProperty property = new WebhookJobProperty(webhooks);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    @Test
    public void extractWebhooks_OnMissingProperty_ReturnsEmptyWebhooks() throws Throwable {

        // given
        Job job = mock(Job.class);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(null);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = MethodReflection.invokeWithCheckedThrows(notifier.getClass(), notifier, "extractWebhooks", new Class[]{Job.class}, job);

        // then
        assertThat(webhooks).isEmpty();
    }

    @Test
    public void extractWebhooks_OnMissingWebhooks_ReturnsEmptyWebhooks() throws Throwable {

        // given
        Job job = mock(Job.class);
        WebhookJobProperty property = mock(WebhookJobProperty.class);
        when(property.getWebhooks()).thenReturn(null);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = MethodReflection.invokeWithCheckedThrows(notifier.getClass(), notifier, "extractWebhooks", new Class[]{Job.class}, job);

        // then
        assertThat(webhooks).isEmpty();
    }
}
