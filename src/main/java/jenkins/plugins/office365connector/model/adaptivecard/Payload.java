package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.ArrayList;
import java.util.List;

public class Payload {

    private String type = "message";
    private final List<Attachment> attachments = new ArrayList<>();

    public Payload(AdaptiveCard adaptiveCard) {
        attachments.add(new Attachment(adaptiveCard));
    }

    public String getType() {
        return type;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }
}
