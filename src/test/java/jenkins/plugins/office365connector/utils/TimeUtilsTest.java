package jenkins.plugins.office365connector.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public final class TimeUtilsTest {

    private static TimeZone defaultTimeZone;
    private static Locale defaultLocale;

    // this code must be executed before tested class TimeUtils is loaded by class loader
    // @Before annotations are executed to late thus this conception must be used
    static {
        setupTimeZoneAndLocale();
    }

    public static void setupTimeZoneAndLocale() {
        defaultTimeZone = TimeZone.getDefault();
        // because of the timezone, daylight etc
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        defaultLocale = Locale.getDefault();
        // because of the how the date is formatted, displayed
        Locale.setDefault(Locale.US);
    }

    @AfterClass
    public static void resetLocales() {
        TimeZone.setDefault(defaultTimeZone);
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void dateToString_ReturnsDateAsString() {

        // given
        long date = 1506717341515L;

        // when
        String format = TimeUtils.dateToString(date);

        // then
        assertThat(format).isEqualTo("2017-09-29 20:35:41 UTC");
    }

    @Test
    public void durationToString_OnSingularValues_ReturnsDurationAsString() {

        // given
        long duration = /* sec*/ 1 + /*min*/ 60 +/*hour*/ +60 * 60 +/*day*/+60 * 60 * 24;

        // when
        String format = TimeUtils.durationToString(duration);

        // then
        assertThat(format).isEqualTo("1 day, 1 hour, 1 minute, 1 second");
    }

    @Test
    public void durationToString_OnPluralValues_ReturnsDurationAsString() {

        // given
        long duration = /* sec*/ 5 + /*min*/60 * 6 +/*hour*/ +60 * 60 * 7 +/*day*/+60 * 60 * 24 * 8;

        // when
        String format = TimeUtils.durationToString(duration);

        // then
        assertThat(format).isEqualTo("8 days, 7 hours, 6 minutes, 5 seconds");
    }


    @Test
    public void durationToString_OnOneSecond_ReturnsDurationOnlyWithSecond() {

        // given
        long duration = /* sec*/ 15;

        // when
        String format = TimeUtils.durationToString(duration);

        // then
        assertThat(format).isEqualTo("15 seconds");
    }
}
