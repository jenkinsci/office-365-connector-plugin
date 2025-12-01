package jenkins.plugins.office365connector.model;

import hudson.Util;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Outer entity for adaptive card mentions
 * @author Ammar Zain (AmmarOFA@github)
 */
public class Mention {
    private String text;       
    private final String type = "mention"; // always included in JSON
    private Mentioned mentioned;  

    @DataBoundConstructor
    public Mention() {
        // Empty constructor for databinding
    }

    public String getText() {
        return text;
    }

    public String getType(){
        return type;
    }

    @DataBoundSetter
    public void setText(String text) {
        this.text = text;
    }

    public Mentioned getMentioned() {
        return mentioned;
    }

    @DataBoundSetter
    public void setMentioned(Mentioned mentioned) {
        this.mentioned = mentioned;
    }

    /**
     * Factory method: create a Mention from a Mentioned object.
     */
    public static Mention fromMentioned(Mentioned mentioned) {
        Mention mention = new Mention();
        mention.setMentioned(mentioned);
        mention.setText("<at>" + mentioned.getName() + "</at>");
        return mention;
    }
}