package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
        assertThat(m.getId(), equalTo(""));
        assertThat(m.getName(), equalTo(""));
    }

    @Test
    void setId_SetsIdCorrectly() {
        // given
        Mentioned m = new Mentioned();

        // when
        m.setId("tester.testing@test.com");

        // then
        assertThat(m.getId(), equalTo("tester.testing@test.com"));
    }

    @Test
    void setName_SetsNameCorrectly() {
        // given
        Mentioned m = new Mentioned();

        // when
        m.setName("tester testing");

        // then
        assertThat(m.getName(), equalTo("tester testing"));
    }

    @Test
    void setId_AllowsNull_ConvertsToEmptyString() {
        // given
        Mentioned m = new Mentioned();

        // when
        m.setId(null);

        // then
        assertThat(m.getId(), equalTo(""));
    }

    @Test
    void setName_AllowsNull_ConvertsToEmptyString() {
        // given
        Mentioned m = new Mentioned();

        // when
        m.setName(null);

        // then
        assertThat(m.getName(), equalTo(""));
    }
}
