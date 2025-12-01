package jenkins.plugins.office365connector.model;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Inner entity for adaptive card mentions
 * @author Ammar Zain (AmmarOFA@github)
 */
public class Mentioned {
    private String id = "";
    private String name = "";

    @DataBoundConstructor
    public Mentioned() {
        // Empty constructor for databinding
    }

    @DataBoundSetter
    public void setId(String id) {
        this.id = Util.fixNull(id);
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = Util.fixNull(name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
