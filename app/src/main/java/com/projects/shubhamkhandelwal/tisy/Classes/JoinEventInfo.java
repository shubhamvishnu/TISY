package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 9/25/2016.
 */
public class JoinEventInfo {

    String description;
    String eventId;

    public JoinEventInfo(String eventId, String description) {
        this.eventId = eventId;
        this.description = description;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

}
