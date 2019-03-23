package jenkins.plugins.office365connector.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public final class CardTest {

    @Test
    public void getSummary_ReturnsSummary() {

        // given
        String summary = "mySummary";

        // when
        Card card = new Card(summary, null);

        // then
        assertThat(card.getSummary()).isEqualTo(summary);
    }

    @Test
    public void getSections_ReturnsSection() {

        // given
        Section section = new Section("myTitle", null, null);

        // when
        Card card = new Card(null, section);

        // then
        assertThat(card.getSections()).hasSize(1).containsOnly(section);
    }

    @Test
    public void getThemeColor_ReturnsThemeColor() {

        // given
        String themeColor = "red";
        Card card = new Card("mySummary", null);

        // when
        card.setThemeColor(themeColor);

        // then
        assertThat(card.getThemeColor()).isEqualTo(themeColor);
    }

    @Test
    public void getPotentialAction_ReturnsPotentialActions() {

        // given
        PotentialAction action = new PotentialAction("myName", Collections.singletonList("someUrl"));
        Card card = new Card("mySummary", null);

        // when
        card.setPotentialAction(Collections.singletonList(action));

        // then
        assertThat(card.getPotentialAction()).hasSize(1).containsOnly(action);
    }
}
