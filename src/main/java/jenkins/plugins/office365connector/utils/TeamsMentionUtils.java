package jenkins.plugins.office365connector.utils;

import org.apache.commons.lang.StringUtils;
import hudson.model.User;
import hudson.tasks.Mailer;

public final class TeamsMentionUtils {

    private TeamsMentionUtils() {
        // utility class
    }

    public static String mentionEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }
        return "<at>" + email + "</at>";
    }

    public static String mentionUserOrEmail(User user) {
        Mailer.UserProperty userProperty = user.getProperty(Mailer.UserProperty.class);
        if (userProperty != null && StringUtils.isNotBlank(userProperty.getAddress())) {
            return mentionEmail(userProperty.getAddress());
        }
        // fallback: just return username
        return user.getFullName();
    }

}
