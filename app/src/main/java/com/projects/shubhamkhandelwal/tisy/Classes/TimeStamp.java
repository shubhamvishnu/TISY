package com.projects.shubhamkhandelwal.tisy.Classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Shubham Khandelwal on 10/10/2016.
 */
public class TimeStamp {
    public static String getTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM HH:mm", Locale.getDefault());
        Date date = new Date();
        return "Created at: " + dateFormat.format(date);

    }
    public static String getRawTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    public static String getLastSeen(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat(
                "HH:mm", Locale.getDefault());
      Date date = new Date();
        return "last updated: at " + timeFormat.format(date)+ " on " + dateFormat.format(date);
    }

    public static String getHour(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH", Locale.getDefault());
        return dateFormat.format(dateFormat);
    }
    public static String getHourAndMinute(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
