package jenkins.plugins.office365connector.workflow;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.FileUtils;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.SCMHeadBuilder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class PullRequestIT extends AbstractTest {

    private static final String JOB_NAME = "hook » PR-1";
    private static final int BUILD_NUMBER = 3;
    private static final String URL_TEMPLATE = "http://localhost:8080/job/GitHub%%20Branch%%20Source/job/hook/job/%s/%s/display/redirect";
    private static final String USER_NAME = "damian";

    private MockedStatic<Jenkins> staticJenkins;
    private MockedStatic<SCMHead.HeadByItem> headByItem;

    @Before
    public void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        mockListener();

        run = mockRun();
        mockCause("Branch indexing");
        mockCommitters();

        mockDisplayURLProvider(JOB_NAME, BUILD_NUMBER, URL_TEMPLATE);
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();

        mockPullRequest();

        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
    }

    @After
    public void tearDown() {
        headByItem.close();
        staticJenkins.close();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);

        AbstractProject job = mockJob(JOB_NAME);
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

    private void mockPullRequest() {
        Job job = run.getParent();
        SCMHead head = new SCMHeadBuilder("Pull Request");

        headByItem = mockStatic(SCMHead.HeadByItem.class);
        headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);

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
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("repeated_failure-pull_request.json"));
    }

    @Test
    public void validateBackToNormal_WithoutActions() {

        // given
        mockResult(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("back_to_normal-without_actions.json"));
    }
}
