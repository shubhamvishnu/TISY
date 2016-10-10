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
                "MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);

    }
}
