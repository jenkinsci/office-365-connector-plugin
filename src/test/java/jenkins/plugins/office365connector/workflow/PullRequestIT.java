package jenkins.plugins.office365connector.workflow;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.FileUtils;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.SCMHeadBuilder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Office365ConnectorWebhookNotifier.class, SCMHead.HeadByItem.class, Jenkins.class})
public class PullRequestIT extends AbstractTest {

    private static final String PARENT_JOB_NAME = "Damian Szczepanik";
    private static final String JOB_NAME = "hook » PR-1";
    private static final int BUILD_NUMBER = 3;
    private static final String URL_TEMPLATE = "http://localhost:8080/job/GitHub%%20Branch%%20Source/job/hook/job/%s/%s/display/redirect";
    private static final String USER_NAME = "damian";

    @Before
    public void setUp() {

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");

        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        mockListener();

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        when(Jenkins.get()).thenReturn(jenkins);

        run = mockRun();
        mockCause("Branch indexing");
        mockCommitters();

        mockDisplayURLProvider();
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();
        mockPullRequest();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);

        Job job = mockJob(JOB_NAME, PARENT_JOB_NAME);
        when(run.getParent()).thenReturn(job);

        mockProperty(job);

        return run;
    }

    private void mockCommitters() {
        User user = AffectedFileBuilder.mockUser(USER_NAME);
        when(user.getFullName()).thenReturn(USER_NAME);

        when(run.getCulprits()).thenReturn(new HashSet(Arrays.asList(user)));
    }

    private void mockGetChangeSets() {
        List<ChangeLogSet> files = new AffectedFileBuilder().singleChangeLog(run, USER_NAME);
        when(run.getChangeSets()).thenReturn(files);
    }

    private void mockDisplayURLProvider() {
        mockStatic(DisplayURLProvider.class);
        when(DisplayURLProvider.get()).thenReturn(
                new ClassicDisplayURLProviderBuilder(JOB_NAME, BUILD_NUMBER, URL_TEMPLATE));
    }

    private void mockPullRequest() {
        Job job = run.getParent();
        SCMHead head = new SCMHeadBuilder("Pull Request");

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(run.getParent())).thenReturn(head);

        ObjectMetadataAction objectMetadataAction = mock(ObjectMetadataAction.class);
        when(objectMetadataAction.getObjectUrl()).thenReturn("https://github.com/damianszczepanik/hook/pull/1");
        when(objectMetadataAction.getObjectDisplayName()).thenReturn("test pull request");
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(objectMetadataAction);

        ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
        when(contributorMetadataAction.getContributor()).thenReturn("damianszczepanik");
        when(contributorMetadataAction.getContributorDisplayName()).thenReturn("Damian Szczepanik");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);
    }

    @Test
    public void validateRepeatedFailure_WithPullRequest() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("repeated_failure-pull_request.json"));
    }

    @Test
    public void validateBackToNormal_WithoutActions() {

        // given
        mockResult(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("back_to_normal-without_actions.json"));
    }
}
