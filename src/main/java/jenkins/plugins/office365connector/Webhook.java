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

import java.util.Collections;
import java.util.List;

import hudson.Util;
import hudson.util.FormValidation;
import jenkins.plugins.office365connector.model.Macro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class Webhook {

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

    @DataBoundConstructor
    public Webhook(String name, String url, boolean startNotification, boolean notifySuccess, boolean notifyAborted,
                   boolean notifyNotBuilt, boolean notifyUnstable, boolean notifyFailure, boolean notifyBackToNormal,
                   boolean notifyRepeatedFailure, int timeout, List<Macro> macros) {
        this.name = name;
        this.url = url;
        this.startNotification = startNotification;
        this.notifySuccess = notifySuccess;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyFailure = notifyFailure;
        this.notifyUnstable = notifyUnstable;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifyAborted = notifyAborted;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.timeout = timeout;
        this.macros = Util.fixNull(macros);
    }

    public Webhook(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public boolean isStartNotification() {
        return startNotification;
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean isNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public int getTimeout() {
        return timeout;
    }

    public List<Macro> getMacros() {
        return macros;
    }

    public FormValidation doCheckURL(@QueryParameter(value = "url", fixEmpty = true) String url) {
        if (url.equals("111"))
            return FormValidation.ok();
        else
            return FormValidation.error("There's a problem here");
    }

    @Override
    public String toString() {
        return url;
    }
}