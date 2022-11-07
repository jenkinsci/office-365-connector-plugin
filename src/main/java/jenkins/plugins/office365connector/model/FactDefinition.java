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

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Defines fact template with name and value.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactDefinition extends AbstractDescribableImpl<FactDefinition> {

    /**
     * Expected name of the fact.
     */
    private String name;

    /**
     * Template that will be evaluated.
     */
    private String template;

    @DataBoundConstructor
    public FactDefinition(String name, String template) {
        this.name = Util.fixNull(name);
        this.template = Util.fixNull(template);
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = Util.fixNull(name);
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setTemplate(String template) {
        this.template = Util.fixNull(template);
    }

    public String getTemplate() {
        return template;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<FactDefinition> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "FactDefinition";
        }
    }
}
