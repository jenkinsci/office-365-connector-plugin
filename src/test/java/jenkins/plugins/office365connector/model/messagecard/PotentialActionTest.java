package jenkins.plugins.office365connector.model.messagecard;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class PotentialActionTest {

    @Test
    void getName_ReturnsName() {

        // given
        final String name = "myName";
        PotentialAction potentialAction = new PotentialAction(name, "url");

        // then
        String returnedName = potentialAction.getName();

        // then
        assertThat(returnedName, equalTo(name));
    }

    @Test
    void setName_ChangesName() {

        // given
        final String name = "myName";
        PotentialAction potentialAction = new PotentialAction("yourName", List.of("url"));

        // then
        potentialAction.setName(name);

        // then
        assertThat(potentialAction.getName(), equalTo(name));
    }

    @Test
    void getTarget_ReturnsTarget() {

        // given
        final List<String> target = Arrays.asList("targetOne", "target2");
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setTargets(target);

        // then
        assertThat(potentialAction.getTarget(), equalTo(target));
    }

    @Test
    void getContext_ReturnsContext() {

        // given
        final String context = "myContext";
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setContext(context);

        // then
        assertThat(potentialAction.getContext(), equalTo(context));
    }

    @Test
    void getType_ReturnsType() {

        // given
        final String type = "myType";
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setType(type);

        // then
        assertThat(potentialAction.getType(), equalTo(type));
    }
}
