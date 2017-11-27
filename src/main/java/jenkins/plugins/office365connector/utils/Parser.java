package jenkins.plugins.office365connector.utils;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import jenkins.plugins.office365connector.model.Fact;

import java.io.File;
import java.util.List;

/**
 * Created by olegfeferman on 23.11.2017.
 */
public class Parser {

    public String getAuthor(List<Cause> causesList) {
        StringBuilder allCauses = new StringBuilder();
        for (int i = 0; i < causesList.size(); i++) {
            allCauses.append(causesList.get(i).getShortDescription());
            allCauses.append(" ");
        }

        int count = 0;
        String[] split = allCauses.toString().split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("user")){
                count = i+1;
                break;}
            else if (split[i].equals("BitBucket")){
                count = i;
                break;
            }
        }
        StringBuilder author = new StringBuilder();
        for (int i = count; i < split.length; i++) {
            if (split[i].equalsIgnoreCase("Started")) {
                break;
            }
            author.append(split[i]);
            author.append(" ");            
        }
        return author.toString();
    }
}
