package jenkins.plugins.office365connector.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import hudson.util.FormValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FormUtilsTest {

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
    public void isUrlValid_ValidatesUrl() {

        // given
        FormValidation formValidation = FormUtils.formValidateUrl(input);
        FormValidation.Kind kind = formValidation.kind;

        // when
        boolean output = FormUtils.isUrlValid(input);

        // then
        assertThat(kind).isEqualTo(expectedKind);
        assertThat(output).isEqualTo(expectedBoolean);
    }
}
