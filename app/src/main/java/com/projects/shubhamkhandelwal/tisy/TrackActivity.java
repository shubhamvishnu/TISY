package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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

import com.firebase.client.ChildEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationLog;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationNote;
import com.projects.shubhamkhandelwal.tisy.Classes.Note;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.projects.shubhamkhandelwal.tisy.Classes.TimeStamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    GoogleMap mMap;
    String username;
    List<LocationLog> locationLogList;
    ImageButton addNoteImageButton;
    // intialization for place picker dialog
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder placePickerBuilder;
    EditText addNoteTitleEditText;
    Boolean fromDialog;

    Map<Integer, Note> tagNoteMap;
    CoordinatorLayout trackCoordinatorLayout;
    LatLngBounds.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        fromDialog = false;

        tagNoteMap = new HashMap<>();
        builder = new LatLngBounds.Builder();
        locationLogList = new ArrayList<>();
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);
        addNoteImageButton = (ImageButton) findViewById(R.id.track_activity_add_note);
        trackCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.trackCoordinatorLayout);
        addNoteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placePickerDialog();
                // showAddNoteDialog();
            }
        });
        initColors();


    }

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
        Firebase saveNoteFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/" + TimeStamp.getRawTime());
        saveNoteFirebase.keepSynced(true);
        LocationNote locationNote = new LocationNote(locationTitle, locationDesc, String.valueOf(location.latitude), String.valueOf(location.longitude));
        saveNoteFirebase.push().setValue(locationNote);
    }

    void showNotes() {
        Firebase saveNoteFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/" + TimeStamp.getRawTime());
        saveNoteFirebase.keepSynced(true);
        saveNoteFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("longitude").getValue().toString()));

                Note note = new Note();
                note.setDesc(dataSnapshot.child("description").getValue().toString());
                note.setTitle(dataSnapshot.child("title").getValue().toString());
                note.setKey(dataSnapshot.getKey());

                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(note.getTitle()).snippet(note.getDesc()).icon(BitmapDescriptorFactory.fromBitmap(InitIcon.getBitmapFromVectorDrawable(TrackActivity.this, R.drawable.note_pin_icon))));


                builder.include(latLng);

                int tag = tagNoteMap.size();
                marker.setTag(tag);
                tagNoteMap.put(tag, note);
                zoomFit();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
       init();
        mMap.setOnMarkerClickListener(this);
    }

    void init(){
        builder = new LatLngBounds.Builder();
        mMap.clear();
        tagNoteMap = new HashMap<>();
        initLatLngs();
        showNotes();
    }
    String getCustomColor(int customColor) {
        if (customColor > 8) {
            customColor = customColor % 8;
            Log.d("custom color : ", "" + customColor);
        }
        return Constants.colors.get(customColor);
    }

    void initLatLngs() {
        Firebase initLatLng = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog/" + TimeStamp.getRawTime());
        initLatLng.keepSynced(true);
        initLatLng.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    locationLogList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        locationLogList.add(snapshot.getValue(LocationLog.class));
                    }
                    showTrack();
                } else {

                }

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


    void showTrack() {

        // int initialHour = Integer.parseInt(locationLogList.get(0).getHourAndMinute().split(":")[0]);
        Bitmap positionMarkerBitmap = InitIcon.getBitmapFromVectorDrawable(this, R.drawable.timeline_position_marker_icon);
        for (int i = 0; i < locationLogList.size(); i++) {
            LocationLog locationLog = locationLogList.get(i);
            int hour = Integer.parseInt(locationLog.getHourAndMinute().split(":")[0]);

            LatLng coordinate = new LatLng(Double.parseDouble(locationLog.getLatitude()), Double.parseDouble(locationLog.getLongitude()));
            builder.include(coordinate);


            positionMarkerBitmap = applyCustomBitmapColor(positionMarkerBitmap, getCustomColor(hour));
            mMap.addMarker(new MarkerOptions().position(coordinate).title("Here at " + locationLog.getHourAndMinute()).icon(BitmapDescriptorFactory.fromBitmap(positionMarkerBitmap)));
        }
        zoomFit();
    }

    void zoomFit(){
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer markerTag = (Integer) marker.getTag();
        if(tagNoteMap.containsKey(markerTag)){
            showNoteDialog(markerTag);
        }
        return false;
    }
    void showNoteDialog(final int markerTag){
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
    void deleteNote(String key, final Dialog dialog, final int markerTag){
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
}
