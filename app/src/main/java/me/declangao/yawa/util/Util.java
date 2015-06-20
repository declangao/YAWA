package me.declangao.yawa.util;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class. Holds useful methods used throughout the app
 */
public class Util {

    /**
     * Convert timestamp to a date string
     * @param timestamp Unix timestamp
     * @return date in a more friendly string format
     */
    public static String timestampToDate(long timestamp) {
        String[] months = new String[] {
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
        };
        String[] days = new String[] {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };

        // Multiply by 1000 to convert timestamp to milliseconds
        Date date = new Date(timestamp * 1000);
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return (days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + cal.get(Calendar.DATE) + " " + months[cal.get(Calendar.MONTH)]);
        //return date.toString();
    }

    /**
     * Convert timestamp to time of the day.
     * The time is based on the timezone of your device. Because when you search the sunrise and
     * sunset time of some other country, you probably want to know when the time is in your own
     * timezone. Say, you want to call your family in New York and you don't know if they are up
     * yet. A sunrise time in your own timezone is more helpful compared to local time.
     * @param timestamp Unix timestamp
     * @return time string in hh:mm format
     */
    public static String timestampToTimeOfTheDay(long timestamp) {
        Date date = new Date(timestamp * 1000);
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        String minute = String.valueOf(cal.get(Calendar.MINUTE));
        return (cal.get(Calendar.HOUR_OF_DAY) + ":" + (minute.length() < 2 ? "0" + minute : minute));
    }

    /**
     * The weather API sometimes returns a very long decimal number for temperature,
     * which affects the overall look and feel of the app.
     * This method is created to fix that.
     * @param num original temperature number
     * @return temperature number in #.# format
     */
    public static double formatTemperature(double num) {
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.valueOf(df.format(num));
    }

}
