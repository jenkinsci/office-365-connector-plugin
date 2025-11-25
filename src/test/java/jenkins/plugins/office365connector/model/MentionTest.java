
package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Ammar Zain (AmmarOFA@github)
 */
class MentionTest {

    @Test
    void Mention_Constructor_SetsFields() {

        // given
        String id = "tester.testing@test.com";
        String name = "tester testing";

        // when
        Mention mention = new Mention(id, name);

        // then
        assertThat(mention.getId(), equalTo(id));
        assertThat(mention.getName(), equalTo(name));
    }

    @Test
    void Mention_Constructor_AllowsNullValues() {

        // given
        Mention mention = new Mention(null, null);

        // when
        String id = mention.getId();
        String name = mention.getName();

        // then
        assertThat(id, notNullValue()); // Should not throw NullPointerException
        assertThat(name, notNullValue());
    }

    @Test
    void getId_ReturnsId() {

        // given
        String id = "test.id@example.com";
        Mention mention = new Mention(id, "Test Name");

        // when
        String actualId = mention.getId();

        // then
        assertThat(actualId, equalTo(id));
    }

    @Test
    void getName_ReturnsName() {

        // given
        String name = "Test Name";
        Mention mention = new Mention("test.id@example.com", name);

        // when
        String actualName = mention.getName();

        // then
        assertThat(actualName, equalTo(name));
    }
}
