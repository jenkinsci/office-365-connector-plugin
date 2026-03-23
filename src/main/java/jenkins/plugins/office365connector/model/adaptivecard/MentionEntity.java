package jenkins.plugins.office365connector.model.adaptivecard;

import jenkins.plugins.office365connector.model.Mention;

public class MentionEntity {

    private final String type = "mention";
    private final String text;
    private final Mentioned mentioned;

    public MentionEntity(Mention mention) {
        this.text = "<at>" + mention.getName() + "</at>";
        this.mentioned = new Mentioned(mention.getUserId(), mention.getName());
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Mentioned getMentioned() {
        return mentioned;
    }
}
