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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Testing HttpWorker which sends post messages with multiple
 * retries.
 */
@RunWith(PowerMockRunner.class)
public class HttpWorkerTest {

    private static Connection connection;

    @BeforeClass
    public static void setUp() throws IOException {
        Container container = new MyHandler();
        Server server = new ContainerServer(container);
        connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(8000);
        connection.connect(address);
    }

    @AfterClass
    public static void destroy() throws IOException {
        connection.close();
    }

    @Test
    public void testSendingMultipleWebhooks() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        HttpWorker worker1 = new HttpWorker("http://localhost:8000/test1", "test1body", 30000, Mockito.mock(PrintStream.class));
        HttpWorker worker2 = new HttpWorker("http://localhost:8000/test2", "test2body", 30000, Mockito.mock(PrintStream.class));
        executorService.submit(worker1);
        executorService.submit(worker2);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue(MyHandler.getTest1Result());
        Assert.assertTrue(MyHandler.getTest2Result());
    }

    private static class MyHandler implements Container {

        private static int trialTestRetries = 0;

        private static boolean test1Result = false;

        private static boolean test2Result = false;

        public static boolean getTest1Result() {
            return test1Result;
        }

        public static boolean getTest2Result() {
            return test2Result;
        }

        public void handle(Request request, Response response) {
            try {
                Path path = request.getPath();
                InputStream is = request.getInputStream();
                String requestBody = IOUtils.toString(is);
                String pathString = path.getPath();
                if ("/test1".equals(pathString)) {
                    if ("test1body".equals(requestBody)) {
                        test1Result = true;
                    }
                } else if ("/test2".equals(pathString)) {
                    if ("test2body".equals(requestBody)) {
                        test2Result = true;
                    }
                } else if ("/retry-test".equals(pathString)) {
                    if (trialTestRetries < 3 - 1) {
                        response.setCode(Status.INTERNAL_SERVER_ERROR.code);
                    }
                    trialTestRetries++;
                }
                PrintStream stream = response.getPrintStream();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
