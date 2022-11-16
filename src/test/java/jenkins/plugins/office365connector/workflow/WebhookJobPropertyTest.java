package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

import java.util.Collections;
import java.util.List;

import hudson.model.AbstractBuild;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.junit.Test;
import org.mockito.MockedConstruction;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookJobPropertyTest extends AbstractTest {

    @Test
    public void getWebhooks_ReturnsHook() {

        // when
        List<Webhook> webhooks = WebhookBuilder.sampleWebhookWithAllStatuses();
        WebhookJobProperty property = new WebhookJobProperty(webhooks);

        // when
        List<Webhook> extractedWebhooks = property.getWebhooks();

        // then
        assertThat(extractedWebhooks)
                .hasSameSizeAs(webhooks)
                .containsAll(webhooks);

    }

    @Test
    public void prebuild_SendNotification() {

        // given
        run = mock(AbstractBuild.class);
        WebhookJobProperty property = new WebhookJobProperty(Collections.emptyList());

        try (MockedConstruction<Office365ConnectorWebhookNotifier> notifierConstruction = mockConstruction(Office365ConnectorWebhookNotifier.class)) {
            // when
            property.prebuild(run, mockListener());

            // then
            assertEquals(1, notifierConstruction.constructed().size());
        }
    }
}
