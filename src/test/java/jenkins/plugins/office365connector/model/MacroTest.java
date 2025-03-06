package jenkins.plugins.office365connector.model;

import hudson.model.Descriptor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class MacroTest {

    @Test
    void newMacro_StoresPassedValues() {

        // given
        String template = "Michael";
        String value = "Jackson";

        // when
        Macro macro = new Macro(template, value);

        // then
        assertThat(macro.getTemplate(), equalTo(template));
        assertThat(macro.getValue(), equalTo(value));
    }

    @Test
    void newMacro_OnNullValue_StoresEmptyString() {

        // given
        String template = null;
        String value = null;

        // when
        Macro macro = new Macro(template, value);

        // then
        assertThat(macro.getTemplate(), emptyString());
        assertThat(macro.getValue(), emptyString());
    }

    @Test
    void getDisplayName_ReturnsName() {

        // given
        Descriptor<Macro> descriptor = new Macro.DescriptorImpl();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name, equalTo("Macro"));
    }
}
