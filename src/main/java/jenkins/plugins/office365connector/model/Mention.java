package jenkins.plugins.office365connector.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Defines a user to mention in an Adaptive Card notification.
 */
public class Mention extends AbstractDescribableImpl<Mention> {

    private String userId;
    private String name;

    @DataBoundConstructor
    public Mention(String userId, String name) {
        this.userId = Util.fixNull(userId);
        this.name = Util.fixNull(name);
    }

    public String getUserId() {
        return userId;
    }

    @DataBoundSetter
    public void setUserId(String userId) {
        this.userId = Util.fixNull(userId);
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = Util.fixNull(name);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Mention> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Mention";
        }
    }
}
