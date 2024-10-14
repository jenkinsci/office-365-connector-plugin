package jenkins.plugins.office365connector.model;

import java.util.List;

public interface CardAction {

    void setName(String name);

    void setTargets(List<String> targets);

    String getName();
}
