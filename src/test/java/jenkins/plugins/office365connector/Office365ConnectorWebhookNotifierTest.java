package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import mockit.Deencapsulation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Office365ConnectorWebhookNotifierTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getAffectedFiles_ReturnsAffectedFiles() {

        // given
        Run run = mock(Run.class);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        Object patternFiles = Collections.emptyList();
        doReturn(patternFiles).when(entry).getAffectedFiles();

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, null);

        // when,
        Collection<ChangeLogSet.AffectedFile> files = Deencapsulation.invoke(notifier, "getAffectedFiles", entry);

        // then
        verify(entry, times(1)).getAffectedFiles();
        assertThat(files).isSameAs(patternFiles);
    }

    @Test
    public void getAffectedFiles_OnException_ReturnsEmptyCollection() {

        // given
        Run run = mock(Run.class);
        TaskListener listener = mock(TaskListener.class);
        PrintStream stream = mock(PrintStream.class);
        when(listener.getLogger()).thenReturn(stream);
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        when(entry.getAffectedFiles()).thenThrow(UnsupportedOperationException.class);

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);

        // when,
        Collection<ChangeLogSet.AffectedFile> files = Deencapsulation.invoke(notifier, "getAffectedFiles", entry);

        // then
        verify(entry, times(1)).getAffectedFiles();
        assertThat(files).isEmpty();
    }
}
