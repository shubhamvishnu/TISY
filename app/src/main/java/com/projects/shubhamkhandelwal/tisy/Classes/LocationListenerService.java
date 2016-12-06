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

import com.firebase.client.Firebase;

import java.sql.Time;

/**
 * Created by Shubham Khandelwal on 12/6/2016.
 */
public class LocationListenerService extends Service {
    LocationManager locationManager; // reference for location manager object.
    String username;
    boolean runLoop;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runLoop = true;
        username = getBaseContext().getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

            init();

        return START_STICKY;
    }
    void init() {

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the cas    e where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
    init();
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0,0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        String dateMonthYear = TimeStamp.getRawTime();
                        Firebase userLocationLogFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog/"+ dateMonthYear);
                        userLocationLogFirebase.keepSynced(true);
                        LocationLog locationLog = new LocationLog();
                        locationLog.setLatitude(String.valueOf(location.getLatitude()));
                        locationLog.setLongitude(String.valueOf(location.getLongitude()));
                        userLocationLogFirebase.push().setValue(locationLog);
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
                });
    }

}