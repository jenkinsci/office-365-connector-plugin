package jenkins.plugins.office365connector.model.messagecard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class PotentialActionTest {

    @Test
    public void getName_ReturnsName() {

        // given
        final String name = "myName";
        PotentialAction potentialAction = new PotentialAction(name, "url");

        // then
        String returnedName = potentialAction.getName();

        // then
        assertThat(returnedName).isEqualTo(name);
    }

    @Test
    public void setName_ChangesName() {

        // given
        final String name = "myName";
        PotentialAction potentialAction = new PotentialAction("yourName", Arrays.asList("url"));

        // then
        potentialAction.setName(name);

        // then
        assertThat(potentialAction.getName()).isEqualTo(name);
    }

    @Test
    public void getTarget_ReturnsTarget() {

        // given
        final List<String> target = Arrays.asList("targetOne", "target2");
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setTarget(target);

        // then
        assertThat(potentialAction.getTarget()).isEqualTo(target);
    }

    @Test
    public void getContext_ReturnsContext() {

        // given
        final String context = "myContext";
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setContext(context);

        // then
        assertThat(potentialAction.getContext()).isEqualTo(context);
    }

    @Test
    public void getType_ReturnsType() {

        // given
        final String type = "myType";
        PotentialAction potentialAction = new PotentialAction("name", "url");

        // then
        potentialAction.setType(type);

        // then
        assertThat(potentialAction.getType()).isEqualTo(type);
    }
}
