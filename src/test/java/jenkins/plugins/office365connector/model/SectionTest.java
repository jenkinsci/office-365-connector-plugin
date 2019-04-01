package jenkins.plugins.office365connector.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class SectionTest {

    private static final String ACTIVITY_TITLE = "myActivityTitle";
    private static final String ACTIVITY_SUBTITLE = "myActivitySubtitle";
    private static final List<Fact> FACTS = Arrays.asList(new Fact("firstName", "firstValue"), new Fact("secondName", "secondValue"));

    @Test
    public void getMarkdown_ReturnsTrue() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        boolean markdown = section.getMarkdown();

        // then
        assertThat(markdown).isTrue();
    }

    @Test
    public void getActivityTitle_ReturnsActivityTitle() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        String activityTitle = section.getActivityTitle();

        // then
        assertThat(activityTitle).isEqualTo(ACTIVITY_TITLE);
    }

    @Test
    public void getFacts_ReturnsFacts() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        List<Fact> facts = section.getFacts();

        // then
        assertThat(facts).isEqualTo(FACTS);
    }

    @Test
    public void getActivitySubtitle_ReturnsActivitySubtitle() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        String activitySubtitle = section.getActivitySubtitle();

        // then
        assertThat(activitySubtitle).isEqualTo(ACTIVITY_SUBTITLE);
    }
}
