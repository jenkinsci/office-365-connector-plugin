package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.plugins.office365connector.model.Fact;

public class FactSet implements AdaptiveCardElement {

    private final List<AdaptiveCardFact> facts;
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private final String type = "FactSet";

    public FactSet(List<Fact> facts) {
        this.facts = facts.stream().map(f -> new AdaptiveCardFact(f.getName(),f.getValue())).collect(Collectors.toList());
    }

    public List<AdaptiveCardFact> getFacts() {
        return facts;
    }

    @Override
    public String getType() {
        return type;
    }
}
