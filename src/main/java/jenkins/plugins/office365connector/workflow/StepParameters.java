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
package jenkins.plugins.office365connector.workflow;

import java.util.List;

import jenkins.plugins.office365connector.model.FactDefinition;

/**
 * @author srhebbar
 */
public class StepParameters {

    private final String message;
    private final String webhookUrl;
    private final String status;
    private final String color;
    private List<FactDefinition> factDefinitions;
    private final Boolean activityTitle;
    
    public StepParameters(String message, String webhookUrl, String status, List<FactDefinition> factDefinitions, String color) {
        this.message = message;
        this.webhookUrl = webhookUrl;
        this.status = status;
        this.factDefinitions = factDefinitions;
        this.color = color;
        this.activityTitle = activityTitle;
    }

    public String getMessage() {
        return message;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getStatus() {
        return status;
    }

    public List<FactDefinition> getFactDefinitions() {
        return factDefinitions;
    }

    public String getColor() {
        return color;
    }
    
    public Boolean getActivityTitle() {
        return activityTitle;
    }
}
