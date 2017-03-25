package com.projects.shubhamkhandelwal.tisy;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.projects.shubhamkhandelwal.tisy.Classes.ActiveEventsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.AllNotesRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationNote;
import com.projects.shubhamkhandelwal.tisy.Classes.Note;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import client.yalantis.com.foldingtabbar.FoldingTabBar;

public class TrackActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final int REQUEST_PERMISSION_SETTINGS = 1; // used for the permission setting intent
    GoogleMap mMap;
    String username;
    public static final int REQUEST_ACCESS_WRITE_STORAGE = 2;
    ImageButton addNoteImageButton;

    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder placePickerBuilder;
    EditText addNoteTitleEditText;
    Boolean fromDialog;
    Map<Integer, Note> tagNoteMap;
    CoordinatorLayout trackCoordinatorLayout;
    ImageButton trackActivityZoomFit;
    LatLngBounds.Builder builder;
    ProgressDialog progressDialog;
boolean fabOptionsClicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        fromDialog = false;

        tagNoteMap = new HashMap<>();

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


        initServices();

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

        initProgressDialog();

        FoldingTabBar tabBar = (FoldingTabBar) findViewById(R.id.folding_tab_bar_track_activity);
        tabBar.setOnFoldingItemClickListener(new FoldingTabBar.OnFoldingItemSelectedListener() {
            @Override
            public boolean onFoldingItemSelected(@NotNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.ftb_shutter_track_activity: {
                        checkForWriteStoragePermission();
                        break;
                    }
                    case R.id.ftb_list_track_activity: {
                        LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showGPSAlert();
                        } else {
                            if(tagNoteMap != null && tagNoteMap.size() > 0){
                                showNoteListDialog();
                            }

                        }
                        break;
                    }
                    case R.id.ftb_new_track_activity: {
                        if (ActivityCompat.checkSelfPermission(TrackActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            showPermissionAlert();
                        } else {
                            LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                showGPSAlert();
                            } else {
                                placePickerDialog();
                            }

                        }
                        break;
                    }
                    case R.id.ftb_zoom_fit_track_activity: {


                        LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showGPSAlert();
                        } else {
                            zoomFit();
                        }
                        break;
                    }

                }
                return false;
            }
        });
        fabOptionsClicked = false;

        tabBar.setOnMainButtonClickListener(new FoldingTabBar.OnMainButtonClickedListener() {
            @Override
            public void onMainButtonClicked() {
                if(fabOptionsClicked){
                    if(mMap !=null){
                        mMap.setPadding(0, 0, 0, 0);
                    }
                    fabOptionsClicked = false;
                }else{
                    if(mMap !=null){
                        mMap.setPadding(0, 0, 0, 320);
                        fabOptionsClicked = true;
                    }
                }

            }
        });
        tabBar.setBackground(getResources().getDrawable(R.drawable.active_members_recycler_view_item_background));


    }

    void showPermissionAlert(){
        Alerter.create(this)
                .setTitle("Enable location permission")
                .setText("TISY uses GPS to locate and track users. It required permission to use your GPS.")
                .setBackgroundColor(R.color.colorAccent)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings();
                    }
                })
                .show();
    }
    void initServices(){
        if(!Constants.LOCATION_NOTIFICATION_SERVICE_STATUS){
            startService(new Intent(getBaseContext(), LocationListenerService.class));
        }
        if(!Constants.CHAT_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), ChatNotificationService.class));
        }
        if(!Constants.REQUEST_NOTIFICATION_SERVICE_STATUS){
            startService(new Intent(getBaseContext(), RequestNotificationService.class));
        }
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

        ImageButton cancelImageButton = (ImageButton) dialog.findViewById(R.id.cancel_add_note_image_button);
        ImageButton editLocationImageButton = (ImageButton) dialog.findViewById(R.id.note_edit_location_image_button);


        addNoteTitleEditText = (EditText) dialog.findViewById(R.id.note_title_edit_text);
        final EditText noteDescriptionEditText = (EditText) dialog.findViewById(R.id.note_description_edit_text);


        ImageButton saveNoteButton = (ImageButton) dialog.findViewById(R.id.save_note_button);
        cancelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


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
                String locationNoteTitle = addNoteTitleEditText.getText().toString();
                if (!(locationDesc.isEmpty() || locationDesc == null || location == null || locationNoteTitle.isEmpty() || locationNoteTitle == null )) {
                    dialog.dismiss();
                    saveNote(location, locationNoteTitle, locationDesc);
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
    void showNoteListDialog(){

        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_add_note_layout);

        RecyclerView allNotesRecyclerView=(RecyclerView) findViewById(R.id.notes_recycler_view);


        allNotesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TrackActivity.this);
        allNotesRecyclerView.setLayoutManager(linearLayoutManager);

        AllNotesRecyclerViewAdapter allNotesRecyclerViewAdapter = new AllNotesRecyclerViewAdapter(TrackActivity.this, tagNoteMap);
        allNotesRecyclerView.setAdapter(allNotesRecyclerViewAdapter);


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
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LatLng latLng = new LatLng(Double.parseDouble(snapshot.child("latitude").getValue().toString()), Double.parseDouble(snapshot.child("longitude").getValue().toString()));
                        Note note = new Note();
                        note.setDesc(snapshot.child("description").getValue().toString());
                        note.setTitle(snapshot.child("title").getValue().toString());
                        note.setKey(snapshot.getKey());
                        note.setLatlng(latLng);

                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(note.getTitle()).snippet(note.getDesc()).icon(BitmapDescriptorFactory.fromBitmap(InitIcon.getCustomBitmapFromVectorDrawable(TrackActivity.this, R.drawable.my_places_location_marker_icon, 200, 200))));


                        int tag = tagNoteMap.size();
                        marker.setTag(tag);
                        tagNoteMap.put(tag, note);
                    }
                    zoomFit();
                }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        tagNoteMap = new HashMap<>();
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
            showGPSAlert();
        } else {
            initializeMap();
        }
    }

    void showGPSAlert() {
        Alerter.create(this)
                .setTitle("Turn on GPS")
                .setText("TISY uses GPS to locate and track users.")
                .setBackgroundColor(R.color.colorAccent)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGPSSettings();
                    }
                })
                .show();
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



    void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
    }

    void checkUserPermission() {
        // enable user location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setPadding(0, 0, 0, 200);
            }
            showPermissionAlert();

        } else {
            addNoteImageButton.setVisibility(View.VISIBLE);
            trackActivityZoomFit.setVisibility(View.VISIBLE);
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
        showNotes();
    }

    void zoomFit() {
        if (tagNoteMap != null && tagNoteMap.size() > 0) {


            builder = new LatLngBounds.Builder();

            for (Map.Entry entry : tagNoteMap.entrySet()) {
                Note note = (Note) entry.getValue();
                builder.include(note.getLatlng());
            }

            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cameraUpdate);
        }
    }


    void showNoteDialog(final int markerTag) {
        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_show_note_layout);

        TextView titleTextView, descTextView;
        ImageButton deleteNoteButton;
        ImageButton cancelImageButton;

        titleTextView = (TextView) dialog.findViewById(R.id.show_note_title);
        descTextView = (TextView) dialog.findViewById(R.id.show_note_desc);
        deleteNoteButton = (ImageButton) dialog.findViewById(R.id.delete_note_button);
        cancelImageButton = (ImageButton) dialog.findViewById(R.id.cancel_show_note_image_button);
        titleTextView.setText(tagNoteMap.get(markerTag).getTitle());
        descTextView.setText(tagNoteMap.get(markerTag).getDesc());

        cancelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
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
        Firebase deleteNote = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationNote/" + key);
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

    void toMainActivity() {
        Intent intent = new Intent(TrackActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void checkForWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkWritePermission();
        } else {

            progressDialog.show();
            captureScreen();
        }

    }
    void checkWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // toast the reason why we need the permission
                    showWritePermissionAlert();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_WRITE_STORAGE);
            }
        }
    }
    public void openShareImageDialog(String filePath) {
        File file = this.getFileStreamPath(filePath);

        if (!filePath.equals("")) {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        } else {
            Toast.makeText(getApplicationContext(), "share failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void captureScreen() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
//                bitmap = snapshot;
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + System.currentTimeMillis() + ".jpg";
//                try {
//
//                    File imageFile = new File(filePath);
//
//                    FileOutputStream outputStream = new FileOutputStream(imageFile);
//                    int quality = 100;
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//                    outputStream.flush();
//                    outputStream.close();
//
//                   openShareImageDialog(fi);
//
//                } catch (Throwable e) {
//                    // Several error may come out with file handling or OOM
//                    e.printStackTrace();
//                }


                bitmap = snapshot;

                OutputStream fout = null;

                String filePath = System.currentTimeMillis() + ".jpeg";

                try {
                    fout = openFileOutput(filePath,
                            MODE_WORLD_READABLE);

                    // Write the string to the file
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                    fout.flush();
                    fout.close();
                } catch (FileNotFoundException e) {

                } catch (IOException e) {

                }

                openShareImageDialog(filePath);

            }
        };

        mMap.snapshot(callback);
    }
    void showWritePermissionAlert() {
        Alerter.create(TrackActivity.this)
                .setTitle("Enable Write Permission")
                .setText("To take screen shots of the view, give Tisy write permission.")
                .setBackgroundColor(R.color.colorPrimaryDark)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ACCESS_WRITE_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showWritePermissionAlert();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Screenshot");
        progressDialog.setMessage("capturing snapshot of the view for you...");
        progressDialog.setCancelable(false);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });

    }
}