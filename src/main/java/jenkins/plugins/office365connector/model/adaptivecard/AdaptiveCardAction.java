package jenkins.plugins.office365connector.model.adaptivecard;

import java.util.List;

public class AdaptiveCardAction implements jenkins.plugins.office365connector.model.Action {

    private String type = "Action.OpenUrl";

    private String title;

    private String url;

    public AdaptiveCardAction(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public void setName(final String name) {
        setTitle(name);
    }

    @Override
    public void setTarget(final List<String> target) {
        target.stream().findFirst().ifPresent(this::setUrl);
    }

    @Override
    public String getName() {
        return getTitle();
    }
}
