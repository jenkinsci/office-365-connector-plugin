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

package jenkins.plugins.office365connector.model;

import java.util.List;

/**
 *
 * @author srhebbar
 */
public class Facts
{
    private String name;

    private String value;

    public Facts(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public Facts(String name, int value) {
        this.name = name;
        this.value = Integer.toString(value);
    }

    public Facts(String name) {
        this.name = name;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    public void setValue(int number) {
        this.value = Integer.toString(number);
    }

    public void setValue(List<String> causesStrList) {
        for (String cause : causesStrList) {
            this.value += cause + ". ";
        }
    }
}
