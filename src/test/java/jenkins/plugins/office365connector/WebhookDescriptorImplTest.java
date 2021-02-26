package jenkins.plugins.office365connector;

import java.io.File;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class)
public class WebhookDescriptorImplTest {

    private final WebhookStub.DescriptorImplStub descriptor = new WebhookStub.DescriptorImplStub();

    @Before
    public void setUp() {
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        File rootDir = new File(".");
        when(jenkins.getRootDir()).thenReturn(rootDir);
        when(Jenkins.get()).thenReturn(jenkins);
    }

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

    @Test
    public void getName_ReturnsName() {

        // given
        String name = "test";

        // when
        descriptor.setName(name);

        // then
        assertThat(descriptor.getName()).isEqualTo(name);
    }

    @Test
    public void getUrl_ReturnsUrl() {

        // given
        String url = "test.com";

        // when
        descriptor.setUrl(url);

        // then
        assertThat(descriptor.getUrl()).isEqualTo(url);
    }

    @Test
    public void configure_ReturnsTrue() {

        // given
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(staplerRequest.bindJSON(any(), any())).thenReturn("");

        // when
        boolean isConfigured = descriptor.configure(staplerRequest, null);

        // then
        assertThat(isConfigured).isTrue();
    }

    @Test
    public void doCheckGlobalUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";

        // when
        FormValidation result = descriptor.doCheckGlobalUrl(validUrl);

        // then
        assertThat(result).isEqualTo(FormValidation.ok());
    }

    @Test
    public void doCheckGlobalUrl_ValidatesUrl_WhenBlank() {

        // given
        String validUrl = "";

        // when
        FormValidation result = descriptor.doCheckGlobalUrl(validUrl);

        // then
        assertThat(result).isEqualTo(FormValidation.ok());
    }
}
