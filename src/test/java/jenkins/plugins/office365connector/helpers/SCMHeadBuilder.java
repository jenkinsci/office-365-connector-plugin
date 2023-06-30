package jenkins.plugins.office365connector.helpers;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.apache.commons.lang.NotImplementedException;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class SCMHeadBuilder extends SCMHead implements ChangeRequestSCMHead {

    private String name;

    public SCMHeadBuilder(@NonNull String name) {
        super(name);
        this.name = name;
    }

    @Override
    public String getPronoun() {
        return name;
    }

    @NonNull
    @Override
    public String getId() {
        throw new NotImplementedException("Not implemented!");
    }

    @NonNull
    @Override
    public SCMHead getTarget() {
        throw new NotImplementedException("Not implemented!");
    }
}
