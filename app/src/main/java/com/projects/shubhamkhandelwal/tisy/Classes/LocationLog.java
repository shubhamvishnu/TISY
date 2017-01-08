package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 10/23/2016.
 */
public class LocationLog {

    String latitude;
    String longitude;
    String hourAndMinute;

    public int getCustomColor() {
        return customColor;
    }

    public void setCustomColor(int customColor) {
        this.customColor = customColor;
    }

    int customColor;

    public String getHourAndMinute() {
        return hourAndMinute;
    }

    public void setHourAndMinute(String hourAndMinute) {
        this.hourAndMinute = hourAndMinute;
    }




    //
//    public LocationLog(String latitude, String longitude) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
