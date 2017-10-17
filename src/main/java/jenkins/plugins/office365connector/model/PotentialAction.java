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

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import hudson.Util;

/**
 *
 * @author srhebbar
 */
public class PotentialAction
{
    @SerializedName("@context") private String context;
    
    @SerializedName("@type") private String type;
    
    private String name;

    private List<String> target;

    public PotentialAction(String name, String url) {
        this(name, Collections.singletonList(url));
    }

    public PotentialAction(String name, List<String> url) {
        this.context = "http://schema.org";
        this.type = "ViewAction";
        this.name = name;
        this.target = Util.fixNull(url);
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public List<String> getTarget ()
    {
        return target;
    }

    public void setTarget (List<String> target)
    {
        this.target = target;
    }
    
    public String getContext()
    {
        return this.context;
    }
    
    public void setContext(String context)
    {
        this.context = context;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }

}
