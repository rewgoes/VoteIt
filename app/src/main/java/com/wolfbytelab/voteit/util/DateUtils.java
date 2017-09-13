package com.wolfbytelab.voteit.util;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

public class DateUtils {

    public static String getFormattedDate(Context context, long date) {

        String dateString;

        //TODO: decide how to parse a date
        //android.text.format.DateFormat.getDateFormat(context)
        //android.text.format.DateFormat.getTimeFormat(context)
        //android.text.format.DateFormat.is24HourFormat(this)
        //DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        if (android.text.format.DateUtils.isToday(date)) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            dateString = timeFormat.format(new Date(date));
        } else {
            DateFormat timeFormat = android.text.format.DateFormat.getDateFormat(context);
            dateString = timeFormat.format(new Date(date));
        }

        return dateString;
    }

}
