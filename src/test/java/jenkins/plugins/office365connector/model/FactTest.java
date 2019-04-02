package jenkins.plugins.office365connector.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactTest {

    @Test
    public void getName_ReturnsName() {

        // given
        String name = "myFact";
        Fact fact = new Fact(name, "someValue");

        // when & then
        assertThat(fact.getName()).isEqualTo(name);
    }

    @Test
    public void getValue_ReturnsValue() {

        // given
        String value = "myValue";
        Fact fact = new Fact("myName", value);

        // when & then
        assertThat(fact.getValue()).isEqualTo(value);
    }
}
