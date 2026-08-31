package jenkins.plugins.office365connector;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.MockedStatic;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true);
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
        FormValidation result = descriptor.doCheckUrl(null, validUrl, "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrl_WithCredentialsId_ReturnsOk() {

        // given & when
        FormValidation result = descriptor.doCheckUrl(null, "", "my-credential-id");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrl_WithBothUrlAndCredentialsId_ReturnsWarning() {

        // given & when
        FormValidation result = descriptor.doCheckUrl(null, "http://myJenkins.abc", "my-credential-id");

        // then
        assertThat(result.kind, equalTo(FormValidation.Kind.WARNING));
    }

    @Test
    void doCheckUrl_WithNoPermission_ReturnsOk() {

        // given
        staticJenkins.close();
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        when(jenkins.getRootDir()).thenReturn(new File("."));
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        // when
        FormValidation result = descriptor.doCheckUrl(null, "invalid-url", "");

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

    @Test
    void doCheckUrl_WithItem_ValidatesUrl() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckUrl(item, "http://myJenkins.abc", "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrl_WithItem_AndBothUrlAndCredentialsId_ReturnsWarning() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckUrl(item, "http://myJenkins.abc", "my-cred");

        // then
        assertThat(result.kind, equalTo(FormValidation.Kind.WARNING));
    }

    @Test
    void doCheckUrl_WithItem_AndNoPermission_ReturnsOk() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(false);

        // when
        FormValidation result = descriptor.doCheckUrl(item, "invalid-url", "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doFillUrlCredentialIdItems_WithNullItem_AndAdminPermission_ReturnsEntries() {

        // when
        ListBoxModel result = descriptor.doFillUrlCredentialIdItems(null, "");

        // then — should at least have the empty value
        assertThat(result.size(), greaterThan(0));
    }

    @Test
    void doFillUrlCredentialIdItems_WithNullItem_AndNoPermission_ReturnsCurrentValue() {

        // given
        staticJenkins.close();
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        when(jenkins.getRootDir()).thenReturn(new File("."));
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        // when
        ListBoxModel result = descriptor.doFillUrlCredentialIdItems(null, "existing-id");

        // then
        assertThat(result.stream().anyMatch(o -> "existing-id".equals(o.value)), equalTo(true));
    }

    @Test
    void doFillUrlCredentialIdItems_WithItem_AndPermission_ReturnsEntries() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.EXTENDED_READ)).thenReturn(true);

        // when
        ListBoxModel result = descriptor.doFillUrlCredentialIdItems(item, "");

        // then — should at least have the empty value
        assertThat(result.size(), greaterThan(0));
    }

    @Test
    void doFillUrlCredentialIdItems_WithItem_AndNoPermission_ReturnsCurrentValue() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.EXTENDED_READ)).thenReturn(false);
        when(item.hasPermission(CredentialsProvider.USE_ITEM)).thenReturn(false);

        // when
        ListBoxModel result = descriptor.doFillUrlCredentialIdItems(item, "existing-id");

        // then
        assertThat(result.stream().anyMatch(o -> "existing-id".equals(o.value)), equalTo(true));
    }

    @Test
    void doCheckUrlCredentialId_WithNullItem_AndAdminPermission_ReturnsOk() {

        // when
        FormValidation result = descriptor.doCheckUrlCredentialId(null, "some-id");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrlCredentialId_WithNullItem_AndNoPermission_ReturnsOk() {

        // given
        staticJenkins.close();
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        when(jenkins.getRootDir()).thenReturn(new File("."));
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        // when
        FormValidation result = descriptor.doCheckUrlCredentialId(null, "some-id");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrlCredentialId_WithItem_AndPermission_ReturnsOk() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.EXTENDED_READ)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckUrlCredentialId(item, "some-id");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrlCredentialId_WithItem_AndNoPermission_ReturnsOk() {

        // given
        Item item = mock(Item.class);
        when(item.hasPermission(Item.EXTENDED_READ)).thenReturn(false);
        when(item.hasPermission(CredentialsProvider.USE_ITEM)).thenReturn(false);

        // when
        FormValidation result = descriptor.doCheckUrlCredentialId(item, "some-id");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrlCredentialId_WithBlankValue_ReturnsOk() {

        // when
        FormValidation result = descriptor.doCheckUrlCredentialId(null, "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }
}
