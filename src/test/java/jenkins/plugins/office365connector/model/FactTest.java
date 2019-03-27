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
        Fact fact = new Fact(name);

        // when & then
        assertThat(fact.getName()).isEqualTo(name);
    }

    @Test
    public void setName_ChangesName() {

        // given
        String name = "myName";
        Fact fact = new Fact("defaultName", "0");

        // when
        fact.setName(name);

        // then
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

    @Test
    public void setValue_OnString_ChangesValue() {

        // given
        String value = "myValue";
        Fact fact = new Fact("myName", "defaultValue");

        // when
        fact.setValue(value);

        // then
        assertThat(fact.getValue()).isEqualTo(value);
    }


    @Test
    public void setValue_OnNumber_ChangesValue() {

        // given
        int value = 100;
        Fact fact = new Fact("myName", "defaultValue");

        // when
        fact.setValue(value);

        // then
        assertThat(fact.getValue()).isEqualTo(String.valueOf(value));
    }
}
