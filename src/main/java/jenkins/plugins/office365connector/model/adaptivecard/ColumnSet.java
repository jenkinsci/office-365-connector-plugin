package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ColumnSet implements AdaptiveCardElement {

    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private final String type = "ColumnSet";
    private final List<Column> columns;
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private final String width = "stretch";

    public ColumnSet(final List<Column> items) {
        this.columns = items;
    }

    public String getWidth() {
        return width;
    }

    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public String getType() {
        return "";
    }
}
