package jenkins.plugins.office365connector.helpers;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import org.mockito.stubbing.Answer;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class AffectedFileBuilder {

    public static final String[] sampleAuthors = {"Peter", "George Great", "Ann, the Queen"};

    public List<ChangeLogSet> singleChangeLog(AbstractBuild run, String singleAuthor) {
        ChangeLogSet.Entry entryMike = mockEntry(singleAuthor);

        when(entryMike.getAffectedFiles()).thenAnswer(createAnswer(Arrays.asList(new File(), new File())));

        return Arrays.asList(new ChangeLogSetBuilder(run, entryMike));
    }

    public List<ChangeLogSet> sampleChangeLogs(AbstractBuild run) {
        ChangeLogSet.Entry entryPeter = mockEntry(sampleAuthors[0]);
        ChangeLogSet.Entry entryGeorge = mockEntry(sampleAuthors[1]);
        ChangeLogSet.Entry entryAnn = mockEntry(sampleAuthors[2]);

        when(entryPeter.getAffectedFiles()).thenAnswer(createAnswer(Arrays.asList(new File(), new File())));
        when(entryPeter.getAffectedFiles()).thenAnswer(createAnswer(Collections.emptyList()));
        when(entryAnn.getAffectedFiles()).thenAnswer(createAnswer(Collections.emptyList()));

        return Arrays.asList(new ChangeLogSetBuilder(run, entryPeter, entryGeorge, entryAnn));
    }

    private ChangeLogSet.Entry mockEntry(String userName) {
        User user = mockUser(userName);
        when(user.getFullName()).thenReturn(userName);

        ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        when(entry.getAuthor()).thenReturn(user);

        return entry;
    }

    public static User mockUser(String userName) {
        User user = mock(User.class);
        when(user.toString()).thenReturn(userName);
        return user;
    }

    public static <T> Answer<T> createAnswer(T value) {
        return (invocation -> value);
    }

    private class File implements ChangeLogSet.AffectedFile {

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public EditType getEditType() {
            return null;
        }
    }
}
