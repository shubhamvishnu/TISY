package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.firebase.client.Firebase;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by Shubham Khandelwal on 9/26/2016.
 */
public class Tisy extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2840079713824644~7949777217");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
