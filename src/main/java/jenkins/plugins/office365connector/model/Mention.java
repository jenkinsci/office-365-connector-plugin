package jenkins.plugins.office365connector.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Ammar Zain (AmmarOFA@github)
 */
public class Mention {
    private final String id;
    private final String name;

    @DataBoundConstructor
    public Mention(String id, String name) {
        this.id = Util.fixNull(id);
        this.name = Util.fixNull(name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
