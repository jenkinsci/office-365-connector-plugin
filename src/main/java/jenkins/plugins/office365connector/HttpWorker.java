/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import hudson.ProxyConfiguration;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

/**
 * Makes http post requests in a separate thread.
 * curl -X POST -H "Content-Type: application/json" -d "@completed-success.json" "https://webhook.office.com/webhookb2..." -vs
 */
public class HttpWorker implements Runnable {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final PrintStream logger;

    private final String url;
    private final String data;
    private final int timeout;

    private static final int RETRIES = 3;

    public HttpWorker(String url, String data, int timeout, PrintStream logger) {
        this.url = url;
        this.data = data;
        this.timeout = timeout;
        this.logger = logger;
    }

    /**
     * Sends the notification to the hook.
     */
    public void submit() {
        executorService.submit(this);
    }

    @Override
    public void run() {
        int tried = 0;
        boolean success = false;
        CloseableHttpClient client = getHttpClient();
        do {
            tried++;
            HttpPost post = new HttpPost(url);

            // uncomment to log what message has been sent
            // log("Posted JSON: %s", data);
            post.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));

            try (ClassicHttpResponse httpResponse = client.execute(post, classicHttpResponse -> classicHttpResponse)) {
                int responseCode = httpResponse.getCode();
                if (responseCode >= HttpStatus.SC_BAD_REQUEST) {
                    log("Posting data to %s may have failed. Webhook responded with status code - %s", url, responseCode);
                    String response =
                            EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    log("Message from webhook - %s", response);

                } else {
                    success = true;
                }
            } catch (IOException | ParseException e) {
                log("Failed to post data to webhook - %s", url);
                e.printStackTrace(logger);
            }
        } while (tried < RETRIES && !success);

    }

    private CloseableHttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Jenkins jenkins = Jenkins.get();
        ProxyConfiguration proxy = jenkins.proxy;
        if (proxy != null) {
            List<Pattern> noHostProxyPatterns = proxy.getNoProxyHostPatterns();
            if (!isNoProxyHost(this.url, noHostProxyPatterns)) {
                builder.setProxy(new HttpHost(proxy.name, proxy.port));
                String username = proxy.getUserName();
                String password = proxy.getSecretPassword().getPlainText();
                // Consider it to be passed if username specified. Sufficient?
                if (StringUtils.isNotBlank(username)) {
                    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                            new AuthScope(proxy.name, proxy.port),
                            new UsernamePasswordCredentials(username, password.toCharArray()));
                    builder.setDefaultCredentialsProvider(credsProvider);
                }
            }
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timeout, TimeUnit.MILLISECONDS)
                .setSocketTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        builder.setDefaultRequestConfig(requestConfig);
        builder.setConnectionManager(connectionManager);

        return builder.build();
    }

    private static boolean isNoProxyHost(String host, List<Pattern> noProxyHostPatterns) {
        if (host != null && noProxyHostPatterns != null) {
            for (Pattern p : noProxyHostPatterns) {
                if (p.matcher(host).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method for logging.
     */
    private void log(String format, Object... args) {
        this.logger.println("[Office365connector] " + String.format(format, args));
    }
}
