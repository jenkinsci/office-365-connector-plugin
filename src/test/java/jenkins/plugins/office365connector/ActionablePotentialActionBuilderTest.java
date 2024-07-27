package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.helpers.SCMHeadBuilder;
import jenkins.plugins.office365connector.model.Action;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import mockit.internal.reflection.FieldReflection;
import mockit.internal.reflection.MethodReflection;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class ActionablePotentialActionBuilderTest {

    private static final String JOB_URL = "http://localhost/job/myFirstJob/167/display/redirect";

    private AbstractBuild run;
    private TaskListener taskListener;

    private ActionableBuilder actionableBuilder;

    private FactsBuilder factsBuilder;

    private MockedStatic<DisplayURLProvider> displayUrlProviderStatic;

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);
        taskListener = mock(TaskListener.class);

        factsBuilder = new FactsBuilder(run, taskListener);
        actionableBuilder = new ActionableBuilder(run, factsBuilder, false);

        DisplayURLProvider displayURLProvider = mock(DisplayURLProvider.class);
        when(displayURLProvider.getRunURL(run)).thenReturn(JOB_URL);

        displayUrlProviderStatic = mockStatic(DisplayURLProvider.class);
        displayUrlProviderStatic.when(DisplayURLProvider::get).thenReturn(displayURLProvider);
    }

    @After
    public void tearDown() {
        displayUrlProviderStatic.close();
    }

    @Test
    public void buildActionable_OnEmptyAction_ReturnsEmptyList() {

        // given
        // from @Before

        // when
        List<Action> potentialActions = actionableBuilder.buildActionable();

        // then
        assertThat(potentialActions).hasSize(1);
        Action potentialAction = potentialActions.get(0);
        assertThat(potentialAction.getName()).isEqualTo("View Build");
    }

    @Test
    public void pullRequestActionable_OnNoSCM_DoesNotAddFact() throws Throwable {

        // given
        // from @Before

        // when
        MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});

        // then
        assertThat(factsBuilder.collect()).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnPureChangeRequestSCMHead_DoesNotAddFact() throws Throwable {

        // given
        // from @Before
        SCMHead head = new SCMHeadBuilder("Pull Request");


        AbstractProject job = mock(AbstractProject.class);
        when(run.getParent()).thenReturn(job);

        try (MockedStatic<SCMHead.HeadByItem> headByItem = mockStatic(SCMHead.HeadByItem.class)) {
            headByItem.when(() -> SCMHead.HeadByItem.findHead(job)).thenReturn(head);
            when(job.getAction(ObjectMetadataAction.class)).thenReturn(null);

            // when
            MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});
        }

        // then
        assertThat(factsBuilder.collect()).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnContributorMetadataAction_AddsFact() throws Throwable {

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
            MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});
        }

        // then
        assertThat(factsBuilder.collect()).hasSize(1);

        List<Action> potentialActions = FieldReflection.getFieldValue(actionableBuilder.getClass().getDeclaredField("potentialActions"), actionableBuilder);
        assertThat(potentialActions).hasSize(1);
    }

    @Test
    public void pullRequestActionable_OnObjectMetadataAction_DoesNotAddFact() throws Throwable {

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
            MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});
        }


        // then
        assertThat(factsBuilder.collect()).hasSize(1);

        List<Action> potentialActions = FieldReflection.getFieldValue(actionableBuilder.getClass().getDeclaredField("potentialActions"), actionableBuilder);
        assertThat(potentialActions).isEmpty();
    }

    @Test
    public void pullRequestActionable_OnEmptyContributor_AdddFact() throws Throwable {

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
            MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});
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
    public void pullRequestActionable_OnEmptyContributorDisplayName_AdddFact() throws Throwable {

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
            MethodReflection.invokeWithCheckedThrows(actionableBuilder.getClass(), actionableBuilder, "pullRequestActionable", new Class[]{});
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
