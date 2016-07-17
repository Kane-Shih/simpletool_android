package tw.kaneshih.simpletool.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;

public class TextUtil {

    public static String getLinkHtml(String url, String text) {
        if (Validator.isEmpty(url) || Validator.isEmpty(text)) {
            return null;
        }
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    /**
     * @param context
     * @param milliseconds
     * @return 24 hour : yyyy/MM/dd HH:mm:ss 12 hour: yyyy/MM/dd hh:mm:ss aa
     */

    public static String getMillisecondsString(Context context, int milliseconds) {
        if (Validator.isNull(context)) {
            return null;
        }

        Date date = new Date(milliseconds);
        if (DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(date);
        } else {
            return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa", Locale.US).format(date);
        }
    }

    /**
     * @param context
     * @param timestamp in second
     * @return 24 hour : yyyy/MM/dd HH:mm:ss 12 hour: yyyy/MM/dd hh:mm:ss aa
     */

    public static String getTimestampString(Context context, int timestamp) {
        return getMillisecondsString(context, timestamp * 1000);
    }

    /**
     * @param sec
     * @return HH:MM:SS
     */
    public static String convertSecondToHHMMSS(int sec) {
        int hours = sec / 3600;
        int minutes = (sec % 3600) / 60;
        int seconds = sec % 60;
        StringBuilder builder = new StringBuilder();

        if (hours > 0) {
            builder.append(String.format(Locale.US, "%02d", hours)).append(":");
        } else if (hours == 0) {
            builder.append(String.format(Locale.US, "%02d", hours)).append(":");
        }

        if (minutes > 0) {
            builder.append(String.format(Locale.US, "%02d", minutes)).append(":");
        } else if (hours == 0) {
            builder.append(String.format(Locale.US, "%02d", minutes)).append(":");
        }
        if (seconds > 0) {
            builder.append(String.format(Locale.US, "%02d", seconds));
        } else if (seconds == 0) {
            builder.append(String.format(Locale.US, "%02d", seconds));
        }
        return builder.toString();
    }

    /**
     * @param number
     * @return EX: 1000 ->1,000
     */
    public static String convertNumberWithDot(int number) {
        return String.format(Locale.US, "%1$,1d", number);
    }

    public static String convertFormatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":") + String.format(Locale.US, "%02d:%02d", minutes % 60, seconds % 60);
    }

}
