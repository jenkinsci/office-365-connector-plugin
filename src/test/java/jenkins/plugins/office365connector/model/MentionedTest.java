package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Tests for Mentioned class.
 * @author Ammar Zain (AmmarOFA@github)
 */
class MentionedTest {

    @Test
    void defaultConstructor_SetsFieldsToEmptyString() {
        // given
        Mentioned m = new Mentioned();

        // then
        assertThat(m.getId(), isEmptyString());
        assertThat(m.getName(), isEmptyString());
    }

    @Test
    void setId_SetsIdCorrectly() {
        // given
        Mentioned m = new Mentioned();

        // when
        String id = "tester.testing@test.com";
        m.setId(id);

        // then
        assertThat(m.getId(), equalTo(id));
    }

    @Test
    void setName_SetsNameCorrectly() {
        // given
        Mentioned m = new Mentioned();

        String name = "tester testing";
        m.setName(name);

        // then
        assertThat(m.getName(), equalTo(name));
    }
}
