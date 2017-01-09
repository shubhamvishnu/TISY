package com.projects.shubhamkhandelwal.tisy.Classes;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Shubham Khandelwal on 1/8/2017.
 */
public class Note {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    LatLng latlng;
    String title;
    String desc;
    String key;

}
