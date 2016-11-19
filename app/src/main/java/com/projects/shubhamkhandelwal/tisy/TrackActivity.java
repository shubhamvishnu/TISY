package com.projects.shubhamkhandelwal.tisy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationLog;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    String trackUsername;
    GoogleMap mMap;
    List<LatLng> locationPoints;
    LatLngBounds.Builder cameraLatLngbuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        trackUsername = getIntent().getStringExtra("username");
        Toast.makeText(TrackActivity.this, "username : "+trackUsername, Toast.LENGTH_SHORT).show();
        initializePoints();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    public void initializePoints() {
        cameraLatLngbuilder = new LatLngBounds.Builder();
        locationPoints = new ArrayList<>();
        Firebase userLocationPointsFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + trackUsername + "/locationLog");
        userLocationPointsFirebase.keepSynced(true);
        userLocationPointsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long childrenCount = dataSnapshot.getChildrenCount();
                Toast.makeText(TrackActivity.this, "child count : "+ childrenCount, Toast.LENGTH_SHORT).show();
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LocationLog locationLog = snapshot.getValue(LocationLog.class);
                        Double latitude = Double.parseDouble(locationLog.getLatitude());
                        Double longitude = Double.parseDouble(locationLog.getLongitude());
                        if (!((latitude == 0.0) && (longitude == 0.0))) {
                            LatLng latLng = new LatLng(latitude, longitude);
                            locationPoints.add(latLng);
                            cameraLatLngbuilder.include(latLng);
                        }
                    }
                    showPolyline();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    void showPolyline(){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(locationPoints);
        polylineOptions.width(25);
        polylineOptions.color(Color.RED);
        polylineOptions.geodesic(false);
        mMap.addPolyline(polylineOptions);

        LatLngBounds bounds = cameraLatLngbuilder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);
    }
}
