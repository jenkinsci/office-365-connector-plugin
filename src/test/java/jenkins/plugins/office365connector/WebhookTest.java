package jenkins.plugins.office365connector;

import java.util.Arrays;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Result;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.model.Macro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class)
public class WebhookTest {
    @Before
    public void setUp() throws Exception {

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);

        Jenkins jenkins = mock(Jenkins.class);
        mockStatic(Jenkins.class);
        Mockito.when(Jenkins.get()).thenReturn(jenkins);
        Mockito.when(jenkins.getDescriptorOrDie(anyObject())).thenReturn(mockDescriptor);
    }

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
    public void getUrl_WithEmptyLocalUrlReturnsGlobalUrl() {

        // given
        String globalUrl = "globalUrl";
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(Jenkins.get()).thenReturn(jenkins);
        when(mockDescriptor.getGlobalUrl()).thenReturn(globalUrl);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        Webhook webhook = new Webhook("");

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl).isEqualTo(globalUrl);
    }

    @Test
    public void getUrl_ReturnsLocalUrlAndNotGlobal() {

        // given
        String globalUrl = "globalUrl";
        String localUrl = "localUrl";
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(Jenkins.get()).thenReturn(jenkins);
        when(mockDescriptor.getUrl()).thenReturn(globalUrl);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
        Webhook webhook = new Webhook(localUrl);

        // when
        String actualUrl = webhook.getUrl();

        // then
        assertThat(actualUrl).isEqualTo(localUrl);
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
    public void getName_WithEmptyLocalNameReturnsGlobalName() {

        // given
        String globalName = "globalName";
        Webhook webhook = new Webhook("someUrl");
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(Jenkins.get()).thenReturn(jenkins);
        when(mockDescriptor.getGlobalName()).thenReturn(globalName);
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);

        // when
        String actualName = webhook.getName();
        // then
        assertThat(actualName).isEqualTo(globalName);
    }

    @Test
    public void getName_ReturnsLocalNameAndNotGlobal() {

        // given
        String localName = "myName";
        Webhook webhook = new Webhook("someUrl");
        webhook.setName(localName);
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(Jenkins.get()).thenReturn(jenkins);
        when(mockDescriptor.getName()).thenReturn("globalName");
        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);

        // when
        String actualName = webhook.getName();

        // then
        assertThat(actualName).isEqualTo(localName);
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

    @Test
    public void getFactDefinitions_ReturnsFactDefinitions() {

        // given
        Webhook webhook = new Webhook("someUrl");
        FactDefinition factDefinition = new FactDefinition("name", "myTemplate");
        webhook.setFactDefinitions(Arrays.asList(factDefinition));

        // when
        List<FactDefinition> returnedFactDefinitions = webhook.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions).containsOnly(factDefinition);
    }

    @Test
    public void getFactDefinitions_OnNullValue_ReturnsEmptyFactDefinitions() {

        // given
        Webhook webhook = new Webhook("someUrl");
        List<FactDefinition> factDefinition = null;
        webhook.setFactDefinitions(factDefinition);

        // when
        List<FactDefinition> returnedFactDefinitions = webhook.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions).isEmpty();
    }
}
