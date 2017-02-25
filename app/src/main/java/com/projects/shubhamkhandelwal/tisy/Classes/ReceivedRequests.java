package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 2/26/2017.
 */
public class ReceivedRequests {

    private String username;
    private String description;
    private String eventID;

    public ReceivedRequests(String username, String description, String eventID) {
        this.username = username;
        this.description = description;
        this.eventID = eventID;


    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
