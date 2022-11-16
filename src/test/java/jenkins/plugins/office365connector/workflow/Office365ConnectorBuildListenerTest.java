package jenkins.plugins.office365connector.workflow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

import hudson.model.AbstractBuild;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Office365ConnectorBuildListenerTest extends AbstractTest {

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);
    }

    @Test
    public void onStarted_SendNotification() {

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
    public void onCompleted_SendNotification() {

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
