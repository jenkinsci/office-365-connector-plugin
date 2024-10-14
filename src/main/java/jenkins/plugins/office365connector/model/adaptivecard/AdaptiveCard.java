package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Result;
import jenkins.plugins.office365connector.model.Card;
import jenkins.plugins.office365connector.model.CardAction;
import jenkins.plugins.office365connector.model.Section;

public class AdaptiveCard implements Card {

    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private String type = "AdaptiveCard";
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    @SerializedName("$schema")
    private final String schema = "http://adaptivecards.io/schemas/adaptive-card.json";
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC")
    private final String version = "1.4";
    private final MsTeams msTeams = new MsTeams();
    private final List<AdaptiveCardElement> body;
    private List<CardAction> actions;

    public AdaptiveCard(final String summary, final Section section, Result result) {
        this.body = new ArrayList<>();
        this.body.add(new TextBlock(summary, "large", "bolder", color(result)));
        if (section != null) {
            this.body.add(new ColumnSet(List.of(new Column(List.of(
                    new TextBlock(section.getActivityTitle(), "default", "bolder"),
                    new TextBlock(section.getActivitySubtitle())
            )))));
            if (!section.getFacts().isEmpty()) {
                this.body.add(new FactSet(section.getFacts()));
            }
        }
    }

    private String color(final Result result) {
        if (result.equals(Result.SUCCESS)) {
            return "good";
        } else if (result.equals(Result.UNSTABLE)) {
            return "warning";
        } else if (result.equals(Result.FAILURE)) {
            return "attention";
        }
        return "default";
    }

    public String getType() {
        return type;
    }

    public String getSchema() {
        return schema;
    }

    public String getVersion() {
        return version;
    }

    public MsTeams getMsTeams() {
        return msTeams;
    }

    public List<AdaptiveCardElement> getBody() {
        return body;
    }

    public List<CardAction> getActions() {
        return actions;
    }

    @Override
    public Object toPaylod() {
        return new Payload(this);
    }

    @Override
    public void setAction(final List<CardAction> actions) {
        this.actions = actions;
    }

    @Override
    public void setThemeColor(final String cardThemeColor) {
        // intentionally empty, unused with AdaptiveCard format
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public List<Section> getSections() {
        return List.of();
    }

    @Override
    public String getThemeColor() {
        return null;
    }
}
