package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class SectionTest {

    private static final String ACTIVITY_TITLE = "myActivityTitle";
    private static final String ACTIVITY_SUBTITLE = "myActivitySubtitle";
    private static final List<Fact> FACTS = Arrays.asList(new Fact("firstName", "firstValue"), new Fact("secondName", "secondValue"));

    @Test
    void getMarkdown_ReturnsTrue() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        boolean markdown = section.getMarkdown();

        // then
        assertThat(markdown, is(true));
    }

    @Test
    void getActivityTitle_ReturnsActivityTitle() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        String activityTitle = section.getActivityTitle();

        // then
        assertThat(activityTitle, equalTo(ACTIVITY_TITLE));
    }

    @Test
    void getFacts_ReturnsFacts() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        List<Fact> facts = section.getFacts();

        // then
        assertThat(facts, equalTo(FACTS));
    }

    @Test
    void getActivitySubtitle_ReturnsActivitySubtitle() {

        // given
        Section section = new Section(ACTIVITY_TITLE, ACTIVITY_SUBTITLE, FACTS);

        // when
        String activitySubtitle = section.getActivitySubtitle();

        // then
        assertThat(activitySubtitle, equalTo(ACTIVITY_SUBTITLE));
    }
}
