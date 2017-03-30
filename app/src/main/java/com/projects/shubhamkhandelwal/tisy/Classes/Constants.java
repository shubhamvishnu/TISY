package com.projects.shubhamkhandelwal.tisy.Classes;

import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 8/14/2016.
 */
public class Constants {

    public static String currentEventId = ""; // holds reference to the eventID of the currently active event
    public static boolean eventAdmin = false; // holds reference to the eventID of the currently active event
    public static List<String> colors = new ArrayList<>();


    public static int dIconResourceId = -1;


    public static int TYPE_ALL_EVENTS = 1000;
    public static int TYPE_DELETE_MEMBERS = 1001;
    public static int TYPE_RECEIVED_REQUESTS = 1002;
    public static int TYPE_SENT_REQUESTS = 1003;



    public static String START_LOCATION_TAG = "start_location_marker";
    public static String DESTINATION_LOCATION_TAG = "destination_location_marker";


    public static int TYPE_MAP_STYLE_DEFAULT = 8000;
    public static int TYPE_MAP_STYLE_AUBERGINE = 8001;
    public static int TYPE_MAP_STYLE_NIGHT = 8002;
    public static int TYPE_MAP_STYLE_RETRO = 8003;
    public static int TYPE_MAP_STYLE_DARK = 8004;
    public static int TYPE_MAP_STYLE_SILVER = 8005;

    public static int TYPE_MAP_NORMAL = 9000;
    public static int TYPE_MAP_SATELLITE = 9001;
    public static int TYPE_MAP_HYBRID = 9002;
    public static int TYPE_MAP_TERRAIN = 9003;


    public static int UNREAD_CHATS_NOTIFICATION_ID = 1100;

    public static boolean CHAT_NOTIFICATION_SERVICE_STATUS = false;
    public static boolean REQUEST_NOTIFICATION_SERVICE_STATUS = false;
    public static boolean LOCATION_NOTIFICATION_SERVICE_STATUS = false;


    public static String LOGIN_TYPE_FACEBOOK = "facebook";
    public static String LOGIN_TYPE_GOOGLE = "google";





}
