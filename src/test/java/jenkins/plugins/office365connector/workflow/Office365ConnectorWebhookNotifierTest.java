package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Collections;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.CardBuilder;
import jenkins.plugins.office365connector.DecisionMaker;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.CardBuilderAnswer;
import jenkins.plugins.office365connector.helpers.MockHelper;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Office365ConnectorWebhookNotifier.class)
public class Office365ConnectorWebhookNotifierTest extends AbstractTest {

    private TaskListener taskListener;
    private Job job;

    @Before
    public void setUp() {
        taskListener = mockListener();

        run = mock(AbstractBuild.class);
        when(run.getResult()).thenReturn(Result.SUCCESS);

        job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockEnvironment();
        mockHttpWorker();
    }

    @Test
    public void Office365ConnectorWebhookNotifier_InitializeJob() {

        // given
        // from @Before

        // when
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // then
        assertThat((Job) Deencapsulation.getField(notifier, "job")).isSameAs(job);
    }

    @Test
    public void sendBuildStartedNotification_OnEmptyWebhooks_SkipsProcessing() {

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
    public void sendBuildStartedNotification_OnNoBuild_SkipsProcessing() {

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
        CardBuilderAnswer builderAnswer = new CardBuilderAnswer();
        MockHelper.mockNew(CardBuilder.class, builderAnswer);

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        setWebhookToJob(WebhookBuilder.sampleWebhookWithAllStatuses());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(builderAnswer.getTimes()).isOne();
    }

    @Test
    public void sendBuildCompletedNotification_OnEmptyWebhooks_SkipsProcessing() {

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
        CardBuilderAnswer builderAnswer = new CardBuilderAnswer();
        MockHelper.mockNew(CardBuilder.class, builderAnswer);

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        setWebhookToJob(WebhookBuilder.sampleWebhookWithAllStatuses());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertThat(builderAnswer.getTimes()).isOne();
    }

    private void injectFakeDecisionMaker(Office365ConnectorWebhookNotifier notifier) {
        DecisionMaker decisionMaker = mock(DecisionMaker.class);
        when(decisionMaker.isAtLeastOneRuleMatched(Matchers.any())).thenThrow(new IllegalStateException());
        Deencapsulation.setField(notifier, "decisionMaker", decisionMaker);
    }

    private void setWebhookToJob(List<Webhook> webhooks) {
        WebhookJobProperty property = new WebhookJobProperty(webhooks);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
    }

    @Test
    public void extractWebhooks_OnMissingProperty_ReturnsEmptyWebhooks() {

        // given
        Job job = mock(Job.class);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(null);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = Deencapsulation.invoke(notifier, "extractWebhooks", job);

        // then
        assertThat(webhooks).isEmpty();
    }

    @Test
    public void extractWebhooks_OnMissingWebhooks_ReturnsEmptyWebhooks() {

        // given
        Job job = mock(Job.class);
        WebhookJobProperty property = mock(WebhookJobProperty.class);
        when(property.getWebhooks()).thenReturn(null);
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);

        // when
        List<Webhook> webhooks = Deencapsulation.invoke(notifier, "extractWebhooks", job);

        // then
        assertThat(webhooks).isEmpty();
    }
}
