/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import hudson.model.JobProperty;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 
 * Job Property.
 *
 */
public class WebhookJobProperty extends
        JobProperty<AbstractProject<?, ?>> {

    public final List<Webhook> webhooks;

    @DataBoundConstructor
    public WebhookJobProperty(List<Webhook> webhooks) {
        this.webhooks = new ArrayList<>( webhooks );
    }

    public List<Webhook> getWebhooks() {
        return webhooks;
    }
    
    @Override
    public WebhookJobPropertyDescriptor getDescriptor() {
        return (WebhookJobPropertyDescriptor) super.getDescriptor();
    }
}