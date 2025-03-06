package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class FactTest {

    @Test
    void getName_ReturnsName() {

        // given
        String name = "myFact";
        Fact fact = new Fact(name, "someValue");

        // when & then
        assertThat(fact.getName(), equalTo(name));
    }

    @Test
    void getValue_ReturnsValue() {

        // given
        String value = "myValue";
        Fact fact = new Fact("myName", value);

        // when & then
        assertThat(fact.getValue(), equalTo(value));
    }
}
