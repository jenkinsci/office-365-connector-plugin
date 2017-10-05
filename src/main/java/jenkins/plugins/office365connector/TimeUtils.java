package jenkins.plugins.office365connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

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
     * @param duration duration to convert
     * @return formatted duration
     */
    public static String durationToString(long duration) {
        long diff[] = new long[4];
        List<String> formats = new ArrayList<>();

        // sec
        diff[3] = (duration >= 60 ? duration % 60 : duration);
        if (diff[3] > 0) {
            formats.add(String.format("%d second%s", diff[3], diff[3] > 1 ? "s" : ""));
        }

        // min
        diff[2] = (duration = (duration / 60)) >= 60 ? duration % 60 : duration;
        if (diff[2] > 0) {
            formats.add(0, String.format("%d minute%s", diff[2], diff[2] > 1 ? "s" : ""));
        }

        // hours
        diff[1] = (duration = (duration / 60)) >= 24 ? duration % 24 : duration;
        if (diff[1] > 0) {
            formats.add(0, String.format("%d hour%s", diff[1], diff[1] > 1 ? "s" : ""));
        }

        // days
        diff[0] = duration / 24;
        if (diff[0] > 0) {
            formats.add(0, String.format("%d day%s", diff[0], diff[0] > 1 ? "s" : ""));
        }

        return StringUtils.join(formats, ", ");
    }
}
