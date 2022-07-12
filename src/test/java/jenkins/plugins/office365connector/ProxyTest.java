package jenkins.plugins.office365connector;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxyTest {

    private Proxy proxyTest;
    private Proxy proxyTestNoCredentials;

    @Before
    public void setUp() {
        proxyTest = new Proxy("10.0.0.1", 65654, "myUsername", "myPassword");
        proxyTestNoCredentials = new Proxy("10.0.0.1", 65654, null, null);
    }

    @Test
    public void getIp() {
        assertThat("10.0.0.1").isEqualTo(proxyTest.getIp());
    }

    @Test
    public void getPort() {
        assertThat(65654).isEqualTo(proxyTest.getPort());
    }

    @Test
    public void getUsername() {
        assertThat("myUsername").isEqualTo(proxyTest.getUsername());
    }

    @Test
    public void getPassword() {
        assertThat("myPassword").isEqualTo(proxyTest.getPassword());
    }

    @Test
    public void proxyConfigured_True() {
        assertThat(proxyTest.proxyConfigured().equals(true));
    }

    @Test
    public void proxyConfigured_False() {
        assertThat(proxyTest.proxyConfigured().equals(false));
    }
}