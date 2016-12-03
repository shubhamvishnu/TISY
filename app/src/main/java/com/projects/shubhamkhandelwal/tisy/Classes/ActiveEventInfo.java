package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 9/22/2016.
 */
public class ActiveEventInfo {
    private String title;

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    private String association;


    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    private  String timeCreated;

    public String getRequests() {
        return requests;
    }

    public void setRequests(String requests) {
        this.requests = requests;
    }

    private String requests;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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



    public ActiveEventInfo(String title, String association, String eventId, EventInfo eventInfo, String requests, String timeCreated) {
        this.association = association;
        this.title = title;
        this.eventId = eventId;
        this.eventInfo = eventInfo;
        this.requests = requests + " pending requests";
        this.timeCreated = timeCreated;
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
