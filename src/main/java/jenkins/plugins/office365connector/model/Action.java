package jenkins.plugins.office365connector.model;

import java.util.List;

public interface Action {

    void setName(String name);

    void setTarget(List<String> target);

    String getName();
}
