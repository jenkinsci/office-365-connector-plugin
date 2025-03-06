package jenkins.plugins.office365connector;

import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.MockedStatic;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class WebhookDescriptorImplTest {

    private final WebhookStub.DescriptorImplStub descriptor = new WebhookStub.DescriptorImplStub();

    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        File rootDir = new File(".");
        when(jenkins.getRootDir()).thenReturn(rootDir);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
    }

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    @Test
    void getDisplayName_ReturnsName() {

        // given & when
        String displayName = descriptor.getDisplayName();

        // then
        assertThat(displayName, equalTo("Webhook"));
    }

    @Test
    void getDefaultTimeout_ReturnsDefaultTimeout() {

        // given & when
        int timeout = descriptor.getDefaultTimeout();

        // then
        assertThat(timeout, equalTo(Webhook.DEFAULT_TIMEOUT));
    }

    @Test
    void doCheckUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";

        // when
        FormValidation result = descriptor.doCheckUrl(validUrl);

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void getName_ReturnsName() {

        // given
        String name = "test";

        // when
        descriptor.setName(name);

        // then
        assertThat(descriptor.getName(), equalTo(name));
    }

    @Test
    void getUrl_ReturnsUrl() {

        // given
        String url = "test.com";

        // when
        descriptor.setUrl(url);

        // then
        assertThat(descriptor.getUrl(), equalTo(url));
    }

    @Test
    void configure_ReturnsTrue() {

        // given
        StaplerRequest2 staplerRequest = mock(StaplerRequest2.class);
        when(staplerRequest.bindJSON(any(), any())).thenReturn("");

        // when
        boolean isConfigured = descriptor.configure(staplerRequest, null);

        // then
        assertThat(isConfigured, is(true));
    }

    @Test
    void doCheckGlobalUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";

        // when
        FormValidation result = descriptor.doCheckGlobalUrl(validUrl);

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckGlobalUrl_ValidatesUrl_WhenBlank() {

        // given
        String validUrl = "";

        // when
        FormValidation result = descriptor.doCheckGlobalUrl(validUrl);

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }
}
