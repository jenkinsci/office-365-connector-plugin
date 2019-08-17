package jenkins.plugins.office365connector.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactDefinitionTest {

    @Test
    public void FactDefinition_TrimsNullValues() {

        // given
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        String name = factDefinition.getName();
        String template = factDefinition.getTemplate();

        // then
        assertThat(name).isNotNull();
        assertThat(template).isNotNull();
    }

    @Test
    public void setName_SetsName() {

        // given
        String name = "superName";
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        factDefinition.setName(name);

        // then
        assertThat(factDefinition.getName()).isEqualTo(name);
    }

    @Test
    public void setTemplate_SetsTemplate() {

        // given
        String temaplate = "coolTemplate";
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        factDefinition.setTemplate(temaplate);

        // then
        assertThat(factDefinition.getTemplate()).isEqualTo(temaplate);
    }

    @Test
    public void getDisplayName_ReturnsName() {

        // given
        FactDefinition.DescriptorImpl descriptor = new FactDefinition.DescriptorImpl();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name).isEqualTo("FactDefinition");
    }
}
