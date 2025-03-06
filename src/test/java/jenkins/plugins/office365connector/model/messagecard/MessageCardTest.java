package jenkins.plugins.office365connector.model.messagecard;

import jenkins.plugins.office365connector.model.Section;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class MessageCardTest {

    @Test
    void getSummary_ReturnsSummary() {

        // given
        String summary = "mySummary";

        // when
        MessageCard messageCard = new MessageCard(summary, null);

        // then
        assertThat(messageCard.getSummary(), equalTo(summary));
    }

    @Test
    void getSections_ReturnsSection() {

        // given
        Section section = new Section("myTitle", null, null);

        // when
        MessageCard messageCard = new MessageCard(null, section);

        // then
        assertThat(messageCard.getSections(), hasSize(1));
        assertThat(messageCard.getSections(), contains(section));
    }

    @Test
    void getThemeColor_ReturnsThemeColor() {

        // given
        String themeColor = "red";
        MessageCard messageCard = new MessageCard("mySummary", null);

        // when
        messageCard.setThemeColor(themeColor);

        // then
        assertThat(messageCard.getThemeColor(), equalTo(themeColor));
    }

    @Test
    void getPotentialAction_ReturnsPotentialActions() {

        // given
        PotentialAction action = new PotentialAction("myName", Collections.singletonList("someUrl"));
        MessageCard messageCard = new MessageCard("mySummary", null);

        // when
        messageCard.setAction(Collections.singletonList(action));

        // then
        assertThat(messageCard.getAction(), hasSize(1));
        assertThat(messageCard.getAction(), contains(action));
    }
}
