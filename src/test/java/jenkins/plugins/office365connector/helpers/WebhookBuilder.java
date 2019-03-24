package jenkins.plugins.office365connector.helpers;

import java.util.Arrays;
import java.util.List;

import jenkins.plugins.office365connector.Webhook;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookBuilder {

    public static List<Webhook> sampleWebhookWithAllStatuses() {
        Webhook webhook = new Webhook();

        webhook.setUrl(ClassicDisplayURLProviderBuilder.URL);
        webhook.setNotifyAborted(true);
        webhook.setNotifyBackToNormal(true);
        webhook.setNotifyFailure(true);
        webhook.setNotifyNotBuilt(true);
        webhook.setNotifyRepeatedFailure(true);
        webhook.setNotifySuccess(true);
        webhook.setNotifyUnstable(true);
        webhook.setStartNotification(true);

        return Arrays.asList(webhook);
    }

    public static List<Webhook> sampleWebhookWithCredentialsURL(String id) {
        Webhook webhook = new Webhook();

        webhook.setUrlCredentialsId(id);
        webhook.setNotifyAborted(true);
        webhook.setNotifyBackToNormal(true);
        webhook.setNotifyFailure(true);
        webhook.setNotifyNotBuilt(true);
        webhook.setNotifyRepeatedFailure(true);
        webhook.setNotifySuccess(true);
        webhook.setNotifyUnstable(true);
        webhook.setStartNotification(true);

        return Arrays.asList(webhook);
    }
}
