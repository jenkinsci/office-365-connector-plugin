package jenkins.plugins.office365connector.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class FactDefinitionTest {

    @Test
    void FactDefinition_TrimsNullValues() {

        // given
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        String name = factDefinition.getName();
        String template = factDefinition.getTemplate();

        // then
        assertThat(name, notNullValue());
        assertThat(template, notNullValue());
    }

    @Test
    void setName_SetsName() {

        // given
        String name = "superName";
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        factDefinition.setName(name);

        // then
        assertThat(factDefinition.getName(), equalTo(name));
    }

    @Test
    void setTemplate_SetsTemplate() {

        // given
        String temaplate = "coolTemplate";
        FactDefinition factDefinition = new FactDefinition(null, null);

        // when
        factDefinition.setTemplate(temaplate);

        // then
        assertThat(factDefinition.getTemplate(), equalTo(temaplate));
    }

    @Test
    void getDisplayName_ReturnsName() {

        // given
        FactDefinition.DescriptorImpl descriptor = new FactDefinition.DescriptorImpl();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name, equalTo("FactDefinition"));
    }
}
