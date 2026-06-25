package jenkins.plugins.office365connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Run;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Tests for {@link Webhook#resolveUrl(Run)}.
 */
class WebhookResolveUrlTest {

    private MockedStatic<Jenkins> staticJenkins;
    private MockedStatic<CredentialsProvider> staticCredentials;

    @AfterEach
    void tearDown() {
        if (staticJenkins != null) {
            staticJenkins.close();
        }
        if (staticCredentials != null) {
            staticCredentials.close();
        }
    }

    private Webhook createWebhook(String url) {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(mockDescriptor.getGlobalUrl()).thenReturn("");
        when(mockDescriptor.getGlobalName()).thenReturn("");
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        return new Webhook(url);
    }

    @Test
    void resolveUrl_WithCredentialsId_ReturnsSecretValue() {
        // given
        Webhook webhook = createWebhook("");
        webhook.setCredentialsId("my-credentials-id");
        webhook.setName("Test Webhook");

        Run<?, ?> run = mock(Run.class);
        StringCredentials credentials = mock(StringCredentials.class);
        Secret secret = Secret.fromString("https://outlook.office.com/webhook/secret-url");
        when(credentials.getSecret()).thenReturn(secret);

        staticCredentials = mockStatic(CredentialsProvider.class);
        staticCredentials.when(() -> CredentialsProvider.findCredentialById(
                "my-credentials-id",
                StringCredentials.class,
                run
        )).thenReturn(credentials);

        // when
        String resolvedUrl = webhook.resolveUrl(run);

        // then
        assertThat(resolvedUrl, equalTo("https://outlook.office.com/webhook/secret-url"));
    }

    @Test
    void resolveUrl_WithUrlOnly_ReturnsUrl() {
        // given
        String url = "https://outlook.office.com/webhook/plain-url";
        Webhook webhook = createWebhook(url);

        Run<?, ?> run = mock(Run.class);

        // when
        String resolvedUrl = webhook.resolveUrl(run);

        // then
        assertThat(resolvedUrl, equalTo(url));
    }

    @Test
    void resolveUrl_WithBothCredentialsAndUrl_CredentialsTakePrecedence() {
        // given
        Webhook webhook = createWebhook("https://outlook.office.com/webhook/plain-url");
        webhook.setCredentialsId("my-credentials-id");
        webhook.setName("Test Webhook");

        Run<?, ?> run = mock(Run.class);
        StringCredentials credentials = mock(StringCredentials.class);
        Secret secret = Secret.fromString("https://outlook.office.com/webhook/secret-url");
        when(credentials.getSecret()).thenReturn(secret);

        staticCredentials = mockStatic(CredentialsProvider.class);
        staticCredentials.when(() -> CredentialsProvider.findCredentialById(
                "my-credentials-id",
                StringCredentials.class,
                run
        )).thenReturn(credentials);

        // when
        String resolvedUrl = webhook.resolveUrl(run);

        // then
        assertThat(resolvedUrl, equalTo("https://outlook.office.com/webhook/secret-url"));
    }

    @Test
    void resolveUrl_WithCredentialsIdNotFound_ThrowsException() {
        // given
        Webhook webhook = createWebhook("");
        webhook.setCredentialsId("nonexistent-id");
        webhook.setName("Test Webhook");

        Run<?, ?> run = mock(Run.class);

        staticCredentials = mockStatic(CredentialsProvider.class);
        staticCredentials.when(() -> CredentialsProvider.findCredentialById(
                "nonexistent-id",
                StringCredentials.class,
                run
        )).thenReturn(null);

        // when/then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> webhook.resolveUrl(run));
        assertThat(exception.getMessage(), equalTo("Could not find credentials with id 'nonexistent-id' for webhook 'Test Webhook'"));
    }

    @Test
    void resolveUrl_WithNeitherUrlNorCredentials_ThrowsException() {
        // given
        Webhook webhook = createWebhook("");
        webhook.setName("Test Webhook");

        Run<?, ?> run = mock(Run.class);

        // when/then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> webhook.resolveUrl(run));
        assertThat(exception.getMessage(), equalTo("No URL or credentialsId configured for webhook 'Test Webhook'"));
    }
}
