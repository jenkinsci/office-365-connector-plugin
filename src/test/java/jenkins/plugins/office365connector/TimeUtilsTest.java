package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public final class TimeUtilsTest {

    @Test
    public void dateToString_ReturnsDateAsString() {

        // given
        long date = 1506717341515L;

        // when
        String format = TimeUtils.dateToString(date);

        // then
        // validates only the date as the timezone and local can be different
        assertThat(format).startsWith("2017-09-29");
        // because of the timezone, hours depends whete the test is executed
        assertThat(format).contains("35:41");
    }

    @Test
    public void durationToString_OnSingularValues_ReturnsDurationAsString() {

        // given
        long duration = /* sec*/ 1 + /*min*/60 +/*hour*/ +60 * 60 +/*day*/+60 * 60 * 24;

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
