package jenkins.plugins.office365connector.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.User;
import hudson.tasks.Mailer;

import org.junit.jupiter.api.Test;

class TeamsMentionUtilsTest {

    @Test
    void mentionUser_userWithEmail_returnsMentionTag() throws Exception {
        User user = mock(User.class);
        Mailer.UserProperty userProperty = mock(Mailer.UserProperty.class);
        
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(userProperty);
        when(userProperty.getAddress()).thenReturn("user@example.com");

        String actual = TeamsMentionUtils.mentionUserOrEmail(user);
        assertEquals("<at>user@example.com</at>", actual);
    }

    @Test
    void mentionUser_userWithoutEmail_returnsFullName() {
        User user = mock(User.class);
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(null);
        when(user.getFullName()).thenReturn("John Doe");

        String actual = TeamsMentionUtils.mentionUserOrEmail(user);
        assertEquals("John Doe", actual);
    }

    @Test
    void mentionEmail_nullOrEmpty_returnsAsIs() {
        assertNull(TeamsMentionUtils.mentionEmail(null));
        assertEquals("", TeamsMentionUtils.mentionEmail(""));
    }

    @Test
    void mentionEmail_validEmail_returnsMentionTag() {
        String actual = TeamsMentionUtils.mentionEmail("user@example.com");
        assertEquals("<at>user@example.com</at>", actual);
    }
}
