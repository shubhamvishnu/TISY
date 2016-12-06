package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.projects.shubhamkhandelwal.tisy.MainActivity;

/**
 * Created by Shubham Khandelwal on 12/3/2016.
 */
public class MovementTracker implements LocationListener {
    Firebase firebase;
    Context context;
    public MovementTracker(Context context){
        this.context = context;
    }
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            Toast.makeText(context, "location" + location.getLatitude() + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }
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
