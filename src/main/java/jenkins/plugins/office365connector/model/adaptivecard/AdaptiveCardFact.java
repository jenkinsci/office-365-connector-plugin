package jenkins.plugins.office365connector.model.adaptivecard;

public class AdaptiveCardFact {
    private final String title;
    private final String value;

    public AdaptiveCardFact(final String title, final String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }
}
