package jenkins.plugins.office365connector;

import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.model.Macro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class WebhookTest {

    private MockedStatic<Jenkins> staticJenkins;

    @AfterEach
    void tearDown() {
        if (staticJenkins != null) {
            staticJenkins.close();
        }
    }

    @Test
    void getUrl_ReturnsUrl() {

        // given
        String url = "myUrl";
        Webhook webhook = new Webhook(url);

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl, equalTo(url));
    }

    @Test
    void getUrl_WithEmptyLocalUrlReturnsGlobalUrl() {

        // given
        String globalUrl = "globalUrl";
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(mockDescriptor.getGlobalUrl()).thenReturn(globalUrl);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        Webhook webhook = new Webhook("");

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl, equalTo(globalUrl));
    }

    @Test
    void getUrl_ReturnsLocalUrlAndNotGlobal() {

        // given
        String globalUrl = "globalUrl";
        String localUrl = "localUrl";
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(mockDescriptor.getUrl()).thenReturn(globalUrl);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        Webhook webhook = new Webhook(localUrl);

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl, equalTo(localUrl));
    }

    @Test
    void getName_ReturnsName() {

        // given
        String name = "myName";
        Webhook webhook = new Webhook("someUrl");
        webhook.setName(name);

        // when
        String actualName = webhook.getName();

        // then
        assertThat(actualName, equalTo(name));
    }

    @Test
    void getName_WithEmptyLocalNameReturnsGlobalName() {

        // given
        String globalName = "globalName";
        Webhook webhook = new Webhook("someUrl");
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(mockDescriptor.getGlobalName()).thenReturn(globalName);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);

        // when
        String actualName = webhook.getName();
        // then
        assertThat(actualName, equalTo(globalName));
    }

    @Test
    void getName_ReturnsLocalNameAndNotGlobal() {

        // given
        String localName = "myName";
        Webhook webhook = new Webhook("someUrl");
        webhook.setName(localName);
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(mockDescriptor.getName()).thenReturn("globalName");
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);

        // when
        String actualName = webhook.getName();

        // then
        assertThat(actualName, equalTo(localName));
    }

    @Test
    void isNotifySuccess_CheckSuccessNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifySuccess(true);

        // when
        boolean result = webhook.isNotifySuccess();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isStartNotification_CheckStartNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setStartNotification(true);

        // when
        boolean result = webhook.isStartNotification();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyAborted_CheckAbortedNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyAborted(true);

        // when
        boolean result = webhook.isNotifyAborted();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyNotBuilt_CheckNotBuiltNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyNotBuilt(true);

        // when
        boolean result = webhook.isNotifyNotBuilt();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyUnstable_CheckUnstableNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyUnstable(true);

        // when
        boolean result = webhook.isNotifyUnstable();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyFailure_CheckFailureNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyFailure(true);

        // when
        boolean result = webhook.isNotifyFailure();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyBackToNormal_CheckBackToNormalNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyBackToNormal(true);

        // when
        boolean result = webhook.isNotifyBackToNormal();

        // then
        assertThat(result, is(true));
    }

    @Test
    void isNotifyRepeatedFailure_CheckRepeatedFailureNotification() {

        // given
        Webhook webhook = new Webhook("someUrl");
        webhook.setNotifyRepeatedFailure(true);

        // when
        boolean result = webhook.isNotifyRepeatedFailure();

        // then
        assertThat(result, is(true));
    }

    @Test
    void getTimeout_ReturnsTimeout() {

        // given
        Webhook webhook = new Webhook("someUrl");
        int timeout = 1234;
        webhook.setTimeout(timeout);

        // when
        int actualTimeout = webhook.getTimeout();

        // then
        assertThat(actualTimeout, equalTo(timeout));
    }

    @Test
    void getTimeout_ReturnsDefaultTimeout() {

        // given
        Webhook webhook = new Webhook("someUrl");

        // when
        int actualTimeout = webhook.getTimeout();

        // then
        assertThat(actualTimeout, equalTo(Webhook.DEFAULT_TIMEOUT));
    }

    @Test
    void getMacros_ReturnsMacros() {

        // given
        Webhook webhook = new Webhook("someUrl");
        Macro macro = new Macro("myTemplate", "yourValue");
        webhook.setMacros(List.of(macro));

        // when
        List<Macro> macros = webhook.getMacros();

        // then
        assertThat(macros, hasSize(1));
        assertThat(macros, contains(macro));
    }

    @Test
    void getMacros_ReturnEmptyList() {

        // given
        Webhook webhook = new Webhook("someUrl");

        // when
        List<Macro> macros = webhook.getMacros();

        // then
        assertThat(macros, empty());
    }

    @Test
    void getFactDefinitions_ReturnsFactDefinitions() {

        // given
        Webhook webhook = new Webhook("someUrl");
        FactDefinition factDefinition = new FactDefinition("name", "myTemplate");
        webhook.setFactDefinitions(List.of(factDefinition));

        // when
        List<FactDefinition> returnedFactDefinitions = webhook.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions, contains(factDefinition));
    }

    @Test
    void getFactDefinitions_OnNullValue_ReturnsEmptyFactDefinitions() {

        // given
        Webhook webhook = new Webhook("someUrl");
        List<FactDefinition> factDefinition = null;
        webhook.setFactDefinitions(factDefinition);

        // when
        List<FactDefinition> returnedFactDefinitions = webhook.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions, empty());
    }
}
