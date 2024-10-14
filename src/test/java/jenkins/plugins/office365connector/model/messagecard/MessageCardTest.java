package jenkins.plugins.office365connector.model.messagecard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import jenkins.plugins.office365connector.model.Section;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class MessageCardTest {

    @Test
    public void getSummary_ReturnsSummary() {

        // given
        String summary = "mySummary";

        // when
        MessageCard messageCard = new MessageCard(summary, null);

        // then
        assertThat(messageCard.getSummary()).isEqualTo(summary);
    }

    @Test
    public void getSections_ReturnsSection() {

        // given
        Section section = new Section("myTitle", null, null);

        // when
        MessageCard messageCard = new MessageCard(null, section);

        // then
        assertThat(messageCard.getSections()).hasSize(1).containsOnly(section);
    }

    @Test
    public void getThemeColor_ReturnsThemeColor() {

        // given
        String themeColor = "red";
        MessageCard messageCard = new MessageCard("mySummary", null);

        // when
        messageCard.setThemeColor(themeColor);

        // then
        assertThat(messageCard.getThemeColor()).isEqualTo(themeColor);
    }

    @Test
    public void getPotentialAction_ReturnsPotentialActions() {

        // given
        PotentialAction action = new PotentialAction("myName", Collections.singletonList("someUrl"));
        MessageCard messageCard = new MessageCard("mySummary", null);

        // when
        messageCard.setAction(Collections.singletonList(action));

        // then
        assertThat(messageCard.getAction()).hasSize(1).containsOnly(action);
    }
}
