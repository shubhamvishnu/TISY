package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 8/15/2016.
 */
public class RequestsDetails {

    private String username;
    private String description;

    public RequestsDetails(String username, String description) {
        this.username = username;
        this.description = description;

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
