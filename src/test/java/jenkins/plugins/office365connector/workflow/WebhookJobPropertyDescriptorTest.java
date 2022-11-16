package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.WebhookJobProperty;
import jenkins.plugins.office365connector.WebhookJobPropertyDescriptor;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class WebhookJobPropertyDescriptorTest {

    private static final String KEY = "webhooks";

    private MockedStatic<Jenkins> staticJenkins;

    @Before
    public void setUp() {
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

    @After
    public void tearDown() {
        staticJenkins.close();
    }

    @Test
    public void isEnabled_ByDefault_ReturnsFalse() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isEnabled = descriptor.isEnabled();

        // then
        assertThat(isEnabled).isFalse();
    }


    @Test
    public void isEnabled_ChecksWebhookSize() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        List<Webhook> webhooks = WebhookBuilder.sampleWebhookWithAllStatuses();
        descriptor.setWebhooks(webhooks);

        // when
        boolean isEnabled = descriptor.isEnabled();

        // then
        assertThat(isEnabled).isTrue();
    }

    @Test
    public void isApplicable_ReturnsTrue() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isApplicable = descriptor.isApplicable(null);

        // then
        assertThat(isApplicable).isTrue();
    }

    @Test
    public void getDisplayName_ReturnsName() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        String name = descriptor.getDisplayName();

        // then
        assertThat(name).isEqualTo("Job Notification");
    }


    @Test
    public void newInstance_OnNullObject_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        WebhookJobProperty property = descriptor.newInstance(null, null);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks()).isEmpty();
    }

    @Test
    public void newInstance_OnNullForm_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject(true);

        // when
        WebhookJobProperty property = descriptor.newInstance(null, jsonObject);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks()).isEmpty();
    }

    @Test
    public void newInstance_OnEmptyForm_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();

        // when
        WebhookJobProperty property = descriptor.newInstance(null, jsonObject);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks()).isEmpty();
    }

    @Test
    public void newInstance_OnEmptyWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY, Collections.emptyList());

        // when
        WebhookJobProperty property = descriptor.newInstance(null, jsonObject);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks()).isEmpty();
    }

    @Test
    public void newInstance_OnSingleWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        Webhook webhook = new Webhook("myUrl");

        // Excluding "descriptor" to avoid infinite loop in jsonObject.put()
        Map map = new HashMap<String, Object>();
        map.put(KEY, webhook);
        JsonConfig config = new JsonConfig();
        config.setExcludes(new String[]{"descriptor"});

        jsonObject.putAll(map, config);

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.bindJSON(ArgumentMatchers.any(), (JSONObject) ArgumentMatchers.eq(jsonObject.get(KEY)))).thenReturn(webhook);

        // when
        WebhookJobProperty property = descriptor.newInstance(request, jsonObject);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks()).containsExactly(webhook);
    }

    @Test
    public void newInstance_OnArrayWebhook_ReturnsJobProperty() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();
        JSONObject jsonObject = new JSONObject();
        Webhook webhook = new Webhook("myUrl");
        List<Object> webhooks = Arrays.asList(webhook);

        // Excluding "descriptor" to avoid infinite loop in jsonObject.put()
        Map map = new HashMap<String, Object>();
        map.put(KEY, webhooks);
        JsonConfig config = new JsonConfig();
        config.setExcludes(new String[]{"descriptor"});

        jsonObject.putAll(map, config);

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.bindJSONToList(ArgumentMatchers.any(), ArgumentMatchers.eq(jsonObject.get(KEY)))).thenReturn(webhooks);

        // when
        WebhookJobProperty property = descriptor.newInstance(request, jsonObject);

        // then
        assertThat(property).isNotNull();
        assertThat(property.getWebhooks())
                .hasSameSizeAs(webhooks)
                .containsExactly(webhook);
    }

    @Test
    public void configure_ReturnsTrue() {

        // when
        WebhookJobPropertyDescriptor descriptor = new WebhookJobPropertyDescriptor();

        // when
        boolean isConfigured = descriptor.configure(null, null);

        // then
        // very naive, worth to improve
        assertThat(isConfigured).isTrue();
    }
}
