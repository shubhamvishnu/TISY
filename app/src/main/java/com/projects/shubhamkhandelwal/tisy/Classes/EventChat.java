package com.projects.shubhamkhandelwal.tisy.Classes;

/**
 * Created by Shubham Khandelwal on 8/30/2016.
 */
public class EventChat {
    String username;
    String message;
    public EventChat(){}

    public EventChat(String username, String message){
        this.username = username;
        this.message = message;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
