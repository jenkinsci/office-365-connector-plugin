package jenkins.plugins.office365connector.model;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.Descriptor;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class MacroTest {

    @Test
    public void newMacro_StoresPassedValues() {

        // given
        String template = "Michael";
        String value = "Jackson";

        // when
        Macro macro = new Macro(template, value);

        // then
        assertThat(macro.getTemplate()).isEqualTo(template);
        assertThat(macro.getValue()).isEqualTo(value);
    }

    @Test
    public void newMacro_OnNullValue_StoresEmptyString() {

        // given
        String template = null;
        String value = null;

        // when
        Macro macro = new Macro(template, value);

        // then
        assertThat(macro.getTemplate()).isEmpty();
        assertThat(macro.getValue()).isEmpty();
    }

    @Test
    public void getDisplayName_ReturnsName() {

        // given
        Descriptor<Macro> descriptor = new Macro.DescriptorImpl();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name).isEqualTo("Macro");
    }
}
