package jenkins.plugins.office365connector.helpers;

import java.util.Arrays;
import java.util.List;

import hudson.model.Cause;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class CauseBuilder {

    public static final String[] causes = {"someone has started", "somehow was triggered"};

    public static List<Cause> sampleCauses() {

        return Arrays.asList(new SampleCause(causes[0]), new SampleCause(causes[1]));
    }

    private static class SampleCause extends Cause {

        private final String description;

        SampleCause(String description) {
            this.description = description;
        }

        @Override
        public String getShortDescription() {
            return description;
        }
    }
}
