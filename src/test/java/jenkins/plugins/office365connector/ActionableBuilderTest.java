package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.List;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.helpers.SCMHeadBuilder;
import jenkins.plugins.office365connector.model.PotentialAction;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Run.class, DisplayURLProvider.class, SCMHead.HeadByItem.class})
public class ActionableBuilderTest {

    private static final String JOB_URL = "http://localhost/job/myFirstJob/167/display/redirect";

    private Run run;
    private TaskListener taskListener;

    private ActionableBuilder actionableBuilder;

    private FactsBuilder factsBuilder;

    @Before
    public void setUp() {
        run = mock(Run.class);
        taskListener = mock(TaskListener.class);

        factsBuilder = new FactsBuilder(run, taskListener);
        actionableBuilder = new ActionableBuilder(run, factsBuilder);

        DisplayURLProvider displayURLProvider = mock(DisplayURLProvider.class);
        when(displayURLProvider.getRunURL(run)).thenReturn(JOB_URL);

        mockStatic(DisplayURLProvider.class);
        when(DisplayURLProvider.get()).thenReturn(displayURLProvider);
    }

    @Test
    public void buildActionable_OnEmptyAction_ReturnsEmptyList() {

        // given
        // from @Before

        // when
        List<PotentialAction> potentialActions = actionableBuilder.buildActionable();

        // then
        assertThat(potentialActions).hasSize(1);
        PotentialAction potentialAction = potentialActions.get(0);
        assertThat(potentialAction.getName()).isEqualTo("View Build");
    }

    @Test
    public void pullRequestActionable_OnNoSCM_DoesNotAddFact() {

        // given
        // from @Before

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        assertThat(factsBuilder.collect()).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnPureChangeRequestSCMHead_DoesNotAddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");


        Job job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(job)).thenReturn(head);
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        assertThat(factsBuilder.collect()).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnContributorMetadataAction_AddsFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        Job job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(job)).thenReturn(head);

        ObjectMetadataAction objectMetadataAction = mock(ObjectMetadataAction.class);
        when(objectMetadataAction.getObjectUrl()).thenReturn("https://github.com/organization/repository/pull/1");
        when(objectMetadataAction.getObjectDisplayName()).thenReturn("test pull request");
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(objectMetadataAction);

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        assertThat(factsBuilder.collect()).hasSize(1);

        List<PotentialAction> potentialActions = Deencapsulation.getField(actionableBuilder, "potentialActions");
        assertThat(potentialActions).hasSize(1);
    }

    @Test
    public void pullRequestActionable_OnObjectMetadataAction_DoesNotAddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        Job job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(job)).thenReturn(head);
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

        ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
        when(contributorMetadataAction.getContributor()).thenReturn("damianszczepanik");
        when(contributorMetadataAction.getContributorDisplayName()).thenReturn("Damian Szczepanik");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        assertThat(factsBuilder.collect()).hasSize(1);

        List<PotentialAction> potentialActions = Deencapsulation.getField(actionableBuilder, "potentialActions");
        assertThat(potentialActions).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnEmptyContributor_AdddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        Job job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(job)).thenReturn(head);
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

        ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
        when(contributorMetadataAction.getContributor()).thenReturn(null);
        when(contributorMetadataAction.getContributorDisplayName()).thenReturn("Damian Szczepanik");
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        String pronoun = StringUtils.defaultIfBlank(
                head.getPronoun(),
                Messages.Office365ConnectorWebhookNotifier_ChangeRequestPronoun());
        FactAssertion.assertThat(factsBuilder)
                .hasName(Messages.Office365ConnectorWebhookNotifier_AuthorHeader(pronoun))
                .hasValue("Damian Szczepanik");
    }

    @Test
    public void pullRequestActionable_OnEmptyContributorDisplayName_AdddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        Job job = mock(Job.class);
        when(run.getParent()).thenReturn(job);

        mockStatic(SCMHead.HeadByItem.class);
        when(SCMHead.HeadByItem.findHead(job)).thenReturn(head);
        when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

        ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
        when(contributorMetadataAction.getContributor()).thenReturn("damianszczepanik");
        when(contributorMetadataAction.getContributorDisplayName()).thenReturn(null);
        when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

        // when
        Deencapsulation.invoke(actionableBuilder, "pullRequestActionable");

        // then
        String pronoun = StringUtils.defaultIfBlank(
                head.getPronoun(),
                Messages.Office365ConnectorWebhookNotifier_ChangeRequestPronoun());
        FactAssertion.assertThat(factsBuilder)
                .hasName(Messages.Office365ConnectorWebhookNotifier_AuthorHeader(pronoun))
                .hasValue("damianszczepanik");
    }
}
