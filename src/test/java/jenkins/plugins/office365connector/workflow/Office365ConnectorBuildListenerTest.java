package jenkins.plugins.office365connector.workflow;

import hudson.model.AbstractBuild;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class Office365ConnectorBuildListenerTest extends AbstractTest {

    @BeforeEach
    void setUp() {
        run = mock(AbstractBuild.class);
    }

    @Test
    void onStarted_SendNotification() {

        // given
        Office365ConnectorBuildListener listener = new Office365ConnectorBuildListener();

        try (MockedConstruction<Office365ConnectorWebhookNotifier> notifierConstruction = mockConstruction(Office365ConnectorWebhookNotifier.class)) {
            // when
            listener.onStarted(run, mockListener());

            // then
            assertEquals(1, notifierConstruction.constructed().size());
        }
    }

    @Test
    void onCompleted_SendNotification() {

        // given
        Office365ConnectorBuildListener listener = new Office365ConnectorBuildListener();

        try (MockedConstruction<Office365ConnectorWebhookNotifier> notifierConstruction = mockConstruction(Office365ConnectorWebhookNotifier.class)) {
            // when
            listener.onCompleted(run, mockListener());

            // then
            assertEquals(1, notifierConstruction.constructed().size());
        }
    }
}
