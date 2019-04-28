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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.FormValidation;
import hudson.util.Secret;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import jenkins.plugins.office365connector.utils.FormUtils;
import jenkins.plugins.office365connector.model.Macro;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class Webhook extends AbstractDescribableImpl<Webhook> {

    public static final Integer DEFAULT_TIMEOUT = 30000;

    private String name;
    // The url can be specified using either a string or the id of plain text credentials
    private String url;
    private String urlCredentialsId;

    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;

    private int timeout;

    private List<Macro> macros = Collections.emptyList();

    public String getUrl(Run run) {
        if (this.url != null) {
            return this.url;
        } else if (this.urlCredentialsId != null){
            StringCredentials urlCredentials = CredentialsProvider.findCredentialById(this.urlCredentialsId, StringCredentials.class, run);
            if (urlCredentials == null) {
                throw new RuntimeException("Cannot find a credential with the ID " + this.urlCredentialsId);
            }
            return Secret.toString(urlCredentials.getSecret());
        } else {
            throw new RuntimeException("Neither url nor urlCredentialsId specified!");
        }
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = Util.fixEmptyAndTrim(url);
    }

    @DataBoundSetter
    public void setUrlCredentialsId(String id) {
        this.urlCredentialsId = Util.fixEmptyAndTrim(id);
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = Util.fixEmptyAndTrim(name);
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    @DataBoundSetter
    public void setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
    }

    public boolean isStartNotification() {
        return startNotification;
    }

    @DataBoundSetter
    public void setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    @DataBoundSetter
    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    @DataBoundSetter
    public void setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    @DataBoundSetter
    public void setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    @DataBoundSetter
    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    @DataBoundSetter
    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    public boolean isNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    @DataBoundSetter
    public void setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
        this.notifyRepeatedFailure = notifyRepeatedFailure;
    }

    public int getTimeout() {
        return timeout == 0 ? DEFAULT_TIMEOUT : timeout;
    }

    @DataBoundSetter
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<Macro> getMacros() {
		if (macros == null) {
			this.macros = Util.fixNull(macros);
		}
		return macros;
    }

    @DataBoundSetter
    public void setMacros(List<Macro> macros) {
        this.macros = Util.fixNull(macros);
    }

    @Override
    public String toString() {
        return url;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Webhook> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Webhook";
        }

        public int getDefaultTimeout() {
            return Webhook.DEFAULT_TIMEOUT;
        }

        public FormValidation doCheckUrl(@QueryParameter String value, @QueryParameter String urlCredentialsId) {
            return FormUtils.formValidateUrl(value, urlCredentialsId);
        }
    }
}
