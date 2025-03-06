package jenkins.plugins.office365connector.utils;

import hudson.util.FormValidation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FormUtilsTest {

    private static final FormValidation.Kind OK = FormValidation.Kind.OK;

    private static final FormValidation.Kind ERROR = FormValidation.Kind.ERROR;

    static Stream<Object[]> data() {
        return Stream.of(
                new Object[] {"", ERROR, false},
                new Object[] {"demo", ERROR, false},
                new Object[] {"hpp://demo", ERROR, false},
                new Object[] {"http://demo", ERROR, false},
                /* Still need to figure a way out with this*/// { "$$$$$demo", ERROR, false },
                new Object[] {"$", ERROR, false},
                new Object[] {"$demo", OK, true},
                new Object[] {"https://demo.com", OK, true}
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void isUrlValid_ValidatesUrl(String input, FormValidation.Kind expectedKind, boolean expectedBoolean) {

        // given
        FormValidation formValidation = FormUtils.formValidateUrl(input);
        FormValidation.Kind kind = formValidation.kind;

        // when
        boolean output = FormUtils.isUrlValid(input);

        // then
        assertThat(kind, equalTo(expectedKind));
        assertThat(output, equalTo(expectedBoolean));
    }
}
