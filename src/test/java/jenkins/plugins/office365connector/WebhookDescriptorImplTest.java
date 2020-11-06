package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.util.FormValidation;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookDescriptorImplTest {

    private final WebhookStub.DescriptorImplStub descriptor = new WebhookStub.DescriptorImplStub();

    @Test
    public void getDisplayName_ReturnsName() {

        // given & when
        String displayName = descriptor.getDisplayName();

        // then
        assertThat(displayName).isEqualTo("Webhook");
    }

    @Test
    public void getDefaultTimeout_ReturnsDefaultTimeout() {

        // given & when
        int timeout = descriptor.getDefaultTimeout();

        // then
        assertThat(timeout).isEqualTo(Webhook.DEFAULT_TIMEOUT);
    }

    @Test
    public void doCheckUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";

        // when
        FormValidation result = descriptor.doCheckUrl(validUrl);

        // then
        assertThat(result).isEqualTo(FormValidation.ok());
    }
}
