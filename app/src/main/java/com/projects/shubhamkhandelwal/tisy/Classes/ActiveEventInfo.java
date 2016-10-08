package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 9/22/2016.
 */
public class ActiveEventInfo {
    private String eventId;
    private EventInfo eventInfo;
    private String admin;

    private int dIconResourceId;
    public int getdIconResourceId() {
        return dIconResourceId;
    }

    public void setdIconResourceId(int dIconResourceId) {
        this.dIconResourceId = dIconResourceId;
    }


    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }



    public ActiveEventInfo(String eventId, EventInfo eventInfo) {
        this.eventId = eventId;
        this.eventInfo = eventInfo;
    }

    public EventInfo getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
