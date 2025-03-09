package jenkins.plugins.office365connector;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.helpers.SCMHeadBuilder;
import jenkins.plugins.office365connector.model.CardAction;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ActionableBuilderTest {

    private static final String JOB_URL = "http://localhost/job/myFirstJob/167/display/redirect";

    private AbstractBuild run;
    private TaskListener taskListener;

    private ActionableBuilder actionableBuilder;

    private FactsBuilder factsBuilder;

    private MockedStatic<DisplayURLProvider> displayUrlProviderStatic;

    @BeforeEach
    void setUp() {
        run = mock(AbstractBuild.class);
        taskListener = mock(TaskListener.class);

        factsBuilder = new FactsBuilder(run, taskListener);
        actionableBuilder = new ActionableBuilder(run, factsBuilder, false);

        DisplayURLProvider displayURLProvider = mock(DisplayURLProvider.class);
        when(displayURLProvider.getRunURL(run)).thenReturn(JOB_URL);

        displayUrlProviderStatic = mockStatic(DisplayURLProvider.class);
        displayUrlProviderStatic.when(DisplayURLProvider::get).thenReturn(displayURLProvider);
    }

    @AfterEach
    void tearDown() {
        displayUrlProviderStatic.close();
    }

    @Test
    void buildActionable_OnEmptyAction_ReturnsEmptyList() {

        // given
        // from @Before

        // when
        List<CardAction> potentialActions = actionableBuilder.buildActionable();

        // then
        assertThat(potentialActions, hasSize(1));
        CardAction potentialAction = potentialActions.get(0);
        assertThat(potentialAction.getName(), equalTo("View Build"));
    }

    @Test
    void pullRequestActionable_OnNoSCM_DoesNotAddFact() {

        // given
        // from @Before

        // when
        ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");

        // then
        assertThat(factsBuilder.collect(), empty());
    }

    @Test
    void pullRequestActionable_OnPureChangeRequestSCMHead_DoesNotAddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");


        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

            // when
            ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");
        }

        // then
        assertThat(factsBuilder.collect(), empty());
    }

    @Test
    void pullRequestActionable_OnContributorMetadataAction_AddsFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);

            ObjectMetadataAction objectMetadataAction = mock(ObjectMetadataAction.class);
            when(objectMetadataAction.getObjectUrl()).thenReturn("https://github.com/organization/repository/pull/1");
            when(objectMetadataAction.getObjectDisplayName()).thenReturn("test pull request");
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(objectMetadataAction);

            // when
            ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");
        }

        // then
        assertThat(factsBuilder.collect(), hasSize(1));

        List<CardAction> potentialActions = ReflectionHelper.getField(actionableBuilder,"potentialActions");
        assertThat(potentialActions, hasSize(1));
    }

    @Test
    void pullRequestActionable_OnObjectMetadataAction_DoesNotAddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

            ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
            when(contributorMetadataAction.getContributor()).thenReturn("damianszczepanik");
            when(contributorMetadataAction.getContributorDisplayName()).thenReturn("Damian Szczepanik");
            when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

            // when
            ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");
        }


        // then
        assertThat(factsBuilder.collect(), hasSize(1));

        List<CardAction> potentialActions = ReflectionHelper.getField(actionableBuilder,"potentialActions");
        assertThat(potentialActions, empty());
    }

    @Test
    void pullRequestActionable_OnEmptyContributor_AdddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

            ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
            when(contributorMetadataAction.getContributor()).thenReturn(null);
            when(contributorMetadataAction.getContributorDisplayName()).thenReturn("Damian Szczepanik");
            when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

            // when
            ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");
        }

        // then
        String pronoun = StringUtils.defaultIfBlank(
                head.getPronoun(),
                Messages.Office365ConnectorWebhookNotifier_ChangeRequestPronoun());
        FactAssertion.assertThat(factsBuilder)
                .hasName(Messages.Office365ConnectorWebhookNotifier_AuthorHeader(pronoun))
                .hasValue("Damian Szczepanik");
    }

    @Test
    void pullRequestActionable_OnEmptyContributorDisplayName_AdddFact() {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");

        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

            ContributorMetadataAction contributorMetadataAction = mock(ContributorMetadataAction.class);
            when(contributorMetadataAction.getContributor()).thenReturn("damianszczepanik");
            when(contributorMetadataAction.getContributorDisplayName()).thenReturn(null);
            when(job.getAction(ContributorMetadataAction.class)).thenReturn(contributorMetadataAction);

            // when
            ReflectionHelper.invokeMethod(actionableBuilder, "pullRequestActionable");
        }

        // then
        String pronoun = StringUtils.defaultIfBlank(
                head.getPronoun(),
                Messages.Office365ConnectorWebhookNotifier_ChangeRequestPronoun());
        FactAssertion.assertThat(factsBuilder)
                .hasName(Messages.Office365ConnectorWebhookNotifier_AuthorHeader(pronoun))
                .hasValue("damianszczepanik");
    }
}
