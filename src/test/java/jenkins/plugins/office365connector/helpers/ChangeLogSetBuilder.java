package jenkins.plugins.office365connector.helpers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ChangeLogSetBuilder extends ChangeLogSet {

    private final List entries;

    public ChangeLogSetBuilder(Run run, ChangeLogSet.Entry... entry) {
        super(run, mock(RepositoryBrowser.class));
        entries = Arrays.asList(entry);
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }

    @Override
    public @NonNull Iterator iterator() {
        return entries.iterator();
    }
}
