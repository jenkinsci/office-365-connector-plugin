package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

import jenkins.plugins.office365connector.model.Mention;

public class MsTeams {

    private String width = "Full";

    @SerializedName("entities")
    private List<Mention> mentions = new ArrayList<>();

    public String getWidth() {
        return width;
    }

    public List<Mention> getMentions() {
        return (mentions == null || mentions.isEmpty()) ? null : mentions;
    }

    public void setMentions(List<Mention> mentions) {
        this.mentions = mentions;
    }
}