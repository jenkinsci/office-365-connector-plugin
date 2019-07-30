package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;

import hudson.model.AbstractBuild;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Office365ConnectorWebhookNotifier.class, Office365ConnectorBuildListener.class})
public class Office365ConnectorBuildListenerTest extends AbstractTest {

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);

        mockOffice365ConnectorWebhookNotifier();
    }

    @Test
    public void onStarted_SendNotification() {

        // given
        Office365ConnectorBuildListener listener = new Office365ConnectorBuildListener();

        // when
        listener.onStarted(run, mockListener());

        assertThat(notifierAnswer.getTimes()).isOne();
    }

    @Test
    public void onCompleted_SendNotification() {

        // given
        Office365ConnectorBuildListener listener = new Office365ConnectorBuildListener();

        // when
        listener.onCompleted(run, mockListener());

        assertThat(notifierAnswer.getTimes()).isOne();
    }
}
