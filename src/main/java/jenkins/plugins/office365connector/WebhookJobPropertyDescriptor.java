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

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest2;

@Extension
@Symbol("office365ConnectorWebhooks")
public final class WebhookJobPropertyDescriptor extends JobPropertyDescriptor {

    private List<Webhook> webhooks = new ArrayList<>();

    public WebhookJobPropertyDescriptor() {
        super(WebhookJobProperty.class);
        load();
    }

    public boolean isEnabled() {
        return !webhooks.isEmpty();
    }

    public void setWebhooks(List<Webhook> webhooks) {
        this.webhooks = new ArrayList<>(webhooks);
    }

    @Override
    public boolean isApplicable(Class<? extends Job> jobType) {
        // applicable to all types of jobs
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Job Notification";
    }

    @Override
    public WebhookJobProperty newInstance(StaplerRequest2 req, JSONObject formData) {

        List<Webhook> webhooks = new ArrayList<>();
        if (formData != null && !formData.isNullObject()) {
            JSON webhooksData = (JSON) formData.get("webhooks");
            if (webhooksData != null && !webhooksData.isEmpty()) {
                if (webhooksData.isArray()) {
                    JSONArray webhooksArrayData = (JSONArray) webhooksData;
                    webhooks.addAll(req.bindJSONToList(Webhook.class, webhooksArrayData));
                } else {
                    JSONObject webhooksObjectData = (JSONObject) webhooksData;
                    webhooks.add(req.bindJSON(Webhook.class, webhooksObjectData));
                }
            }
        }
        return new WebhookJobProperty(webhooks);
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject formData) {
        save();
        return true;
    }

}
