package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import jenkins.plugins.office365connector.model.Macro;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookTest {

    @Test
    public void getUrl_ReturnsUrl() {

        // given
        String url = "myUrl";
        Webhook webhook = new Webhook(url);

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl).isEqualTo(url);
    }

    @Test
    public void getName_ReturnsName() {

        // given
        String name = "myName";
        Webhook webhook = new Webhook("someUrl");
        webhook.setName(name);

        // when
        String actualName = webhook.getName();

        // then
        assertThat(actualName).isEqualTo(name);
    }

    @Test
    public void isNotifySuccess_CheckSuccessNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean result = webhook.isNotifySuccess();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isStartNotification_CheckStartNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setStartNotification(true);

        // when
        boolean result = webhook.isStartNotification();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyAborted_CheckAbortedNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean result = webhook.isNotifyAborted();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyNotBuilt_CheckNotBuiltNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean result = webhook.isNotifyNotBuilt();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyUnstable_CheckUnstableNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean result = webhook.isNotifyUnstable();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyFailure_CheckFailureNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean result = webhook.isNotifyFailure();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyBackToNormal_CheckBackToNormalNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean result = webhook.isNotifyBackToNormal();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotifyRepeatedFailure_CheckRepeatedFailureNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean result = webhook.isNotifyRepeatedFailure();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void getTimeout_ReturnsTimeout() {

        // given
        Webhook webhook = new Webhook("someUrl");
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
        Webhook webhook = new Webhook("someUrl");

        // when
        int actualTimeout = webhook.getTimeout();

        // then
        assertThat(actualTimeout).isEqualTo(Webhook.DEFAULT_TIMEOUT);
    }

    @Test
    public void getMacros_ReturnsMacros() {

        // given
        Webhook webhook = new Webhook("someUrl");
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
        Webhook webhook = new Webhook("someUrl");

        // when
        List<Macro> macros = webhook.getMacros();

        // then
        assertThat(macros).isEmpty();
    }
}
