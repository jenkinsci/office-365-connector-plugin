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

import java.util.Arrays;
import java.util.List;

/**
 * @author srhebbar
 */
public class Card {

    private String summary;
    private String themeColor = "3479BF";

    // even plugin needs only single 'section' connector API expects arrays
    private List<Section> sections;

    private List<PotentialAction> potentialAction;

    public Card(String summary, Section section) {
        this.summary = summary;
        this.sections = Arrays.asList(section);
    }

    public String getSummary() {
        return summary;
    }

    public List<Section> getSections() {
        return this.sections;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setPotentialAction(List<PotentialAction> potentialActions) {
        this.potentialAction = potentialActions;
    }

    public List<PotentialAction> getPotentialAction() {
        return this.potentialAction;
    }
}
