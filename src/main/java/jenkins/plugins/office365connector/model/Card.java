package jenkins.plugins.office365connector.model;

import java.util.List;

public interface Card {

    Object toPayload();

    void setAction(List<CardAction> actions);

    void setThemeColor(String cardThemeColor);

    String getSummary();

    List<Section> getSections();

    String getThemeColor();
}
