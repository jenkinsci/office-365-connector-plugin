package jenkins.plugins.office365connector.utils;

import org.apache.commons.lang.time.FastDateFormat;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Collects method for time and duration.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public final class TimeUtils {

    /**
     * Formatter for date. Uses local timezone and locale.
     */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss z");

    private static final PeriodFormatter DURATION_FORMATTER = new PeriodFormatterBuilder()
            .appendWeeks()
            .appendSuffix(" week", " weeks")
            .appendSeparator(", ")
            .printZeroNever()
            .appendMonths()
            .appendSuffix(" month", " months")
            .appendSeparator(", ")
            .printZeroNever()
            .appendDays()
            .appendSuffix(" day", " days")
            .appendSeparator(", ")
            .printZeroNever()
            .appendHours()
            .appendSuffix(" hour", " hours")
            .appendSeparator(", ")
            .printZeroNever()
            .appendMinutes()
            .appendSuffix(" minute", " minutes")
            .appendSeparator(", ")
            .printZeroNever()
            .appendSeconds()
            .appendSuffix(" second", " seconds")
            .toFormatter();

    private TimeUtils() {
    }

    /**
     * Converts data to formatted string.
     *
     * @param date date to format
     * @return formatted date
     */
    public static String dateToString(long date) {
        return DATE_FORMAT.format(date);
    }

    /**
     * Converts duration to formatted string.
     *
     * @param duration duration to convert
     * @return formatted duration
     */

    public static String formatDuration(long duration) {
        return DURATION_FORMATTER.print(new Period(0, duration * 1000));
    }

    /**
     * Counts build completion time.
     *
     * @param startTime moment when the build has started
     * @param duration  build duration
     * @return completion time
     */
    public static long countCompletionTime(long startTime, long duration) {
        long fixedDuration = duration == 0L
                ? System.currentTimeMillis() - startTime
                : duration;
        return startTime + fixedDuration;
    }
}
