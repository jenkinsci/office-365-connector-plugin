package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;

public class MsTeams {

    private String width = "Full";
    private List<MentionEntity> entities;

    public String getWidth() {
        return width;
    }

    public List<MentionEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<MentionEntity> entities) {
        this.entities = entities;
    }
}
