package jenkins.plugins.office365connector.workflow;

import hudson.model.AbstractBuild;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class WebhookJobPropertyTest extends AbstractTest {

    @Test
    void getWebhooks_ReturnsHook() {

        // when
        List<Webhook> webhooks = WebhookBuilder.sampleWebhookWithAllStatuses();
        WebhookJobProperty property = new WebhookJobProperty(webhooks);

        // when
        List<Webhook> extractedWebhooks = property.getWebhooks();

        // then
        assertThat(extractedWebhooks, hasSize(webhooks.size()));
        assertThat(extractedWebhooks, equalTo(webhooks));

    }

    @Test
    void prebuild_SendNotification() {

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
