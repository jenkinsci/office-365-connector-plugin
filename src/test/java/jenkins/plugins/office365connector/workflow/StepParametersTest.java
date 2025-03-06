package jenkins.plugins.office365connector.workflow;

import jenkins.plugins.office365connector.model.FactDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class StepParametersTest {

    private static final String MESSAGE = "myMessage";
    private static final String WEBHOOK_URL = "www.my.host";
    private static final String STATUS = "myStatus";
    private static final FactDefinition FACT_DEFINITION = new FactDefinition("I'm", "developer");
    private static final String COLOR = "myColor";

    private StepParameters stepParameters;

    @BeforeEach
    void setUp() {
        stepParameters = new StepParameters(
                MESSAGE, WEBHOOK_URL, STATUS, Collections.singletonList(FACT_DEFINITION), COLOR, false);
    }

    @Test
    void getMessage_ReturnsMessage() {

        // given from @Before

        // when
        String message = stepParameters.getMessage();

        // then
        assertThat(message, equalTo(MESSAGE));
    }

    @Test
    void getUrl_ReturnsUrl() {

        // given from @Before

        // when
        String url = stepParameters.getWebhookUrl();

        // then
        assertThat(url, equalTo(WEBHOOK_URL));
    }

    @Test
    void getStatus_ReturnsStatus() {

        // given from @Before

        // when
        String status = stepParameters.getStatus();

        // then
        assertThat(status, equalTo(STATUS));
    }

    @Test
    void getFactDefinitions_ReturnsFactDefinitions() {

        // given from @Before

        // when
        List<FactDefinition> factDefinitions = stepParameters.getFactDefinitions();

        // then
        assertThat(factDefinitions, contains(FACT_DEFINITION));
    }

    @Test
    void getColor_ReturnsColor() {

        // given from @Before

        // when
        String color = stepParameters.getColor();

        // then
        assertThat(color, equalTo(COLOR));
    }
}
