package jenkins.plugins.office365connector.utils;

import org.apache.commons.lang3.StringUtils;
import hudson.model.User;
import hudson.model.UserProperty;

import java.lang.reflect.Method;

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
        try {
            @SuppressWarnings("unchecked")
            Class<? extends UserProperty> mailerPropClass =
                (Class<? extends UserProperty>)
                    Class.forName("hudson.tasks.Mailer$UserProperty");

            UserProperty prop = user.getProperty(mailerPropClass);
            if (prop != null) {
                Method getAddress = mailerPropClass.getMethod("getAddress");
                Object email = getAddress.invoke(prop);

                if (email instanceof String && StringUtils.isNotBlank((String) email)) {
                    return mentionEmail((String) email);
                }
            }
        } catch (ClassNotFoundException e) {
            // Mailer not installed — ignore
        } catch (Exception e) {
            // Defensive: ignore and fall back
        }

        return user.getFullName();
    }
}
