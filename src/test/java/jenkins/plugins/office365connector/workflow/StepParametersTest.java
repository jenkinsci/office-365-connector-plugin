package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import jenkins.plugins.office365connector.model.FactDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class StepParametersTest {

    private final static String MESSAGE = "myMessage";
    private final static String WEBHOOK_URL = "www.my.host";
    private final static String STATUS = "myStatus";
    private final static FactDefinition FACT_DEFINITION = new FactDefinition("I'm", "developer");
    private final static String COLOR = "myColor";

    private StepParameters stepParameters;

    @Before
    public void setUp() {
        stepParameters = new StepParameters(
                MESSAGE, WEBHOOK_URL, STATUS, Collections.singletonList(FACT_DEFINITION), COLOR);
    }

    @Test
    public void getMessage_ReturnsMessage() {

        // given from @Before

        // when
        String message = stepParameters.getMessage();

        // then
        assertThat(message).isEqualTo(MESSAGE);
    }

    @Test
    public void getUrl_ReturnsUrl() {

        // given from @Before

        // when
        String url = stepParameters.getWebhookUrl();

        // then
        assertThat(url).isEqualTo(WEBHOOK_URL);
    }

    @Test
    public void getStatus_ReturnsStatus() {

        // given from @Before

        // when
        String status = stepParameters.getStatus();

        // then
        assertThat(status).isEqualTo(STATUS);
    }

    @Test
    public void getColor_ReturnsColor() {

        // given from @Before

        // when
        String color = stepParameters.getColor();

        // then
        assertThat(color).isEqualTo(COLOR);
    }
}
