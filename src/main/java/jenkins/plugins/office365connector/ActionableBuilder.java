/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import java.util.ArrayList;
import java.util.List;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.plugins.office365connector.model.Action;
import jenkins.plugins.office365connector.model.adaptivecard.AdaptiveCardAction;
import jenkins.plugins.office365connector.model.messagecard.PotentialAction;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ActionableBuilder {

    private final Run run;
    private final FactsBuilder factsBuilder;
    private final List<Action> potentialActions = new ArrayList<>();
    private final boolean isAdaptiveCards;

    public ActionableBuilder(Run run, FactsBuilder factsBuilder, boolean isAdaptiveCards) {
        this.run = run;
        this.factsBuilder = factsBuilder;
        this.isAdaptiveCards = isAdaptiveCards;
    }

    public List<Action> buildActionable() {

        pullRequestActionable();
        buildViewBuild();

        return potentialActions;
    }

    private void buildViewBuild() {
        String urlToJob = DisplayURLProvider.get().getRunURL(run);
        String build = Messages.Office365ConnectorWebhookNotifier_BuildPronoun();

        // hide action button when the build succeed
        if (run.getResult() != Result.SUCCESS) {
            String viewHeader = Messages.Office365ConnectorWebhookNotifier_ViewHeader(build);
            potentialActions.add(isAdaptiveCards ? new AdaptiveCardAction(viewHeader,urlToJob) : new PotentialAction(viewHeader, urlToJob));
        }
    }

    // support for pull requests such as https://github.com/jenkinsci/github-branch-source-plugin
    private void pullRequestActionable() {
        Job job = run.getParent();
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        if (head instanceof ChangeRequestSCMHead) {
            String pronoun = StringUtils.defaultIfBlank(
                    head.getPronoun(),
                    Messages.Office365ConnectorWebhookNotifier_ChangeRequestPronoun()
            );
            String viewHeader = Messages.Office365ConnectorWebhookNotifier_ViewHeader(pronoun);
            String titleHeader = Messages.Office365ConnectorWebhookNotifier_TitleHeader(pronoun);
            String authorHeader = Messages.Office365ConnectorWebhookNotifier_AuthorHeader(pronoun);

            ObjectMetadataAction oma = job.getAction(ObjectMetadataAction.class);
            if (oma != null) {
                String urlString = oma.getObjectUrl();
                Action viewPRPotentialAction = isAdaptiveCards ? null : new PotentialAction(viewHeader, urlString);
                potentialActions.add(viewPRPotentialAction);
                factsBuilder.addFact(titleHeader, oma.getObjectDisplayName());
            }
            ContributorMetadataAction cma = job.getAction(ContributorMetadataAction.class);
            if (cma != null) {
                String contributor = cma.getContributor();
                String contributorDisplayName = cma.getContributorDisplayName();

                if (StringUtils.isNotBlank(contributor) && StringUtils.isNotBlank(contributorDisplayName)) {
                    factsBuilder.addFact(authorHeader, String.format("%s (%s)", contributor, contributorDisplayName));
                } else {
                    factsBuilder.addFact(authorHeader, StringUtils.defaultIfBlank(contributor, contributorDisplayName));
                }
            }
        }
    }
}
