/*
 * Copyright 2016 srhebbar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.apache.commons.lang.StringUtils;

/**
 * @author srhebbar
 */
public class Office365ConnectorWebhookNotifier {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting().create();

    private final DecisionMaker decisionMaker;

    private final Run run;
    private final Job job;
    private final TaskListener taskListener;

    public Office365ConnectorWebhookNotifier(Run run, TaskListener taskListener) {
        this.run = run;
        this.taskListener = taskListener;
        this.decisionMaker = new DecisionMaker(run, taskListener);
        this.job = run.getParent();
    }

    public void sendBuildStartedNotification(boolean isFromPreBuild) {
        boolean isBuild = run instanceof AbstractBuild;
        if (isBuild == isFromPreBuild) {

            List<Webhook> webhooks = extractWebhooks(job);
            for (Webhook webhook : webhooks) {
                if (decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                    if (webhook.isStartNotification()) {
                        CardBuilder cardBuilder = new CardBuilder(run, taskListener, webhook.isAdaptiveCards());
                        Card card = cardBuilder.createStartedCard(webhook.getFactDefinitions());
                        executeWorker(webhook, card);
                    }
                }
            }
        }
    }

    public void sendBuildCompletedNotification() {
        List<Webhook> webhooks = extractWebhooks(job);

        for (Webhook webhook : webhooks) {
            if (decisionMaker.isAtLeastOneRuleMatched(webhook)) {
                if (decisionMaker.isStatusMatched(webhook)) {
                    CardBuilder cardBuilder = new CardBuilder(run, taskListener, webhook.isAdaptiveCards());
                    Card card = cardBuilder.createCompletedCard(webhook.getFactDefinitions());
                    executeWorker(webhook, card);
                }
            }
        }
    }

    private static List<Webhook> extractWebhooks(Job job) {
        WebhookJobProperty property = (WebhookJobProperty) job.getProperty(WebhookJobProperty.class);
        if (property != null && property.getWebhooks() != null) {
            return property.getWebhooks();
        }
        return Collections.emptyList();
    }

    public void sendBuildStepNotification(StepParameters stepParameters) {
        Webhook webhook = new Webhook(stepParameters.getWebhookUrl());

        CardBuilder cardBuilder = new CardBuilder(run, taskListener, stepParameters.isAdaptiveCards());
        Card card;
        // TODO: improve this logic as the user may send any 'status' via pipeline step
        if (StringUtils.isNotBlank(stepParameters.getMessage())) {
            card = cardBuilder.createBuildMessageCard(stepParameters);
        } else if (StringUtils.equalsIgnoreCase(stepParameters.getStatus(), "started")) {
            card = cardBuilder.createStartedCard(stepParameters.getFactDefinitions());
        } else {
            card = cardBuilder.createCompletedCard(stepParameters.getFactDefinitions());
        }

        executeWorker(webhook, card);
    }

    private void executeWorker(Webhook webhook, Card card) {
        try {
            String url = run.getEnvironment(taskListener).expand(webhook.getUrl());
            String data = gson.toJson(card == null ? null : card.toPaylod());
            HttpWorker worker = new HttpWorker(url, data, webhook.getTimeout(), taskListener.getLogger());
            worker.submit();
        } catch (IOException | InterruptedException | RejectedExecutionException e) {
            log(String.format("Failed to notify webhook: %s", webhook.getName()));
            e.printStackTrace(taskListener.getLogger());
        }
    }

    /**
     * Helper method for logging.
     */
    private void log(String message) {
        taskListener.getLogger().println("[Office365connector] " + message);
    }
}
