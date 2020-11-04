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
import hudson.util.FormValidation;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.model.Macro;
import jenkins.plugins.office365connector.utils.FormUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class Webhook extends AbstractDescribableImpl<Webhook> {

    public static final Integer DEFAULT_TIMEOUT = 30000;

    private String name;
    private String url;

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

    private List<FactDefinition> factDefinitions = Collections.emptyList();

    @Override
    public DescriptorImpl getDescriptor() {
            return (DescriptorImpl)super.getDescriptor();
    }

    @DataBoundConstructor
    public Webhook(String url) {
        this.url = url;
    }

    public String getUrl() {
        return Util.fixEmptyAndTrim(
            !StringUtils.isEmpty(url) ? url : getDescriptor().getUrl());
    }

    public String getName() {
        return Util.fixEmptyAndTrim(
            !StringUtils.isEmpty(name) ? name : getDescriptor().getName());
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
        return Util.fixNull(macros);
    }

    @DataBoundSetter
    public void setMacros(List<Macro> macros) {
        this.macros = Util.fixNull(macros);
    }

    public List<FactDefinition> getFactDefinitions() {
        return Util.fixNull(factDefinitions);
    }

    @DataBoundSetter
    public void setFactDefinitions(List<FactDefinition> factDefinitions) {
        this.factDefinitions = Util.fixNull(factDefinitions);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Webhook> {
        private String url;
        private String name;

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Webhook";
        }

        public int getDefaultTimeout() {
            return Webhook.DEFAULT_TIMEOUT;
        }

        public FormValidation doCheckUrl(@QueryParameter String value) {
            return FormUtils.formValidateUrl(value);
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            req.bindJSON(this, formData);
            save();
            return true;
        }
    }
}
