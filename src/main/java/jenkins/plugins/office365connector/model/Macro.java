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
package jenkins.plugins.office365connector.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Defines template and expected value for the macro.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Macro extends AbstractDescribableImpl<Macro> {

    /**
     * Template that will be evaluated.
     */
    private final String template;

    /**
     * Expected value of the evaluated {@link #template}.
     */
    private final String value;

    @DataBoundConstructor
    public Macro(String template, String value) {
        this.template = Util.fixNull(template);
        this.value = Util.fixNull(value);
    }

    public String getTemplate() {
        return template;
    }

    public String getValue() {
        return value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Macro> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Macro";
        }
    }
}
