package jenkins.plugins.office365connector.workflow;

import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.WebhookJobPropertyDescriptor;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class WebhookJobPropertyDescriptorTest {

    private static final String KEY = "webhooks";

    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        File rootDir = new File(".");
        when(jenkins.getRootDir()).thenReturn(rootDir);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");
        when(mockDescriptor.getId()).thenReturn("testId");
        when(mockDescriptor.getDescriptorFullUrl()).thenReturn("http://test.com");

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
    }

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    @Test
    void isEnabled_ByDefault_ReturnsFalse() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isEnabled = descriptor.isEnabled();

        // then
        assertThat(isEnabled, is(false));
    }


    @Test
    void isEnabled_ChecksWebhookSize() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        List<Webhook> webhooks = WebhookBuilder.sampleWebhookWithAllStatuses();
        descriptor.setWebhooks(webhooks);

        // when
        boolean isEnabled = descriptor.isEnabled();

        // then
        assertThat(isEnabled, is(true));
    }

    @Test
    void isApplicable_ReturnsTrue() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isApplicable = descriptor.isApplicable(null);

        // then
        assertThat(isApplicable, is(true));
    }

    @Test
    void getDisplayName_ReturnsName() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name, equalTo("Job Notification"));
    }


    @Test
    void newInstance_OnNullObject_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        WebhookJobProperty property = descriptor.newInstance((StaplerRequest2) null, null);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), empty());
    }

    @Test
    void newInstance_OnNullForm_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject(true);

        // when
        WebhookJobProperty property = descriptor.newInstance((StaplerRequest2) null, jsonObject);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), empty());
    }

    @Test
    void newInstance_OnEmptyForm_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();

        // when
        WebhookJobProperty property = descriptor.newInstance((StaplerRequest2) null, jsonObject);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), empty());
    }

    @Test
    void newInstance_OnEmptyWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY, Collections.emptyList());

        // when
        WebhookJobProperty property = descriptor.newInstance((StaplerRequest2) null, jsonObject);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), empty());
    }

    @Test
    void newInstance_OnSingleWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        Webhook webhook = new Webhook("myUrl");

        // Excluding "descriptor" to avoid infinite loop in jsonObject.put()
        Map<String, Object> map = new HashMap<>();
        map.put(KEY, webhook);
        JsonConfig config = new JsonConfig();
        config.setExcludes(new String[]{"descriptor"});

        jsonObject.putAll(map, config);

        StaplerRequest2 request = mock(StaplerRequest2.class);
        when(request.bindJSON(ArgumentMatchers.any(), (JSONObject) ArgumentMatchers.eq(jsonObject.get(KEY)))).thenReturn(webhook);

        // when
        WebhookJobProperty property = descriptor.newInstance(request, jsonObject);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), contains(webhook));
    }

    @Test
    void newInstance_OnArrayWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        Webhook webhook = new Webhook("myUrl");
        List<Object> webhooks = List.of(webhook);

        // Excluding "descriptor" to avoid infinite loop in jsonObject.put()
        Map<String, Object> map = new HashMap<>();
        map.put(KEY, webhooks);
        JsonConfig config = new JsonConfig();
        config.setExcludes(new String[]{"descriptor"});

        jsonObject.putAll(map, config);

        StaplerRequest2 request = mock(StaplerRequest2.class);
        when(request.bindJSONToList(ArgumentMatchers.any(), ArgumentMatchers.eq(jsonObject.get(KEY)))).thenReturn(webhooks);

        // when
        WebhookJobProperty property = descriptor.newInstance(request, jsonObject);

        // then
        assertThat(property, notNullValue());
        assertThat(property.getWebhooks(), hasSize(webhooks.size()));
        assertThat(property.getWebhooks(), contains(webhook));
    }

    @Test
    void configure_ReturnsTrue() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isConfigured = descriptor.configure((StaplerRequest2) null, null);

        // then
        // very naive, worth to improve
        assertThat(isConfigured, is(true));
    }
}
