package jenkins.plugins.office365connector;

import static jenkins.plugins.office365connector.Office365ConnectorWebhookNotifierIntegrationTest.mockJob;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Office365ConnectorWebhookNotifierTest {

    private static final String CUSTOM_RUN_NAME = "myCustomRunName";
    private static final String JOB_NAME = "myFirstJob";
    private static final String MULTI_BRANCH_NAME = "myFirstMultiBranchProject";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private Run run;
    private TaskListener listener;

    @Before
    public void setUp() {
        run = mock(Run.class);
        listener = mock(TaskListener.class);
    }

    @Test
    public void getAffectedFiles_ReturnsAffectedFiles() {

        // given
        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        Object patternFiles = Collections.emptyList();
        doReturn(patternFiles).when(entry).getAffectedFiles();

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);

        // when,
        Collection<ChangeLogSet.AffectedFile> files = Deencapsulation.invoke(notifier, "getAffectedFiles", entry);

        // then
        verify(entry, times(1)).getAffectedFiles();
        assertThat(files).isSameAs(patternFiles);
    }

    @Test
    public void getAffectedFiles_OnException_ReturnsEmptyCollection() {

        // given
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

    @Test
    public void getDisplayName_ParentWithoutName() {

        // given
        Job job = mockJob("");
        when(run.getParent()).thenReturn(job);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);

        // when,
        String getDisplayName = Deencapsulation.invoke(notifier, "getDisplayName");

        // then
        assertThat(getDisplayName).isEqualTo(JOB_NAME);
    }

    @Test
    public void getDisplayName_ParentWithName() {

        // given
        Job job = mockJob(MULTI_BRANCH_NAME);
        when(run.getParent()).thenReturn(job);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);

        // when,
        String getJobDisplayName = Deencapsulation.invoke(notifier, "getDisplayName");

        // then
        assertThat(getJobDisplayName).isEqualTo(MULTI_BRANCH_NAME + " » " + JOB_NAME);
    }

    @Test
    public void getDisplayName_RunWithCustomName() throws Exception {

        // given
        when(run.hasCustomDisplayName()).thenReturn(true);
        when(run.getDisplayName()).thenReturn(CUSTOM_RUN_NAME);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);

        // when,
        String getDisplayName = Deencapsulation.invoke(notifier, "getDisplayName");

        // then
        assertThat(getDisplayName).isEqualTo(CUSTOM_RUN_NAME);
    }
}
