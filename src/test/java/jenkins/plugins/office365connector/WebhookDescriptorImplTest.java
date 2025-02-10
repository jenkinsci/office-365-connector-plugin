package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;

import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.MockedStatic;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookDescriptorImplTest {

    private final WebhookStub.DescriptorImplStub descriptor = new WebhookStub.DescriptorImplStub();

    private MockedStatic<Jenkins> staticJenkins;

    @Before
    public void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        File rootDir = new File(".");
        when(jenkins.getRootDir()).thenReturn(rootDir);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
    }

    @After
    public void tearDown() {
        staticJenkins.close();
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
        StaplerRequest2 staplerRequest = mock(StaplerRequest2.class);
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
