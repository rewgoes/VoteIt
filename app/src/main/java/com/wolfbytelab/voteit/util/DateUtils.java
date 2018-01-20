package com.wolfbytelab.voteit.util;

import android.content.Context;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static final long DATE_NOT_SET = -1;

    public static String getFormattedDate(Context context, long date, boolean showTodayTime) {

        String dateString;

        if (showTodayTime && android.text.format.DateUtils.isToday(date)) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            dateString = timeFormat.format(new Date(date));
        } else {
            DateFormat timeFormat = android.text.format.DateFormat.getDateFormat(context);
            dateString = timeFormat.format(new Date(date));
        }

        return dateString;
    }

    public static String getFormattedTime(Context context, long time) {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        return timeFormat.format(new Date(time));
    }

    public static void startCalendar(Calendar calendar, long endDate) {
        if (endDate != DATE_NOT_SET) {
            calendar.setTimeInMillis(endDate);
        } else {
            clearCalendarTime(calendar);
        }
    }

    public static void clearCalendarTime(Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
        calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
    }
}
