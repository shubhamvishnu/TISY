package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shubham Khandelwal on 12/6/2016.
 */
public class LocationListenerService extends Service {
    LocationManager locationManager; // reference for location manager object.
    String username;
    int customColor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constants.LOCATION_NOTIFICATION_SERVICE_STATUS =  true;
        username = getBaseContext().getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        init();
        return START_STICKY;
    }

    void init() {
        customColor = 0;

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Constants.LOCATION_NOTIFICATION_SERVICE_STATUS =  false;
    }


    void updateEventPosition(final Location location) {
        Firebase getUserActiveEvents = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent/");
        getUserActiveEvents.keepSynced(true);
        getUserActiveEvents.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateUserPosition(dataSnapshot.getKey(), location);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    void updateUserPosition(String eventID, final Location location) {

        final Firebase updateUserCurrentLocationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + eventID + "/members");
        updateUserCurrentLocationFirebase.keepSynced(true);
        updateUserCurrentLocationFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (checkInternetConnection()) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> currentLocation = new HashMap<>();
                        currentLocation.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), location.getLatitude() + "," + location.getLongitude());
                        updateUserCurrentLocationFirebase.updateChildren(currentLocation);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getAccuracy() >= 10 && location.getAccuracy() != 0.0) {
                updateEventPosition(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }


        @Override
        public void onProviderEnabled(String s) {
            Firebase updateLastKnowStatus = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username);
            updateLastKnowStatus.keepSynced(true);
            Map<String, Object> lastSeenMap = new HashMap<>();
            lastSeenMap.put("lastSeen", TimeStamp.getLastSeen());
            updateLastKnowStatus.updateChildren(lastSeenMap);


        }

        @Override
        public void onProviderDisabled(String s) {
            Firebase updateLastKnowStatus = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username);
            updateLastKnowStatus.keepSynced(true);
            Map<String, Object> lastSeenMap = new HashMap<>();
            lastSeenMap.put("lastSeen", TimeStamp.getLastSeen());
            updateLastKnowStatus.updateChildren(lastSeenMap);


        }
    }
}