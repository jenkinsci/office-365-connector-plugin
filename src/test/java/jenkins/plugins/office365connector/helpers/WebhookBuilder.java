package jenkins.plugins.office365connector.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.model.Macro;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookBuilder {

    public static List<Webhook> sampleWebhookWithAllStatuses() {
        Webhook webhook = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook);
        return Arrays.asList(webhook);
    }

    public static List<Webhook> sampleWebhookWithMacro(String template, String value, int repeated) {
        List<Webhook> webhooks = new ArrayList<>();
        for (int i = 0; i < repeated; i++) {
            webhooks.add(createWebhook(template, value));
        }
        return webhooks;
    }

    private static Webhook createWebhook(String template, String value) {
        Webhook webhook = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook);

        webhook.setMacros(Arrays.asList(new Macro(template, value)));
        return webhook;
    }

    private static void enableAllStatuses(Webhook webhook) {
        webhook.setNotifyAborted(true);
        webhook.setNotifyBackToNormal(true);
        webhook.setNotifyFailure(true);
        webhook.setNotifyNotBuilt(true);
        webhook.setNotifyRepeatedFailure(true);
        webhook.setNotifySuccess(true);
        webhook.setNotifyUnstable(true);
        webhook.setStartNotification(true);
    }
}
