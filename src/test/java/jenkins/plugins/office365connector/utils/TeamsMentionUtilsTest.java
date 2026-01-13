package jenkins.plugins.office365connector.utils;

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.User;
import hudson.tasks.Mailer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TeamsMentionUtilsTest {

    @Test
    void mentionUser_userWithEmail_returnsMentionTag() throws Exception {
        User user = Mockito.mock(User.class);
        Mailer.UserProperty userProperty = Mockito.mock(Mailer.UserProperty.class);
        Mockito.when(user.getProperty(Mailer.UserProperty.class)).thenReturn(userProperty);
        Mockito.when(userProperty.getAddress()).thenReturn("user@example.com");

        String actual = TeamsMentionUtils.mentionUserOrEmail(user);
        String expected = "<at>user@example.com</at>";
        assertEquals(expected, actual, "User email should be wrapped in <at> tag exactly");
    }

    @Test
    void mentionUser_userWithoutEmail_returnsFullName() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getProperty(Mailer.UserProperty.class)).thenReturn(null);
        Mockito.when(user.getFullName()).thenReturn("John Doe");

        String actual = TeamsMentionUtils.mentionUserOrEmail(user);
        String expected = "John Doe";
        assertEquals(expected, actual, "Fallback to full name when email is missing");
    }

    @Test
    void mentionEmail_nullOrEmpty_returnsAsIs() {
        assertNull(TeamsMentionUtils.mentionEmail(null), "Null email should return null");
        assertEquals("", TeamsMentionUtils.mentionEmail(""), "Empty email should return empty string");
    }

    @Test
    void mentionEmail_validEmail_returnsMentionTag() {
        String email = "user@example.com";
        String actual = TeamsMentionUtils.mentionEmail(email);
        String expected = "<at>user@example.com</at>";
        assertEquals(expected, actual, "Email should be wrapped in <at> tag exactly");
    }
}

