package jenkins.plugins.office365connector.workflow;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import javax.annotation.Nonnull;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;

/**
 * Office365ConnectorBuildListener {@link RunListener}.
 * <p>
 * When a build starts, the {@link #onStarted(Run, TaskListener)} method will be invoked. And
 * when a build finishes, the {@link #onCompleted(Run, TaskListener)} method will be invoked.
 *
 * @author Srivardhan Hebbar
 */

@Extension
public class Office365ConnectorBuildListener extends RunListener<Run> {

    /**
     * Called when a build is first started.
     *
     * @param run      - A Run object representing a particular execution of Job.
     * @param listener - A TaskListener object which receives events that happen during some
     *                 operation.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);
        notifier.sendBuildStartedNotification(false);
    }

    /**
     * Called when a build is completed.
     *
     * @param run      - A Run object representing a particular execution of Job.
     * @param listener - A TaskListener object which receives events that happen during some
     *                 operation.
     */
    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);
        notifier.sendBuildCompletedNotification();
    }
}
