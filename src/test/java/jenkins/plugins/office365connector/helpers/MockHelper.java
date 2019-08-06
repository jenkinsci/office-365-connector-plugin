package jenkins.plugins.office365connector.helpers;

import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.mockito.stubbing.Answer;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class MockHelper {

    public static void mockNew(Class classToMock, Answer answer) {
        Answer mock = answer;
        try {
            whenNew(classToMock).withAnyArguments().thenAnswer(mock);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
