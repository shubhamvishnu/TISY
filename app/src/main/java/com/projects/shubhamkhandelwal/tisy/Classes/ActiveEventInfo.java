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
        this.timeCreated = timeCreated;
        this.memberList = memberList;
    }

    public List<String> getMemberList() {
        return memberList;
    }

    public String getAssociation() {
        return association;
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



    public String getEventId() {
        return eventId;
    }


}
