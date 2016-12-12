package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 12/6/2016.
 */
public class LocationListenerService extends Service {
    LocationManager locationManager; // reference for location manager object.
    String username;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        username = getBaseContext().getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        Toast.makeText(getBaseContext(), "service called" + username, Toast.LENGTH_SHORT).show();
        init();
        return START_STICKY;
    }



    void init() {
        Toast.makeText(getBaseContext(), "init called.", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 1, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 1, locationListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void updateLocationLog(LocationLog locationLog, String dateMonthYear){
        Firebase userLocationLogFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog/" + dateMonthYear + "/");
        userLocationLogFirebase.keepSynced(true);
        userLocationLogFirebase.push().setValue(locationLog);
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getBaseContext(), "location listener called" + location, Toast.LENGTH_SHORT).show();
            String dateMonthYear = TimeStamp.getRawTime();
            String hourAndMinute = TimeStamp.getHourAndMinute();

            LocationLog locationLog = new LocationLog();
            locationLog.setLatitude(String.valueOf(location.getLatitude()));
            locationLog.setLongitude(String.valueOf(location.getLongitude()));
            locationLog.setHourAndMinute(hourAndMinute);

            updateLocationLog(locationLog, dateMonthYear);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}