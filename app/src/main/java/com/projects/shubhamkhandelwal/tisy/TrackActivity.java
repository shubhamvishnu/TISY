package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationNote;
import com.projects.shubhamkhandelwal.tisy.Classes.Note;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.projects.shubhamkhandelwal.tisy.Classes.TimeStamp;

import java.util.HashMap;
import java.util.Map;

public class TrackActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    GoogleMap mMap;
    String username;
    //List<LocationLog> locationLogList;
    ImageButton addNoteImageButton;
    ImageButton showCalendarImageButton;
    // intialization for place picker dialog
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder placePickerBuilder;
    EditText addNoteTitleEditText;
    Boolean fromDialog;

    Map<Integer, Note> tagNoteMap;
    CoordinatorLayout trackCoordinatorLayout;
    ImageButton trackActivityZoomFit;
    LatLngBounds.Builder builder;
    public static final int REQUEST_PERMISSION_SETTINGS = 1; // used for the permission setting intent


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        fromDialog = false;

        tagNoteMap = new HashMap<>();

        // locationLogList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            Toast.makeText(TrackActivity.this, "extras : " + username, Toast.LENGTH_SHORT).show();
        } else {
            username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);

        addNoteImageButton = (ImageButton) findViewById(R.id.track_activity_add_note);
        addNoteImageButton.setVisibility(View.INVISIBLE);

        trackCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.trackCoordinatorLayout);
        addNoteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placePickerDialog();
            }
        });

        trackActivityZoomFit = (ImageButton) findViewById(R.id.track_activity_zoom_fit_icon);
        trackActivityZoomFit.setVisibility(View.INVISIBLE);
        trackActivityZoomFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomFit();
            }
        });
        //initColors();


    }

    /* void deleteJunkTracks(){
         final Firebase junkTracksFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog/" );
         junkTracksFirebase.keepSynced(true);
         junkTracksFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if(dataSnapshot.hasChildren()){
                     for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                         if(!snapshot.getKey().equals(TimeStamp.getRawTime())){
                             Toast.makeText(TrackActivity.this, "deleted event date :" + snapshot.getKey(), Toast.LENGTH_SHORT).show();
                             junkTracksFirebase.child(snapshot.getKey()).removeValue();
                         }
                     }
                 }
             }

             @Override
             public void onCancelled(FirebaseError firebaseError) {

             }
         });
     }

 */

    // to call the placepicker dialog
    void placePickerDialog() {
        placePickerBuilder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(placePickerBuilder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // callback for place picker dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latLng = place.getLatLng();
                String locationDesc = place.getName().toString();
                if (fromDialog) {
                    addNoteTitleEditText.setText(locationDesc);
                    fromDialog = false;
                } else {
                    showAddNoteDialog(latLng, locationDesc);
                }
            }
        }
    }

    void showAddNoteDialog(final LatLng location, final String locationTitle) {
        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_add_note_layout);

        ImageButton editLocationImageButton = (ImageButton) dialog.findViewById(R.id.note_edit_location_image_button);
        addNoteTitleEditText = (EditText) dialog.findViewById(R.id.note_title_edit_text);
        final EditText noteDescriptionEditText = (EditText) dialog.findViewById(R.id.note_description_edit_text);
        Button saveNoteButton = (Button) dialog.findViewById(R.id.save_note_button);

        if (!(locationTitle == null || locationTitle.isEmpty())) {
            addNoteTitleEditText.setText(locationTitle);
        }
        editLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromDialog = true;
                placePickerDialog();
            }
        });
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationDesc = noteDescriptionEditText.getText().toString();
                if (!(locationDesc.isEmpty() || locationDesc == null || location == null)) {
                    dialog.dismiss();
                    saveNote(location, locationTitle, locationDesc);
                } else {
                    Toast.makeText(TrackActivity.this, "enter all the details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    void saveNote(LatLng location, String locationTitle, String locationDesc) {
        Firebase saveNoteFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/");
        saveNoteFirebase.keepSynced(true);
        LocationNote locationNote = new LocationNote(locationTitle, locationDesc, String.valueOf(location.latitude), String.valueOf(location.longitude));
        saveNoteFirebase.push().setValue(locationNote);
    }

    void showNotes() {
        Firebase saveNoteFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/");
        saveNoteFirebase.keepSynced(true);
        saveNoteFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tagNoteMap = new HashMap<Integer, Note>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("latitude").getValue().toString()), Double.parseDouble(snapshot.child("longitude").getValue().toString()));
                    Note note = new Note();
                    note.setDesc(snapshot.child("description").getValue().toString());
                    note.setTitle(snapshot.child("title").getValue().toString());
                    note.setKey(snapshot.getKey());
                    note.setLatlng(latLng);

                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(note.getTitle()).snippet(note.getDesc()).icon(BitmapDescriptorFactory.fromBitmap(InitIcon.getCustomBitmapFromVectorDrawable(TrackActivity.this, R.drawable.my_places_location_marker_icon, 150, 150))));


                    int tag = tagNoteMap.size();
                    marker.setTag(tag);
                    tagNoteMap.put(tag, note);
                }
                zoomFit();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer markerTag = (Integer) marker.getTag();
        if (tagNoteMap.containsKey(markerTag)) {
            showNoteDialog(markerTag);
        }
        return false;
    }

    /*
        void initColors() {
            Constants.colors.add(this.getString(R.string.customColor0));
            Constants.colors.add(this.getString(R.string.customColor1));
            Constants.colors.add(this.getString(R.string.customColor2));
            Constants.colors.add(this.getString(R.string.customColor3));
            Constants.colors.add(this.getString(R.string.customColor4));
            Constants.colors.add(this.getString(R.string.customColor5));
            Constants.colors.add(this.getString(R.string.customColor6));
            Constants.colors.add(this.getString(R.string.customColor7));
            Constants.colors.add(this.getString(R.string.customColor8));
        }
    */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initializeMapStyle();
        initializeMapType();
        GPSEnabledCheck();

    }

    void initializeMapStyle() {
        SharedPreferences mapSharedPreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        int styleType = mapSharedPreference.getInt("style", 0);
        if (!(styleType == 0)) {
            setMapStyle(styleType);
        }
    }

    void setMapType(int type) {

        if (type == Constants.TYPE_MAP_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } else if (type == Constants.TYPE_MAP_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        } else if (type == Constants.TYPE_MAP_TERRAIN) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        } else if (type == Constants.TYPE_MAP_HYBRID) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

    }

    void setMapStyle(int type) {

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = false;
            if (type == Constants.TYPE_MAP_STYLE_DEFAULT) {
                success = mMap.setMapStyle(null);
            } else if (type == Constants.TYPE_MAP_STYLE_AUBERGINE) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.aubergine_style_json));
            } else if (type == Constants.TYPE_MAP_STYLE_NIGHT) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.night_style_json));

            } else if (type == Constants.TYPE_MAP_STYLE_RETRO) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.retro_style_json));

            } else if (type == Constants.TYPE_MAP_STYLE_DARK) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.dark_style_json));

            } else if (type == Constants.TYPE_MAP_STYLE_SILVER) {
                success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.silver_style_json));
            }
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }

        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }


    }

    void initializeMapType() {
        SharedPreferences mapSharedPreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        int styleType = mapSharedPreference.getInt("type", 0);
        if (!(styleType == 0)) {
            setMapType(styleType);
        }
    }

    void GPSEnabledCheck() {
        // check for GPS is enabled, if not show snackbar, else just call the location Action
        LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (mMap != null) {
                mMap.setPadding(0, 0, 0, 200);
            }
            showSnackBar();
        } else {
            initializeMap();
        }
    }

    void initializeMap() {
        checkUserPermission();
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

    }
    void showPermissionSnackBar() {

        Snackbar snackbar = Snackbar
                .make(trackCoordinatorLayout, "enable location permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mMap != null) {
                            mMap.setPadding(0, 0, 0, 0);
                        }
                        openSettings();
                    }
                });

        snackbar.setActionTextColor(Color.parseColor("#009688"));
        snackbar.show();
    }

    void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
    }
void checkUserPermission(){
        // enable user location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setPadding(0, 0, 0, 200);
            }
            showPermissionSnackBar();
        } else {
            addNoteImageButton.setVisibility(View.VISIBLE);
            init();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {

                }
            });
    }
}
    void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(trackCoordinatorLayout, "enable location setting", Snackbar.LENGTH_INDEFINITE)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGPSSettings();
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

    void openGPSSettings() {
        if (mMap != null) {
            mMap.setPadding(0, 0, 0, 0);
        }
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    void init() {

        mMap.clear();
        tagNoteMap = new HashMap<>();
        //      locationLogList = new ArrayList<>();
        //     initLatLngs();
        showNotes();
    }

   /* String getCustomColor(int customColor) {
        if (customColor > 8) {
            customColor = customColor % 8;
            Log.d("custom color : ", "" + customColor);
        }
        return Constants.colors.get(customColor);
    } */
/*
    void initLatLngs() {

        Firebase initLatLng = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog/" + TimeStamp.getRawTime());
        initLatLng.keepSynced(true);
        initLatLng.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationLogList = new ArrayList<LocationLog>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    LocationLog locationLog = snapshot.getValue(LocationLog.class);
                    locationLogList.add(locationLog);
                    int hour = Integer.parseInt(locationLog.getHourAndMinute().split(":")[0]);
                    LatLng coordinate = new LatLng(Double.parseDouble(locationLog.getLatitude()), Double.parseDouble(locationLog.getLongitude()));

                    mMap.addMarker(new MarkerOptions().position(coordinate).title("Here at " + locationLog.getHourAndMinute()).icon(BitmapDescriptorFactory.fromBitmap(getTracksIcon(hour))));
                }
                zoomFit();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    Bitmap applyCustomBitmapColor(Bitmap myBitmap, String color) {

        Paint pnt = new Paint();
        Bitmap myBit = myBitmap;

        Canvas myCanvas = new Canvas(myBit);
        int myColor = myBit.getPixel(0, 0);

        // Set the colour to replace.
        // TODO: change color later
        ColorFilter filter = new LightingColorFilter(myColor, Color.parseColor(color));

        pnt.setColorFilter(filter);

        // Draw onto new bitmap. result Bitmap is newBit
        myCanvas.drawBitmap(myBit, 0, 0, pnt);

        return myBit;
    }
*/

    /*   Bitmap getTracksIcon(int hour){
           Bitmap positionMarkerBitmap = InitIcon.getCustomBitmapFromVectorDrawable(this, R.drawable.footstep_image_icon, 72, 72);
           positionMarkerBitmap = applyCustomBitmapColor(positionMarkerBitmap, getCustomColor(hour));
           return positionMarkerBitmap;
       }
   */
    void zoomFit() {
        builder = new LatLngBounds.Builder();

        for (Map.Entry entry : tagNoteMap.entrySet()) {
            Note note = (Note) entry.getValue();
            builder.include(note.getLatlng());
        }
//        for(int i = 0; i < locationLogList.size(); i++){
//            LatLng latLng = new LatLng(Double.parseDouble(locationLogList.get(i).getLatitude()), Double.parseDouble(locationLogList.get(i).getLongitude()));
//            builder.include(latLng);
//        }

        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);
    }


    void showNoteDialog(final int markerTag) {
        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_show_note_layout);

        TextView titleTextView, descTextView;
        Button deleteNoteButton;

        titleTextView = (TextView) dialog.findViewById(R.id.show_note_title);
        descTextView = (TextView) dialog.findViewById(R.id.show_note_desc);
        deleteNoteButton = (Button) dialog.findViewById(R.id.delete_note_button);

        titleTextView.setText(tagNoteMap.get(markerTag).getTitle());
        descTextView.setText(tagNoteMap.get(markerTag).getDesc());

        deleteNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote(tagNoteMap.get(markerTag).getKey(), dialog, markerTag);
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    void deleteNote(String key, final Dialog dialog, final int markerTag) {
        Firebase deleteNote = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/" + TimeStamp.getRawTime() + "/" + key);
        deleteNote.keepSynced(true);
        deleteNote.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Toast.makeText(TrackActivity.this, "note deleted.", Toast.LENGTH_SHORT).show();

                tagNoteMap.remove(markerTag);

                dialog.dismiss();

                init();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //   deleteJunkTracks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            GPSEnabledCheck();
        }
    }

    @Override
    public void onBackPressed() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            finish();
        } else {
           toMainActivity();
        }
    }
void toMainActivity(){
    Intent intent = new Intent(TrackActivity.this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    finish();
}
}
