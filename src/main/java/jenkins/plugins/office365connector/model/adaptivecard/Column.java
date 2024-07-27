package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Column implements AdaptiveCardElement {

    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private final String type = "Column";
    private final List<AdaptiveCardElement> items;

    public Column(final List<AdaptiveCardElement> items) {
        this.items = items;
    }

    @Override
    public String getType() {
        return type;
    }

    public List<AdaptiveCardElement> getItems() {
        return items;
    }
}
