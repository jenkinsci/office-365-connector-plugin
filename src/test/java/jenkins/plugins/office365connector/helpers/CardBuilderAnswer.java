package jenkins.plugins.office365connector.helpers;

import java.util.List;

import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.CardBuilder;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class CardBuilderAnswer implements Answer<CardBuilder> {

    private int times;

    @Override
    public CardBuilder answer(InvocationOnMock invocation) {
        return new CardBuilderMock(
                (Run) (invocation.getArguments())[0],
                (TaskListener) (invocation.getArguments())[1]);
    }

    private class CardBuilderMock extends CardBuilder {

        private CardBuilderMock(Run run, TaskListener listener) {
            super(run, listener);
        }

        @Override
        public Card createStartedCard(List<FactDefinition> factDefinitions) {
            return register();
        }

        @Override
        public Card createCompletedCard(List<FactDefinition> factDefinitions) {
            return register();
        }

        @Override
        public Card createBuildMessageCard(StepParameters stepParameters) {
            return register();
        }

        private Card register() {
            times++;
            return new Card(null, null);
        }
    }

    public int getTimes() {
        return times;
    }
}