package jenkins.plugins.office365connector.model.adaptivecard;

public class Mentioned {

    private final String id;
    private final String name;

    public Mentioned(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
