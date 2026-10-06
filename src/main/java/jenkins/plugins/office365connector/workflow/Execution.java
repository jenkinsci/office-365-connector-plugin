package jenkins.plugins.office365connector.workflow;


import java.io.IOException;

import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Execution extends SynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = 8433805238008188090L;

    private transient final StepParameters stepParameters;

    public Execution(Office365ConnectorSendStep step, StepContext context) {
        super(context);
        stepParameters = new StepParameters(
                step.getMessage(), step.getWebhookUrl(), step.getStatus(), step.getFactDefinitions(), step.getColor(), step.isAdaptiveCards(), step.getMentions());
    }

    @Override
    protected Void run() throws IOException, InterruptedException {
        Run run = getContext().get(Run.class);
        TaskListener taskListener = getContext().get(TaskListener.class);

        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, taskListener);
        notifier.sendBuildStepNotification(stepParameters);
        return null;
    }
}
