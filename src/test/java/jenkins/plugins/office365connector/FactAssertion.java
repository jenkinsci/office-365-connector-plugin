package jenkins.plugins.office365connector;

import jenkins.plugins.office365connector.model.Fact;
import org.hamcrest.MatcherAssert;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FactAssertion {

    private final Fact fact;

    private FactAssertion(Fact fact) {
        this.fact = fact;
    }

    public static FactAssertion assertThat(FactsBuilder factsBuilder) {
        return assertThat(factsBuilder.collect());
    }

    public static FactAssertion assertThat(List<Fact> facts) {
        MatcherAssert.assertThat(facts, hasSize(1));
        return new FactAssertion(facts.get(0));
    }

    public static FactAssertion assertThatLast(List<Fact> facts, int size) {
        MatcherAssert.assertThat(facts, hasSize(size));
        return new FactAssertion(facts.get(size - 1));
    }

    public FactAssertion hasName(String name) {
        MatcherAssert.assertThat(fact.getName(), equalTo(name));
        return this;
    }

    public FactAssertion hasValue(String value) {
        MatcherAssert.assertThat(fact.getValue(), equalTo(value));
        return this;
    }
}
