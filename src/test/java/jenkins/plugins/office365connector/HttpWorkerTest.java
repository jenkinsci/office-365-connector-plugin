package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.lang.reflect.Method;

import hudson.ProxyConfiguration;
import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import org.apache.commons.httpclient.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class HttpWorkerTest extends AbstractTest {

    private MockedStatic<Jenkins> staticJenkins;

    @Before
    public void setUp() {
        Jenkins mockJenkins = mock(Jenkins.class);
        staticJenkins = mockStatic(Jenkins.class);
        staticJenkins.when(Jenkins::get).thenReturn(mockJenkins);
    }

    @After
    public void tearDown() {
        staticJenkins.close();
    }

    @Test
    public void HttpWorker_getHttpClient_NoProxy() throws NoSuchMethodException {

        // given
        // from @Before
        HttpWorker httpWorker = new HttpWorker("http://127.0.0.1", "{}", 30, System.out);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isNull();
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(-1);
    }

    @Test
    public void HttpWorker_getHttpClient_Proxy() throws NoSuchMethodException {

        // given
        // from @Before
        Jenkins jenkins = Jenkins.get();
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration("name", 123, null, null, "*mockwebsite.com*");
        jenkins.proxy = proxyConfiguration;

        HttpWorker httpWorker = new HttpWorker("http://127.0.0.1", "{}", 30, System.out);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isEqualTo(proxyConfiguration.getName());
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(proxyConfiguration.getPort());
    }

    @Test
    public void HttpWorker_getHttpClient_Proxy_Ignored() throws NoSuchMethodException {

        // given
        // from @Before
        Jenkins jenkins = Jenkins.get();
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration("name", 123, null, null, "*mockwebsite.com*");
        jenkins.proxy = proxyConfiguration;

        HttpWorker httpWorker = new HttpWorker("http://mockwebsite.com", "{}", 30, System.out);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isNull();
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(-1);
    }
}
