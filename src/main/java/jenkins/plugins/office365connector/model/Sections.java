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
public class Sections
{
    private boolean markdown;

    private List<Facts> facts;

    private String activityTitle;

    private String activityText;

    private String activitySubtitle;

    public Sections(String activityTitle, String activitySubtitle, List<Facts> factsList) {
        this.activityTitle = activityTitle;
        this.activitySubtitle = activitySubtitle;
        this.markdown = true;
        this.facts = factsList;
    }

    public boolean getMarkdown ()
    {
        return markdown;
    }

    public void setMarkdown (boolean markdown)
    {
        this.markdown = markdown;
    }

    public String getActivityTitle ()
    {
        return activityTitle;
    }

    public void setActivityTitle (String activityTitle)
    {
        this.activityTitle = activityTitle;
    }

    public List<Facts> getFacts ()
    {
        return facts;
    }
    
    public void setFacts(List<Facts> facts)
    {
        this.facts = facts;
    }

    public String getActivityText ()
    {
        return activityText;
    }

    public void setActivityText (String activityText)
    {
        this.activityText = activityText;
    }

    public String getActivitySubtitle ()
    {
        return activitySubtitle;
    }

    public void setActivitySubtitle (String activitySubtitle)
    {
        this.activitySubtitle = activitySubtitle;
    }
/*
    @Override
    public String toString()
    {
        String temp;
        temp = "{\"markdown\": "+markdown+",\"facts\": [";
        for (Facts fact: this.facts) {
            temp += fact.toString();
        }
        temp += "],"+", \"activityTitle\": \""+activityTitle+"\"}";
        
        return temp;
    }*/

}
