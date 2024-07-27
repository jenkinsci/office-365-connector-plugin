package jenkins.plugins.office365connector.model;

import java.util.List;

public interface Card {

    public Object toPaylod();

    void setAction(List<Action> actions);

    void setThemeColor(String cardThemeColor);

    String getSummary();

    List<Section> getSections();

    String getThemeColor();
}
