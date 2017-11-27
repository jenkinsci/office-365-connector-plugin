package jenkins.plugins.office365connector.utils;

import hudson.model.Cause;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by olegfeferman on 25.11.2017.
 */
public class ParserTest {
    @Test
    public void getAuthor() throws Exception {
        final String patternForTest1 = "BitBucket push by authorname";
        final String patternForTest2 = "Author Name";
        Cause cause1ForTest1 = new Cause() {
            @Override
            public String getShortDescription() {
                return "Started by BitBucket push by authorname" ;
            }
        };
        Cause cause2ForTest1 = new Cause() {
            @Override
            public String getShortDescription() {
                return "Started by BitBucket push by authorname";
            }
        };

        Cause cause1ForTest2 = new Cause() {
            @Override
            public String getShortDescription() {
                return "Started by user Author Name";
            }
        };

        List<Cause> causeListForTest1 = new ArrayList<>();
        List<Cause> causeListForTest2 = new ArrayList<>();
        causeListForTest1.add(cause1ForTest1);
        causeListForTest1.add(cause2ForTest1);
        causeListForTest2.add(cause1ForTest2);
        Parser parser = new Parser();

        Assert.assertTrue(patternForTest1.equals(parser.getAuthor(causeListForTest1).trim()));
        Assert.assertTrue(patternForTest2.equals(parser.getAuthor(causeListForTest2).trim()));

    }

}