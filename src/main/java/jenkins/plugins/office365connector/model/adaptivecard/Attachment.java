package jenkins.plugins.office365connector.model.adaptivecard;

public class Attachment {

    private String contentType = "application/vnd.microsoft.card.adaptive";
    private final AdaptiveCard content;

    public Attachment(final AdaptiveCard content) {
        this.content = content;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public AdaptiveCard getContent() {
        return content;
    }
}
