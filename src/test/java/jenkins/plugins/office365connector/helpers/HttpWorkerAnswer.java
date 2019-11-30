package jenkins.plugins.office365connector.helpers;

import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.office365connector.HttpWorker;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class HttpWorkerAnswer implements Answer<HttpWorker> {

    private List<String> data = new ArrayList<>();

    private int times;

    @Override
    public HttpWorker answer(InvocationOnMock invocation) {
        data.add((String) (invocation.getArguments())[1]);

        times++;
        return mock(HttpWorker.class);
    }

    public String getData() {
        return data.get(0);
    }

    public List<String> getAllData() {
        return data;
    }

    public int getTimes() {
        return times;
    }
}
