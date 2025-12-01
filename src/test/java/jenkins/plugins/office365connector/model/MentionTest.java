package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Strict tests for Mention class (outer entity for Adaptive Card mentions).
 * @author Ammar Zain (AmmarOFA@github)
 */
class MentionTest {

    @Test
    void defaultConstructor_FieldsAreNull() {
        // given / when
        Mention mention = new Mention();

        // then
        assertThat(mention.getText(), equalTo(null));
        assertThat(mention.getMentioned(), equalTo(null));
    }

    @Test
    void setText_SetsExactText() {
        // given
        Mention mention = new Mention();

        // when
        mention.setText("<at>Tester Testing</at>");

        // then
        assertThat(mention.getText(), equalTo("<at>Tester Testing</at>"));
    }

    @Test
    void setMentioned_SetsExactMentioned() {
        // given
        Mention mention = new Mention();
        Mentioned mentioned = new Mentioned();
        mentioned.setId("tester.testing@test.com");
        mentioned.setName("Tester Testing");

        // when
        mention.setMentioned(mentioned);

        // then
        assertThat(mention.getMentioned(), equalTo(mentioned));
        assertThat(mention.getMentioned().getId(), equalTo("tester.testing@test.com"));
        assertThat(mention.getMentioned().getName(), equalTo("Tester Testing"));
    }

    @Test
    void fromMentioned_CreatesMentionWithExactValues() {
        // given
        Mentioned mentioned = new Mentioned();
        mentioned.setId("tester.testing@test.com");
        mentioned.setName("Tester Testing");

        // when
        Mention mention = Mention.fromMentioned(mentioned);

        // then
        assertThat(mention.getMentioned(), equalTo(mentioned));
        assertThat(mention.getText(), equalTo("<at>Tester Testing</at>"));
    }
}