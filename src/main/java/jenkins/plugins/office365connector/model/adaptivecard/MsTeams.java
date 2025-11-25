package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;
import java.util.Map;

public class MsTeams {

    private String width = "Full";
    private List<Map<String, Object>> entities; 

    public String getWidth() {
        return width;
    }

    public List<Map<String, Object>> getEntities() {
        return entities;
    }

    public void setEntities(List<Map<String, Object>> entities) {
        this.entities = entities;
    }
}