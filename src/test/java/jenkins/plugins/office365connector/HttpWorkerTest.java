package jenkins.plugins.office365connector;

import hudson.ProxyConfiguration;
import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.workflow.AbstractTest;
import org.apache.commons.httpclient.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, HttpWorker.class})
public class HttpWorkerTest extends AbstractTest {


    @Before
    public void setUp() {
        Jenkins mockJenkins = mock(Jenkins.class);
        mockStatic(Jenkins.class);
        Mockito.when(Jenkins.get()).thenReturn(mockJenkins);
    }

    @Test
    public void HttpWorker_getHttpClient_NoJenkinsProxy() throws NoSuchMethodException {

        // given
        // from @Before
        Proxy thisPluginProxy = new Proxy(null, null, null, null);
        HttpWorker httpWorker = new HttpWorker("http://127.0.0.1", "{}", 30, System.out, thisPluginProxy);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isNull();
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(-1);
    }

    @Test
    public void HttpWorker_getHttpClient_JenkinsProxy() throws NoSuchMethodException {

        // given
        // from @Before
        Proxy thisPluginProxy = new Proxy(null, null, null, null);
        Jenkins jenkins = Jenkins.get();
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration("name", 123, null, null, "*mockwebsite.com*");
        jenkins.proxy = proxyConfiguration;

        HttpWorker httpWorker = new HttpWorker("http://127.0.0.1", "{}", 30, System.out, thisPluginProxy);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isEqualTo(proxyConfiguration.getName());
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(proxyConfiguration.getPort());
    }

    @Test
    public void HttpWorker_getHttpClient_JenkinsProxy_Ignored() throws NoSuchMethodException {

        // given
        // from @Before
        Proxy thisPluginProxy = new Proxy(null, null, null, null);
        Jenkins jenkins = Jenkins.get();
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration("name", 123, null, null, "*mockwebsite.com*");
        jenkins.proxy = proxyConfiguration;

        HttpWorker httpWorker = new HttpWorker("http://mockwebsite.com", "{}", 30, System.out, thisPluginProxy);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isNull();
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(-1);
    }

    @Test
    public void HttpWorker_getHttpClient_PluginProxy() throws NoSuchMethodException {

        // given
        // from @Before
        Proxy thisPluginProxy = new Proxy("10.0.0.1", 12345, null, null);
        HttpWorker httpWorker = new HttpWorker("http://127.0.0.1", "{}", 30, System.out, thisPluginProxy);
        Method method = HttpWorker.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);

        // when
        HttpClient httpClient = (HttpClient) ReflectionUtils.invokeMethod(method, httpWorker);

        // then
        assertThat(httpClient.getHostConfiguration().getProxyHost()).isEqualTo(thisPluginProxy.getIp());
        assertThat(httpClient.getHostConfiguration().getProxyPort()).isEqualTo(thisPluginProxy.getPort());
    }
}