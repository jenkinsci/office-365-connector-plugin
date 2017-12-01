package jenkins.plugins.office365connector.utils;

import hudson.model.Cause;
import java.util.List;

public class Parser {

    public String getAuthor(List<Cause> causesList) {
        StringBuilder allCauses = new StringBuilder();
        for( Cause cause : causesList){
            allCauses.append(cause.getShortDescription());
            allCauses.append(" ");
        }
        
        int count = 0;
        String[] split = allCauses.toString().split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("user")){
                count = ++i;
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
        return author.toString().trim();
    }
}
