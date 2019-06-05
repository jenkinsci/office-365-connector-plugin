package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.List;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.AbstractBuild;
import hudson.util.Secret;
import jenkins.plugins.office365connector.model.Macro;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class, CredentialsProvider.class})
public class WebhookTest {

    @Test
    public void getUrl_ReturnsUrl() {
        AbstractBuild run = mock(AbstractBuild.class);

        // given
        String url = "myUrl";
        Webhook webhook = new Webhook();
        webhook.setUrl(url);

        // when
        String actualUrl = webhook.getUrl(run);

        // then
        assertThat(actualUrl).isEqualTo(url);
    }

    @Test
    public void getUrl_ReturnsUrlCredential() {
        AbstractBuild run = mock(AbstractBuild.class);

        // given
        String url = "myUrl";
        String urlId = "myUrlId";
        Secret secret = mock(Secret.class);
        mockStatic(Secret.class);
        when(Secret.toString(secret)).thenReturn(url);

        StringCredentials urlCred = new StringCredentialsImpl(CredentialsScope.GLOBAL, urlId, "Credential", secret);
        mockStatic(CredentialsProvider.class);
        when(CredentialsProvider.findCredentialById(urlId, StringCredentials.class, run)).thenReturn(urlCred);

        Webhook webhook = new Webhook();
        webhook.setUrlCredentialsId(urlId);

        // when
        String actualUrl = webhook.getUrl(run);

        // then
        assertThat(actualUrl).isEqualTo(url);
    }

    @Test
    public void getName_ReturnsName() {

        // given
        String name = "myName";
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setName(name);

        // when
        String actualName = webhook.getName();

        // then
        assertThat(actualName).isEqualTo(name);
    }

    @Test
    public void isNotifySuccess_CheckSuccessNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean result = webhook.isNotifySuccess();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isStartNotification_CheckStartNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setStartNotification(true);

        // when
        boolean result = webhook.isStartNotification();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyAborted_CheckAbortedNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean result = webhook.isNotifyAborted();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyNotBuilt_CheckNotBuiltNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean result = webhook.isNotifyNotBuilt();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyUnstable_CheckUnstableNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean result = webhook.isNotifyUnstable();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyFailure_CheckFailureNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean result = webhook.isNotifyFailure();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyBackToNormal_CheckBackToNormalNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean result = webhook.isNotifyBackToNormal();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyRepeatedFailure_CheckRepeatedFailureNotification() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean result = webhook.isNotifyRepeatedFailure();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void getTimeout_ReturnsTimeout() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        int timeout = 1234;
        webhook.setTimeout(timeout);

        // when
        int actualTimeout = webhook.getTimeout();

        // then
        assertThat(actualTimeout).isEqualTo(timeout);
    }

    @Test
    public void getTimeout_ReturnsDefaultTimeout() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");

        // when
        int actualTimeout = webhook.getTimeout();

        // then
        assertThat(actualTimeout).isEqualTo(Webhook.DEFAULT_TIMEOUT);
    }

    @Test
    public void getMacros_ReturnsMacros() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");
        Macro macro = new Macro("myTemplate", "yourValue");
        webhook.setMacros(Arrays.asList(macro));

        // when
        List<Macro> macros = webhook.getMacros();

        // then
        assertThat(macros).hasSize(1).containsOnly(macro);
    }

    @Test
    public void getMacros_ReturnEmptyList() {

        // given
        Webhook webhook = new Webhook();
        webhook.setUrl("someUrl");

        // when
        List<Macro> macros = webhook.getMacros();

        // then
        assertThat(macros).isEmpty();
    }
}
