package jenkins.plugins.office365connector.helpers;

import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ChangeLogSetBuilder extends ChangeLogSet {

    private List entries = new ArrayList();

    public ChangeLogSetBuilder(Run run) {
        super(run, mock(RepositoryBrowser.class));
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }

    @Override
    public Iterator iterator() {
        return entries.iterator();
    }
}
