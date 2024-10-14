package jenkins.plugins.office365connector.model.adaptivecard;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TextBlock implements AdaptiveCardElement {

    private String text;
    private String weight;
    private String size;
    private String color;
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private String type = "TextBlock";
    private boolean wrap;

    public TextBlock(final String text) {
        this(text,"default", "default");
    }

    public TextBlock(final String text, final String size, final String weight) {
        this(text, size,weight,"default");
    }

    public TextBlock(final String text, final String size, final String weight, final String color) {
        this.text = text;
        this.wrap = true;
        this.size = size;
        this.weight = weight;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public boolean isWrap() {
        return wrap;
    }

    public String getWeight() {
        return weight;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }
}
