package com.projects.shubhamkhandelwal.tisy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.projects.shubhamkhandelwal.tisy.Classes.CheckInPoints;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationLog;
import com.projects.shubhamkhandelwal.tisy.Classes.TimeStamp;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    String trackUsername;
    GoogleMap mMap;
    List<LatLng> locationPoints;
    List<String> timePoints;
    List<CheckInPoints> checkInPoints;
    List<Integer> timeLog;
    LatLngBounds.Builder cameraLatLngbuilder;
    String dateMonthYear;

    RecyclerView dateMonthYearRecyclerView;
    CoordinatorLayout trackCoordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        trackCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.trackCoordinatorLayout);
        trackUsername = getIntent().getStringExtra("username");

        dateMonthYear = TimeStamp.getRawTime();
        Toast.makeText(TrackActivity.this, "username : " + trackUsername, Toast.LENGTH_SHORT).show();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);


        initializeDateRecyclerView(trackUsername);


    }

    void initializeDateRecyclerView(String username) {
        dateMonthYearRecyclerView = (RecyclerView) findViewById(R.id.date_track_activity_recycler_view);
        dateMonthYearRecyclerView.setHasFixedSize(true);
        DateRecyclerViewAdapter dateRecyclerViewAdapter = new DateRecyclerViewAdapter(this, username);
        dateMonthYearRecyclerView.setAdapter(dateRecyclerViewAdapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dateMonthYearRecyclerView.setLayoutManager(layoutManager);
    }

    int getCustomColor(int customColor) {

        if (customColor < 8) {
            customColor = customColor % 8;
        }
        Constants.colors.add(R.color.customColor0);
        Constants.colors.add(R.color.customColor1);
        Constants.colors.add(R.color.customColor2);
        Constants.colors.add(R.color.customColor3);
        Constants.colors.add(R.color.customColor4);
        Constants.colors.add(R.color.customColor5);
        Constants.colors.add(R.color.customColor6);
        Constants.colors.add(R.color.customColor7);
        Constants.colors.add(R.color.customColor8);

        return Constants.colors.get(customColor);
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

    public void initializePoints(String user, final String dateMonthYear) {
        cameraLatLngbuilder = new LatLngBounds.Builder();

        locationPoints = new ArrayList<>();
        timePoints = new ArrayList<>();
        timeLog = new ArrayList<>();

        Firebase userLocationPointsFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + user + "/locationLog/" + dateMonthYear);
        userLocationPointsFirebase.keepSynced(true);
        userLocationPointsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long childrenCount = dataSnapshot.getChildrenCount();
                Toast.makeText(TrackActivity.this, "child count : " + childrenCount, Toast.LENGTH_SHORT).show();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LocationLog locationLog = snapshot.getValue(LocationLog.class);
                        Double latitude = Double.parseDouble(locationLog.getLatitude());
                        Double longitude = Double.parseDouble(locationLog.getLongitude());
                        if (!((latitude == 0.0) && (longitude == 0.0))) {
                            LatLng latLng = new LatLng(latitude, longitude);
                            locationPoints.add(latLng);
                            timePoints.add(locationLog.getHourAndMinute());
                            cameraLatLngbuilder.include(latLng);
                            timeLog.add(Integer.parseInt(locationLog.getHourAndMinute().split(":")[0]));
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


    void showPolyline() {
        mMap.clear();
        // TODO: show start time line location
        // TODO show end time line location

        int initialTime = timeLog.get(0);

        PolylineOptions polylineOptions = new PolylineOptions();
        List<LatLng> latLngList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();

        latLngList.add(locationPoints.get(0));
        timeList.add(timePoints.get(0));

        int size = locationPoints.size();

        for (int i = 0; i < size; i++) {
            polylineOptions.add(locationPoints.get(i));

            if (initialTime < timeLog.get(i)) {
                initialTime = timeLog.get(i);
                latLngList.add(locationPoints.get(i));
                timeList.add(timePoints.get(i));
            }
        }
        latLngList.add(locationPoints.get((latLngList.size() - 1)));
        timeList.add(timePoints.get((timePoints.size()-1)));
        showLines(polylineOptions);
        showHourChangeMarkers(latLngList, timeList);


    }

    void showHourChangeMarkers(List<LatLng> latLngList,List<String> timeList) {

        for (int i = 0; i < latLngList.size(); i++) {
            if (i == 0) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLngList.get(i))
                        .title(timeList.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            } else if (i == (latLngList.size() - 1)) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLngList.get(i))
                        .title(timeList.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(latLngList.get(i))
                        .title(timeList.get(i)));
            }
        }
        zoomFitMap();
    }

    void showLines(PolylineOptions polylineOptions) {
        polylineOptions.width(20);
        polylineOptions.color(R.color.customColor2);
        polylineOptions.geodesic(true);

        mMap.addPolyline(polylineOptions).setPoints(locationPoints);

        zoomFitMap();
    }


    void zoomFitMap() {
        LatLngBounds bounds = cameraLatLngbuilder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    class DateRecyclerViewAdapter extends RecyclerView.Adapter<DateRecyclerViewAdapter.DateRecyclerViewHolder> {
        Context context;
        List<String> dateMonthYearList;
        String memberName;
        private LayoutInflater inflator;

        public DateRecyclerViewAdapter(Context context, String memberName) {
            this.context = context;
            this.memberName = memberName;
            inflator = LayoutInflater.from(context);
            dateMonthYearList = new ArrayList<>();

            initList();

        }

        void showSnackBar() {
            if (mMap != null) {
                mMap.setPadding(0, 0, 0, 200);
            }
            Snackbar snackbar = Snackbar
                    .make(trackCoordinatorLayout, "No movement has been recorded", Snackbar.LENGTH_INDEFINITE)
                    .setAction("GO BACK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(TrackActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
            snackbar.setCallback(new Snackbar.Callback() {

                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (mMap != null) {
                        mMap.setPadding(0, 0, 0, 0);
                    }
                }

                @Override
                public void onShown(Snackbar snackbar) {


                }
            });
            snackbar.setActionTextColor(Color.parseColor("#009688"));
            snackbar.show();

        }

        void initList() {
            Firebase dateMonthYearFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + memberName + "/locationLog/");
            dateMonthYearFirebase.keepSynced(true);
            Toast.makeText(TrackActivity.this, "username :" + memberName, Toast.LENGTH_SHORT).show();
            dateMonthYearFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int position = 0;
                    if (dataSnapshot.hasChildren()) {
                        dateMonthYearList = new ArrayList<String>();
                        Toast.makeText(TrackActivity.this, dataSnapshot.getChildrenCount() + "", Toast.LENGTH_SHORT).show();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            int pos = dateMonthYearList.size();
                            dateMonthYearList.add(snapshot.getKey());
                            notifyItemInserted(pos);
                            position = pos;
                        }
                        initializePoints(trackUsername, dateMonthYearList.get(position));
                    } else {
                        dateMonthYearRecyclerView.setVisibility(View.GONE);
                        showSnackBar();

                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        @Override
        public DateRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recycler_view_date_month_year_row_layout, parent, false);
            DateRecyclerViewHolder viewHolder = new DateRecyclerViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(DateRecyclerViewHolder holder, int position) {
            holder.dateMonthYearButton.setText(dateMonthYearList.get(position));
        }

        @Override
        public int getItemCount() {
            return dateMonthYearList.size();
        }

        class DateRecyclerViewHolder extends RecyclerView.ViewHolder {
            Button dateMonthYearButton;

            public DateRecyclerViewHolder(View itemView) {
                super(itemView);
                dateMonthYearButton = (Button) itemView.findViewById(R.id.date_month_year_button);
                dateMonthYearButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateMonthYear = dateMonthYearList.get(getPosition());
                        initializePoints(trackUsername, dateMonthYear);
                    }
                });
            }
        }

    }
}
