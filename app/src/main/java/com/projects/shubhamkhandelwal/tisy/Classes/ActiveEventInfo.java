package com.projects.shubhamkhandelwal.tisy.Classes;

import java.util.List;

/**
 * Created by Shubham Khandelwal on 9/22/2016.
 */
public class ActiveEventInfo {
    private String title;
    private String eventId;
    private EventInfo eventInfo;
    private String admin;
    private int dIconResourceId;
    private String association;
    private String timeCreated;
    //private String requests;
    private List<String> memberList;

    public ActiveEventInfo(String title, String association, String eventId, EventInfo eventInfo, String timeCreated, List<String> memberList) {
        this.association = association;
        this.title = title;
        this.eventId = eventId;
        this.eventInfo = eventInfo;
//        if (Integer.parseInt(requests) == 0) {
//            this.requests = "No pending requests";
//        } else {
//            this.requests = requests + " pending requests";
//        }
        this.timeCreated = timeCreated;
        this.memberList = memberList;
    }

    public List<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<String> memberList) {
        this.memberList = memberList;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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
