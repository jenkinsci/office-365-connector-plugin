package jenkins.plugins.office365connector.helpers;

import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.model.Macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookBuilder {

    public static List<Webhook> sampleWebhookWithAllStatusesAdaptiveCard() {
        Webhook webhook = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook);
        webhook.setAdaptiveCards(true);

        return List.of(webhook);
    }

    public static List<Webhook> sampleWebhookWithAllStatuses() {
        Webhook webhook = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook);
        return List.of(webhook);
    }

    public static List<Webhook> sampleMultiplyWebhookWithAllStatuses() {
        Webhook webhook1 = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);
        Webhook webhook2 = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook1);
        enableAllStatuses(webhook2);
        return Arrays.asList(webhook1, webhook2);
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

        webhook.setMacros(List.of(new Macro(template, value)));
        return webhook;
    }

    public static List<Webhook> sampleFailedWebhookWithMentions() {
        Webhook webhook = new Webhook(ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE);

        enableAllStatuses(webhook);

        webhook.setAdaptiveCards(true);
        webhook.setMentionOnFailure(true);
        
        return List.of(webhook);
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
