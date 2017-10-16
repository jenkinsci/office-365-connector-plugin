package jenkins.plugins.office365connector;

import java.util.Arrays;
import java.util.Collection;

import hudson.util.FormValidation;
import jenkins.plugins.office365connector.util.FormUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ValidateUrlTest {

    private static final FormValidation.Kind OK = FormValidation.Kind.OK;
    private static final FormValidation.Kind ERROR = FormValidation.Kind.ERROR;
    @Parameter()
    public String input;
    @Parameter(value = 1)
    public FormValidation.Kind expectedKind;
    @Parameter(value = 2)
    public boolean expectedBoolean;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"demo", ERROR, false},
                {"hpp://demo", ERROR, false},
                {"http://demo", ERROR, false},
                /* Still need to figure a way out with this*/// { "$$$$$demo", ERROR, false },
                {"$demo", OK, true},
                {"https://demo.com", OK, true},
        });
    }

    @Test
    public void shouldValidateUrlOrVariableReference() {
        FormValidation formValidation = FormUtils.formValidateUrl(input);
        FormValidation.Kind kind = formValidation.kind;
        boolean output = FormUtils.validateUrl(input);
        assertThat(kind).isEqualTo(expectedKind);
        assertThat(output).isEqualTo(expectedBoolean);
    }
}
