package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 1/7/2017.
 */
public class LocationNote {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    String title;
    String description;
    String latitude;
    String longitude;

    public LocationNote(String title, String description, String latitude, String longitude) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
