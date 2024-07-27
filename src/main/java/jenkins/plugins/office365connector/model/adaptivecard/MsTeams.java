package jenkins.plugins.office365connector.model.adaptivecard;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class MsTeams {

    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private String width = "Full";

    public String getWidth() {
        return width;
    }
}
