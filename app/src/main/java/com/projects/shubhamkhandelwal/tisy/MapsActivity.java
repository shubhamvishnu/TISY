package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.projects.shubhamkhandelwal.tisy.Classes.ActiveMembersRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatsRecyclerViewAdpater;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventChat;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfoRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.EventMemberViewRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.EventMembersRecyclerViewAdapater;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.Note;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsDetails;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsRecyclerAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SearchResultsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SentEventJoinRequestRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    public static final int REQUEST_PERMISSION_SETTINGS = 1; // used for the permission setting intent
    public static final int PLACE_PICKER_REQUEST = 1; // used for the place picker intent
    public static boolean zoomFit; // if true: fits the specified LatLng into the view
    // request variables
    public static int numberOfRequests = 0; // number of join event request received
    String username; // to access the username variable throughout the activity
    PlacePicker.IntentBuilder builder; // PlacePicker Intent builder
    EventInfo eventInfo; // EventInfo class Object; All the basic event information
    GoogleMap mMap; // GoogleMap Object
    Firebase firebase; // Firebase Object
    // chat variables
    RecyclerView eventChatsRecyclerView; // chats view recyclerview
    ChatsRecyclerViewAdpater chatsRecyclerViewAdapter; // chats view recyclerview adapter
    // map event variables
    Map<String, Object> members; // members (usernames) in the event
    List<String> namesList; // names of members in the event
    Map<String, Object> memberLocationMarkers; // contains map of (username, marker object) to reference marker positions (LatLng) for every member
    Bitmap destinationIconBitmap; // holds the destination icon
    // checkpoint variables
    Map<String, Note> checkPointCoordinateMap; // contains all the checkpoints in the map; id and it's position (LatLng)
    List<String> checkPointsReached; // checkpoints crossed (reached) by the user
    Dialog requestsDialog; //  received requests dialog object
    List<RequestsDetails> joinRequests; // received requests; username list
    RecyclerView eventRequestRecyclerView; // received requests recyclerview
    RequestsRecyclerAdapter requestsRecyclerAdapter; // received requests recyclerview adapter

    boolean showGPSOption = true;

    int emoticon = 0;

    // View Objects
    CoordinatorLayout coordinatorLayout;

    // event infomation variables
    List<String> membersList; // members (username) in the event
    List<String> memberCoordinate; // coordinates (LatLng) of the members in the event
    List<String> memberProfileImageUrls; // profile Image URL of every member in the event
    List<String> lastSeenInfo;
    List<String> memberProfileName; // member name for users in the event
    String timeStamp; // date and time of when the event was created
    String eventTitle; // title of the event

    String destLocationTextView; // destination location description of the event
    String eventDescription; // event description
    int memberUriCount; // number of URL's fetched of the members

    // search variables
    String nameSearch; // name/username/eventID searched for by the user
    RecyclerView searchOptionChoiceRecyclerView; // search option recyclerview
    SearchResultsRecyclerViewAdapter searchResultsRecyclerViewAdapter; // search option recyclerview adapter

    List<String> eventMemberList;

    /**
     * converts the vector drawables to bitmap
     *
     * @param context    : to reference to the location of the vector in the res/drawable directory
     * @param drawableId : the id of the vector drawable in the res/drawable directory
     * @return : return the bitmap object of the vector drawable
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(120,
                102, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // set the title for the ActionBar
        setTitle("TISY");

        // initialize the GoogleMaps with the activity's context. To create custom icons for the markers.
        MapsInitializer.initialize(getApplicationContext());

        // initializing objects and variables
        // initalizing view objects
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        ImageButton allInOneImageIcon = (ImageButton) findViewById(R.id.allInOneOptionImageButton);
        allInOneImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGPS();
            }
        });
        // initializing variable
        //initializing String variables
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        nameSearch = new String();


        zoomFit = false;

        // initializing Class Objects
        eventInfo = new EventInfo();

        // initializing Collection Objects
        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
        lastSeenInfo = new ArrayList<>();
        memberProfileImageUrls = new ArrayList<>();
        memberProfileName = new ArrayList<>();
        checkPointsReached = new ArrayList<>();
        memberLocationMarkers = new HashMap<>();

        // TODO: change the service functions later; and optimize their usage

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        // callback is triggered when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
    }

    void init() {

        // requests received notification
        unreadRequestsInit();

        // initialize the destination icon
        destinationIconInit();

        // initialize the checkpoints in the map
        checkPointsInit();

        // show event members
        initializeMembers();
    }

    // initialize all the checkpoints for the event
    void checkPointsInit() {
        checkPointCoordinateMap = new HashMap<>();
        Firebase loadCheckPointsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints");
        loadCheckPointsFirebase.keepSynced(true);
        loadCheckPointsFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Note note = new Note();
                note.setTitle(dataSnapshot.child("title").getValue().toString());
                note.setDesc(dataSnapshot.child("desc").getValue().toString());
                note.setKey(dataSnapshot.child("key").getValue().toString());
                LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("latlng/latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("latlng/longitude").getValue().toString()));
                note.setLatlng(latLng);

                checkPointCoordinateMap.put(dataSnapshot.getKey(), note);
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

    /*  void loadEventInfo(){
          FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
          StorageReference imageStorageReference = firebaseStorage.getReferenceFromUrl("gs://fir-trio.appspot.com/" + Constants.currentEventId + "/dIcon");
`
          final long ONE_MEGABYTE = 1024 * 1024;
          imageStorageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
              @Override
              public void onSuccess(byte[] bytes) {
                  Toast.makeText(MapsActivity.this, bytes.toString(), Toast.LENGTH_SHORT).show();
                  destinationIcon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                  // Handle any errors
              }
          });
      }
      */

    public void destinationIconInit() {

        InitIcon destinationIconInit = new InitIcon();
        destinationIconBitmap = destinationIconInit.getDestinationIcon(this, Constants.dIconResourceId);
        // initialize the event information after initalizing the destination icon
        if (destinationIconBitmap != null) {
            initializeEventInfo();
        }
    }

    // initialize the event information
    void initializeEventInfo() {
        Firebase eventInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/info");
        eventInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                eventInfo.setdLocation(dataSnapshot.child("dLocation").getValue().toString());
                eventInfo.setdLocationDesc(dataSnapshot.child("dLocationDesc").getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // to apply custom color to a bitmap
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

    void showAllInOneDialog() {
        LinearLayout requestsLayout;
        LinearLayout sendRequestsLayout;
        final Dialog allInOneDialog = new Dialog(this, R.style.event_info_dialog_style);
        allInOneDialog.setContentView(R.layout.dialog_all_in_one_layout);

        ImageButton requestIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_request_icon);
        ImageButton chatIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_chat_icon);
        ImageButton zoomFitImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_zoom_fit_icon);
        ImageButton addNewMemberImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_add_new_member);
        ImageButton addNewCheckPointImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_add_new_checkpoint);
        ImageButton changeMapStyleImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_change_mode_icon);
        ImageButton mapTypeImageButton = (ImageButton) allInOneDialog.findViewById(R.id.map_type_option_icon);
        ImageButton infoImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_event_info);
        ImageButton leaveEventImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_leave_event_image_button);
        ImageButton suggestionImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_suggestion_image_button);
        requestsLayout = (LinearLayout) allInOneDialog.findViewById(R.id.requests_option_layout);
        sendRequestsLayout = (LinearLayout) allInOneDialog.findViewById(R.id.send_request_option_layout);

        if (!Constants.eventAdmin) {
            requestsLayout.setVisibility(View.GONE);
            sendRequestsLayout.setVisibility(View.GONE);
        }
        suggestionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                showSuggestionDialog();
            }
        });
        leaveEventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                exitEvent();
            }
        });
        infoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                eventInfoDialog();
            }
        });
        mapTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                showMapTypeOptionDialog();
            }
        });
        requestIconImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                showEventRequestDialog();
            }
        });
        chatIconImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                showChatsDialog();
            }
        });
        zoomFitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                zoomFitMembers();

            }
        });
        addNewMemberImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                sendMemberRequest();
            }
        });
        addNewCheckPointImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                addCheckPointDialog();
            }
        });
        changeMapStyleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allInOneDialog.dismiss();
                showMapStyleOptionsDialog();
            }
        });

        Window window = allInOneDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        allInOneDialog.setCanceledOnTouchOutside(true);
        allInOneDialog.show();
    }

    void showMapTypeOptionDialog() {
        final Dialog mapTypeDialog = new Dialog(this, R.style.event_info_dialog_style);
        mapTypeDialog.setContentView(R.layout.dialog_map_type_option_layout);
        ImageButton defaultMapTypeImageButton, terrainMapTypeImageButton, satelliteMapTypeImageButton, hybridMapTypeImageButton;
        defaultMapTypeImageButton = (ImageButton) mapTypeDialog.findViewById(R.id.map_type_default);
        terrainMapTypeImageButton = (ImageButton) mapTypeDialog.findViewById(R.id.map_type_terrain);
        satelliteMapTypeImageButton = (ImageButton) mapTypeDialog.findViewById(R.id.map_type_satellite);
        hybridMapTypeImageButton = (ImageButton) mapTypeDialog.findViewById(R.id.map_type_hybrid);


        defaultMapTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTypeDialog.dismiss();

                setMapType(Constants.TYPE_MAP_NORMAL);
            }
        });
        terrainMapTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTypeDialog.dismiss();

                setMapType(Constants.TYPE_MAP_TERRAIN);

            }
        });
        satelliteMapTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTypeDialog.dismiss();

                setMapType(Constants.TYPE_MAP_SATELLITE);

            }
        });
        hybridMapTypeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTypeDialog.dismiss();
                setMapType(Constants.TYPE_MAP_HYBRID);

            }
        });
        Window window = mapTypeDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        mapTypeDialog.setCanceledOnTouchOutside(true);
        mapTypeDialog.show();
    }

    void showMapStyleOptionsDialog() {

        final Dialog mapStyleDialog = new Dialog(this, R.style.event_info_dialog_style);
        mapStyleDialog.setContentView(R.layout.dialog_map_style_option_layout);
        ImageButton defaultModeButton, aubergineModeButton, retroModeButton, darkModeButton, nightModeButton, silverModeButton;

        defaultModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.default_mode);
        aubergineModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.aubergine_mode);
        nightModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.night_mode);
        retroModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.retro_mode);
        darkModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.dark_mode);
        silverModeButton = (ImageButton) mapStyleDialog.findViewById(R.id.silver_mode);

        defaultModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();
                setMapStyle(Constants.TYPE_MAP_STYLE_DEFAULT);
            }
        });
        aubergineModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();

                setMapStyle(Constants.TYPE_MAP_STYLE_AUBERGINE);
            }
        });
        nightModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();

                setMapStyle(Constants.TYPE_MAP_STYLE_NIGHT);
            }
        });
        retroModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();

                setMapStyle(Constants.TYPE_MAP_STYLE_RETRO);
            }
        });
        darkModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();

                setMapStyle(Constants.TYPE_MAP_STYLE_DARK);
            }
        });
        silverModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapStyleDialog.dismiss();

                setMapStyle(Constants.TYPE_MAP_STYLE_SILVER);
            }
        });


        Window window = mapStyleDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        mapStyleDialog.setCanceledOnTouchOutside(true);
        mapStyleDialog.show();
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

        storeMapType(type);

    }

    void storeMapType(int type) {
        SharedPreferences mapStylePreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor mapStyleEditor = mapStylePreference.edit();
        mapStyleEditor.putInt("type", type);
        mapStyleEditor.apply();
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
            } else {
                storeMapStyle(type);
            }

        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }


    }

    void storeMapStyle(int type) {
        SharedPreferences mapStylePreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor mapStyleEditor = mapStylePreference.edit();
        mapStyleEditor.putInt("style", type);
        mapStyleEditor.apply();
    }

    void addCheckPointDialog() {
        if (checkInternetConnection()) {
            placePickerDialog();
        } else {
            Toast.makeText(MapsActivity.this, "No internet connection, please try again later.", Toast.LENGTH_SHORT).show();

        }
    }

    void placePickerDialog() {
        builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                if (checkInternetConnection()) {
                    showCheckPointAddOptionDialog(place.getLatLng(), place.getName().toString());
                } else {


                    Toast.makeText(MapsActivity.this, "No internet connection, please try again later.", Toast.LENGTH_SHORT).show();
                }
                // saveCheckPoint(place.getLatLng().latitude, place.getLatLng().longitude);
            }
        }
    }

    void sendMemberRequest() {

        final Dialog sendMemberRequestDialog = new Dialog(this, R.style.event_info_dialog_style);
        sendMemberRequestDialog.setContentView(R.layout.dialog_send_request_from_event_layout);
        final LinearLayout eventJoinRequestRecyclerViewLinearLayout = (LinearLayout) sendMemberRequestDialog.findViewById(R.id.event_join_request_recycler_view);

        final LinearLayout noInvitesSentLinearLayout = (LinearLayout) sendMemberRequestDialog.findViewById(R.id.no_invites_sent_linear_layout);
        ImageButton searchButton = (ImageButton) sendMemberRequestDialog.findViewById(R.id.search_choice_dialog_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMemberRequestDialog.dismiss();
                showSearchOptionDialog(sendMemberRequestDialog.getContext());
            }
        });


        firebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId);
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    noInvitesSentLinearLayout.setVisibility(View.GONE);
                    eventJoinRequestRecyclerViewLinearLayout.setVisibility(View.VISIBLE);

                    RecyclerView eventJoinRequestSendRecyclerView = (RecyclerView) sendMemberRequestDialog.findViewById(R.id.dialog_event_join_request_sent_recycler_view);
                    eventJoinRequestSendRecyclerView.setLayoutManager(new LinearLayoutManager(sendMemberRequestDialog.getContext()));
                    eventJoinRequestSendRecyclerView.setHasFixedSize(true);

                    SentEventJoinRequestRecyclerViewAdapter adapter = new SentEventJoinRequestRecyclerViewAdapter(MapsActivity.this);
                    eventJoinRequestSendRecyclerView.setAdapter(adapter);

                } else {
                    noInvitesSentLinearLayout.setVisibility(View.VISIBLE);
                    eventJoinRequestRecyclerViewLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        Window window = sendMemberRequestDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        sendMemberRequestDialog.setCanceledOnTouchOutside(true);
        sendMemberRequestDialog.show();


    }

    void showSearchOptionDialog(Context context) {

        final EditText searchEditText;
        ImageButton searchButton;
        final Dialog searchOptionDialog = new Dialog(context, R.style.event_info_dialog_style);
        searchOptionDialog.setContentView(R.layout.dialog_search_option_layout);

        searchEditText = (EditText) searchOptionDialog.findViewById(R.id.search_option_choice_dialog_edit_text);
        searchButton = (ImageButton) searchOptionDialog.findViewById(R.id.search_option_choice_dialog_button);
        searchOptionChoiceRecyclerView = (RecyclerView) searchOptionDialog.findViewById(R.id.dialog_search_results_recycler_view);
        searchOptionChoiceRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(searchOptionDialog.getContext());
        searchOptionChoiceRecyclerView.setLayoutManager(linearLayoutManager);
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    {
                        nameSearch = searchEditText.getText().toString();
                        if (!nameSearch.trim().isEmpty()) {
                            searchResultsRecyclerViewAdapter = new SearchResultsRecyclerViewAdapter(searchOptionDialog.getContext(), nameSearch);
                            searchOptionChoiceRecyclerView.setAdapter(searchResultsRecyclerViewAdapter);
                        }
                    }
                }
                return false;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameSearch = searchEditText.getText().toString();
                if (!nameSearch.isEmpty()) {
                    searchResultsRecyclerViewAdapter = new SearchResultsRecyclerViewAdapter(searchOptionDialog.getContext(), nameSearch);
                    searchOptionChoiceRecyclerView.setAdapter(searchResultsRecyclerViewAdapter);
                }
            }
        });


        Window window = searchOptionDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        searchOptionDialog.setCanceledOnTouchOutside(true);
        searchOptionDialog.show();

    }


    void eventInfoDialog() {
        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
        timeStamp = new String();
        eventTitle = new String();
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.child("info").getChildren()) {
                    if (snapshot.getKey().equals("dLocationDesc")) {
                        destLocationTextView = snapshot.getValue().toString();
                    }
                }
                eventDescription = dataSnapshot.child("desc").getValue().toString();
                timeStamp = dataSnapshot.child("time").getValue().toString();
                eventTitle = dataSnapshot.child("title").getValue().toString();

                for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                    membersList.add(snapshot.getKey());
                    memberCoordinate.add(snapshot.getValue().toString());
                }
                //showEventInfoDialog();
                loadProfileInfo();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void loadProfileInfo() {
        lastSeenInfo = new ArrayList<>();
        memberProfileImageUrls = new ArrayList<>();
        memberProfileName = new ArrayList<>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching event details for you!");
        progressDialog.setCancelable(false);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                showEventInfoDialog();
            }
        });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        progressDialog.show();
        memberUriCount = 0;
        for (String name : membersList) {
            firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + name);
            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ++memberUriCount;
                    memberProfileImageUrls.add(dataSnapshot.child("userPhotoUri").getValue().toString());
                    memberProfileName.add(dataSnapshot.child("name").getValue().toString());
                    lastSeenInfo.add(dataSnapshot.child("lastSeen").getValue().toString());
                    if (membersList.size() == memberUriCount) {
                        progressDialog.dismiss();

                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }


    }

    void showEventInfoDialog() {

        TextView eventIdDialogTextView;

        TextView destLocationDialogTextView;
        TextView eventDescriptionTextView;
        TextView timeStampTextView;
        TextView titleTextView;
        RecyclerView eventInfoMembersRecyclerView;
        ImageButton editMembersImageButton;

        Dialog eventInfoDialog = new Dialog(this, R.style.event_info_dialog_style);
        eventInfoDialog.setContentView(R.layout.dialog_event_info_layout);

        eventIdDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.event_id_info_text_view);

        destLocationDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.dest_location_desc_text_view);
        eventDescriptionTextView = (TextView) eventInfoDialog.findViewById(R.id.event_desc_text_view);
        timeStampTextView = (TextView) eventInfoDialog.findViewById(R.id.time_stamp_text_view);
        titleTextView = (TextView) eventInfoDialog.findViewById(R.id.event_title_text_view);

        editMembersImageButton = (ImageButton) eventInfoDialog.findViewById(R.id.editMembersImageButton);
        if (!Constants.eventAdmin) {
            editMembersImageButton.setVisibility(View.INVISIBLE);
        }
        eventInfoMembersRecyclerView = (RecyclerView) eventInfoDialog.findViewById(R.id.members_recycler_view);
        eventInfoMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        titleTextView.setText(eventTitle);
        eventIdDialogTextView.setText(Constants.currentEventId);


        destLocationDialogTextView.setText(destLocationTextView);
        eventDescriptionTextView.setText(eventDescription);
        timeStampTextView.setText(timeStamp);

        eventInfoMembersRecyclerView.setHasFixedSize(true);
        EventInfoRecyclerViewAdapter adapter = new EventInfoRecyclerViewAdapter(this, eventInfoDialog, membersList, memberCoordinate, memberProfileImageUrls, memberProfileName, lastSeenInfo);
        eventInfoMembersRecyclerView.setAdapter(adapter);


        editMembersImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //    new EventDialogs().showDialog(MapsActivity.this, Constants.TYPE_DELETE_MEMBERS);
                showMembersDialog();
            }
        });

        Window window = eventInfoDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        eventInfoDialog.setCanceledOnTouchOutside(true);
        eventInfoDialog.show();

    }

    void showMembersDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching event members!");
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
        progressDialog.show();
        eventMemberList = new ArrayList<>();
        Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!Objects.equals(snapshot.getKey(), username)) {
                            eventMemberList.add(snapshot.getKey());
                        }
                    }
                    progressDialog.dismiss();
                    initDeleteEventMemberRecyclerView(eventMemberList);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    void initDeleteEventMemberRecyclerView(List<String> members) {
        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_delete_event_members_layout);
        RecyclerView deleteEventMemberRecyclerView;
        EventMembersRecyclerViewAdapater eventMembersRecyclerViewAdapater;

        deleteEventMemberRecyclerView = (RecyclerView) dialog.findViewById(R.id.delete_event_members_recycler_view);
        deleteEventMemberRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        deleteEventMemberRecyclerView.setLayoutManager(linearLayoutManager);

        eventMembersRecyclerViewAdapater = new EventMembersRecyclerViewAdapater(this, members);
        deleteEventMemberRecyclerView.setAdapter(eventMembersRecyclerViewAdapater);
        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    void showSuggestionDialog() {
        final Dialog suggestionDialog = new Dialog(this, R.style.event_info_dialog_style);
        suggestionDialog.setContentView(R.layout.dialog_suggestion_layout);


        final LinearLayout lovelyLinearLayout, happyLinearLayout, sadLinearLayout, confusedLinearLayout;
        lovelyLinearLayout = (LinearLayout) suggestionDialog.findViewById(R.id.lovely_linear_layout);
        happyLinearLayout = (LinearLayout) suggestionDialog.findViewById(R.id.happy_linear_layout);
        confusedLinearLayout = (LinearLayout) suggestionDialog.findViewById(R.id.confused_linear_layout);
        sadLinearLayout = (LinearLayout) suggestionDialog.findViewById(R.id.sad_linear_layout);


        final EditText suggestionEditText = (EditText) suggestionDialog.findViewById(R.id.suggestion_edit_text);
        final ImageButton lovelyImageIcon, happyImageIcon, confusedImageIcon, sadImageIcon;
        lovelyImageIcon = (ImageButton) suggestionDialog.findViewById(R.id.lovely_emoticon);
        happyImageIcon = (ImageButton) suggestionDialog.findViewById(R.id.happy_emoticon);
        confusedImageIcon = (ImageButton) suggestionDialog.findViewById(R.id.pokerface_emoticon);
        sadImageIcon = (ImageButton) suggestionDialog.findViewById(R.id.sad_emoticon);

        ImageButton sendSuggestionImageButton, cancelSuggestionImageButton;
        sendSuggestionImageButton = (ImageButton) suggestionDialog.findViewById(R.id.send_suggestion_button);

        cancelSuggestionImageButton = (ImageButton) suggestionDialog.findViewById(R.id.cancel_suggestion_image_button);

        cancelSuggestionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                suggestionDialog.dismiss();
            }
        });


        lovelyImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emoticon = 1;
                lovelyLinearLayout.setBackgroundColor(Color.parseColor("#d3d3d3"));
                happyLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                sadLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                confusedLinearLayout.setBackgroundColor(Color.TRANSPARENT);

            }
        });

        happyImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emoticon = 2;
                happyLinearLayout.setBackgroundColor(Color.parseColor("#d3d3d3"));
                lovelyLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                sadLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                confusedLinearLayout.setBackgroundColor(Color.TRANSPARENT);


            }
        });
        confusedImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emoticon = 3;
                confusedLinearLayout.setBackgroundColor(Color.parseColor("#d3d3d3"));
                happyLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                sadLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                lovelyLinearLayout.setBackgroundColor(Color.TRANSPARENT);

            }
        });
        sadImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emoticon = 4;
                sadLinearLayout.setBackgroundColor(Color.parseColor("#d3d3d3"));
                happyLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                lovelyLinearLayout.setBackgroundColor(Color.TRANSPARENT);
                confusedLinearLayout.setBackgroundColor(Color.TRANSPARENT);

            }
        });


        sendSuggestionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emoticon == 0) {
                    Toast.makeText(MapsActivity.this, "Kindly give us your feedback!", Toast.LENGTH_SHORT).show();
                } else {
                    suggestionDialog.dismiss();
                    String suggestion = emoticon + " - " + suggestionEditText.getText().toString().trim();
                    sendSuggestion(suggestion);
                }
            }
        });

        Window window = suggestionDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        suggestionDialog.setCanceledOnTouchOutside(true);
        suggestionDialog.show();

    }

    void sendSuggestion(String suggestion) {
        Firebase suggestionFirebase = new Firebase(FirebaseReferences.FIREBASE_SUGGESTION);
        HashMap<String, Object> suggestionMap = new HashMap<>();
        suggestionMap.put("username", username);
        suggestionMap.put("suggestion", suggestion);
        suggestionFirebase.push().setValue(suggestionMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Toast.makeText(MapsActivity.this, "Thank you!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showChatsDialog() {
        TextView eventIdTextView;
        ImageButton backArrowImageView;
        final Dialog chatsDialog = new Dialog(this, R.style.chat_dialog_style);

        chatsDialog.setContentView(R.layout.recycler_view_chats_layout);

        eventIdTextView = (TextView) chatsDialog.findViewById(R.id.chatsEventIdTextView);

        backArrowImageView = (ImageButton) chatsDialog.findViewById(R.id.back_arrow_image_button_chat_dialog);

        eventChatsRecyclerView = (RecyclerView) chatsDialog.findViewById(R.id.event_chats_recycler_view);
        eventChatsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(chatsDialog.getContext());
        linearLayoutManager.setStackFromEnd(true);
        eventChatsRecyclerView.setLayoutManager(linearLayoutManager);

        chatsRecyclerViewAdapter = new ChatsRecyclerViewAdpater(getApplicationContext(), linearLayoutManager);
        eventChatsRecyclerView.setAdapter(chatsRecyclerViewAdapter);

        final EditText chatsMessageEditText = (EditText) chatsDialog.findViewById(R.id.chatsMessageEditText);
        final ImageButton chatsSendButton = (ImageButton) chatsDialog.findViewById(R.id.chatsSendButton);
        final int color = Color.parseColor("#666666");
        chatsSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chatsSendButton.setColorFilter(color);
                        break;
                    case MotionEvent.ACTION_UP:
                        chatsSendButton.setColorFilter(Color.TRANSPARENT);
                        break;
                }
                return false;
            }
        });

        chatsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageEditText = chatsMessageEditText.getText().toString();
                if (messageEditText.isEmpty()) {
                } else {
                    sendMessage(messageEditText);
                    chatsMessageEditText.setText("");
                }
            }
        });

        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatsDialog.dismiss();
            }
        });
        Window window = chatsDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        chatsDialog.show();
        chatsDialog.setCanceledOnTouchOutside(true);
        Log.d("listenerForChats", "listener removed");
        chatsDialog.show();

    }

    void sendMessage(final String message) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        EventChat chat = new EventChat(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), message);
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), message);
        firebase.push().setValue(chat);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 250, 0, 0);
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
                mMap.setPadding(0, 250, 0, 200);
            }
            if (showGPSOption) {
                showGPSDialog();
                showGPSOption = false;
            } else {
                showSnackBar();
            }
        } else {
            initializeMap();
        }
    }

    void showGPSDialog() {
        final Dialog gpsDialog = new Dialog(this, R.style.event_info_dialog_style);
        gpsDialog.setContentView(R.layout.dialog_turn_on_gps_layout);
        ImageButton turnonGPSImageButton = (ImageButton) gpsDialog.findViewById(R.id.turn_on_gps_image_button);
        Button turnOnGPSButton = (Button) gpsDialog.findViewById(R.id.turn_on_gps_button);
        turnonGPSImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsDialog.dismiss();
                openGPSSettings();
            }
        });
        turnOnGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsDialog.dismiss();
                openGPSSettings();
            }
        });
        Window window = gpsDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        gpsDialog.setCanceledOnTouchOutside(true);
        gpsDialog.show();


    }

    void initializeMap() {
        userlocationAction();
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);


    }
    void initializeMembers(){
        RecyclerView activeEventMemberRecyclerView;
        EventMemberViewRecyclerViewAdapter activeMembersRecyclerViewAdapter;
        activeEventMemberRecyclerView = (RecyclerView) findViewById(R.id.active_event_member_recycler_view_maps);
        activeEventMemberRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        activeEventMemberRecyclerView.setLayoutManager(layoutManager);

        activeMembersRecyclerViewAdapter = new EventMemberViewRecyclerViewAdapter(this, mMap);
        activeEventMemberRecyclerView.setAdapter(activeMembersRecyclerViewAdapter);

    }
    void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Turn on GPS", Snackbar.LENGTH_INDEFINITE)
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
                    mMap.setPadding(0, 250, 0, 0);
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
            mMap.setPadding(0, 250, 0, 0);
        }
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    // updates the location of the user in firebase
    void showPermissionSnackBar() {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "enable location permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mMap != null) {
                            mMap.setPadding(0, 250, 0, 0);
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

    // list of all the users requested to join this event
    void unreadRequestsInit() {
        joinRequests = new ArrayList<>();
        numberOfRequests = 0;
        Firebase requestsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/requested");
        requestsFirebase.keepSynced(true);
        requestsFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("requests", "onChildAddedCalled");
                RequestsDetails requestItem = new RequestsDetails(dataSnapshot.getKey().toString(), dataSnapshot.getValue().toString());
                joinRequests.add(requestItem);
                ++numberOfRequests;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("requests", "onchildremovedcalled");
                    RequestsDetails requestItem = new RequestsDetails(dataSnapshot.getKey().toString(), dataSnapshot.getValue().toString());
                    joinRequests.remove(requestItem);
                    --numberOfRequests;
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /*
        void showRequestNotification(int numberOfRequests, boolean added) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (numberOfRequests <= 0) {
                notificationManager.cancel(Constants.EVENT_REQUEST_NOTIFICATION_ID);
            } else {
                // TODO: start from here
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
                notificationBuilder.setSmallIcon(R.drawable.sample_notification_icon);
                notificationBuilder.setContentTitle("Requests");
                notificationBuilder.setContentText(numberOfRequests + " of them would love to join " + Constants.currentEventId);
                Intent notificationIntent = new Intent(this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent notificationPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                notificationBuilder.setContentIntent(notificationPendingIntent);
                if (added) {
                    Uri defaultNotificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    notificationBuilder.setSound(defaultNotificationSoundUri);
                }
                notificationBuilder.setAutoCancel(true);
                notificationManager.notify(Constants.EVENT_REQUEST_NOTIFICATION_ID , notificationBuilder.build());
            }
        }
    */
    void showEventRequestDialog() {

//        List<RequestsDetails> request = joinRequests;
        // TODO: remove all the requests for that event from the requests database
        requestsDialog = new Dialog(this, R.style.dialog_sent_request_detail);
        requestsDialog.setContentView(R.layout.recycler_view_requests_layout);

        final LinearLayout noRequestsLinearLayout = (LinearLayout) requestsDialog.findViewById(R.id.no_requests_linear_layout);

        final Button closeEventRequestDialogButton = (Button) requestsDialog.findViewById(R.id.close_event_requests_dialog_button);
        eventRequestRecyclerView = (RecyclerView) requestsDialog.findViewById(R.id.event_requests_recycler_view);
        eventRequestRecyclerView.setHasFixedSize(true);

        if (joinRequests.size() == 0) {
            eventRequestRecyclerView.setVisibility(View.GONE);
            noRequestsLinearLayout.setVisibility(View.VISIBLE);

        } else {
            noRequestsLinearLayout.setVisibility(View.INVISIBLE);
            eventRequestRecyclerView.setVisibility(View.VISIBLE);

            requestsRecyclerAdapter = new RequestsRecyclerAdapter(this, joinRequests);
            eventRequestRecyclerView.setAdapter(requestsRecyclerAdapter);
            eventRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

        closeEventRequestDialogButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        closeEventRequestDialogButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        closeEventRequestDialogButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
                        break;
                }
                return false;
            }
        });
        closeEventRequestDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestsDialog.dismiss();
            }
        });

        Window window = requestsDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        requestsDialog.setCanceledOnTouchOutside(true);
        requestsDialog.show();
    }

    void exitEvent() {
        mMap.setOnMyLocationChangeListener(null);
        userExit();
    }

    void userExit() {
        if (Constants.eventAdmin) {
            Firebase eventMembers = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
            eventMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                        Firebase removeMember = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + snapshot.getKey() + "/activeEvent/" + Constants.currentEventId);
                        removeMember.removeValue();
                    }
                    if (dataSnapshot.child("requested").exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.child("requested").getChildren()) {
                            Firebase removeRequest = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + snapshot.getKey() + Constants.currentEventId);
                            removeRequest.removeValue();
                        }
                    }
                    deleteEvent();

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } else {
            Firebase removeMember = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members/" + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
            removeMember.removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    Firebase removeCurrentUser = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "/activeEvent/" + Constants.currentEventId);
                    removeCurrentUser.removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            exitMapEvent();
                        }
                    });
                }
            });

        }

    }

    void deleteEvent() {
        Firebase deleteEvent = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
        deleteEvent.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                exitMapEvent();
            }
        });
    }

    void exitMapEvent() {

        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.leave_event_menu_item) {
            exitEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    void checkGPS() {
        if (mMap != null) {
            showAllInOneDialog();
        }


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
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    // Version 2: STARTS HERE
    void userlocationAction() {

        // enable user location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setPadding(0, 250, 0, 200);
            }
            showPermissionSnackBar();
        } else {
            mMap.setMyLocationEnabled(true);
            // listener for change in location of the user
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {

                    checkNearCheckPoint(location);
                    updateUserCurrentLocation(location);
                }
            });
            changeInLocation();
        }


    }

    void checkNearCheckPoint(Location location) {
        if (checkPointCoordinateMap == null || checkPointCoordinateMap.isEmpty()) {
        } else {
            for (Map.Entry<String, Note> checkpoint : checkPointCoordinateMap.entrySet()) {
                Note note = checkpoint.getValue();
                LatLng latLng = note.getLatlng();
                float[] distance = new float[2];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), latLng.latitude, latLng.longitude, distance);
                if (distance[0] <= 20) {


                    if (!checkPointsReached.contains(checkpoint.getKey())) {
                        checkPointsReached.add(checkpoint.getKey());
                        vibrateDevice();
                    }

                }
            }
        }

    }

    void vibrateDevice() {
        Toast.makeText(MapsActivity.this, "checkpoint reached" + checkPointsReached, Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    void showIneternetConnectionSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("SETTINGS", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInternetSettings();
            }
        });
        snackbar.setActionTextColor(Color.parseColor("#009688"));
        snackbar.show();
    }

    void openInternetSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
        startActivity(intent);
    }

    void updateUserCurrentLocation(final Location location) {
        final Firebase updateUserCurrentLocationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
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

    void changeInLocation() {
        Firebase changeInLocationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        changeInLocationFirebase.keepSynced(true);
        changeInLocationFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                eventMembersUpdate();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                updateMemberLocation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                eventMembersUpdate();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    void eventMembersUpdate() {
        Firebase membersUpdateFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        membersUpdateFirebase.keepSynced(true);
        membersUpdateFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members = new HashMap<>();
                memberProfileImageUrls = new ArrayList<String>();
                lastSeenInfo = new ArrayList<String>();
                long numberOfMembers = 0;
                if (dataSnapshot.exists()) {
                    numberOfMembers = dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        members.put(snapshot.getKey(), snapshot.getValue().toString());
                    }
                    zoomFit = true;
                    if (members.size() == numberOfMembers) {
                        fetchUserNames();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void fetchUserNames() {
        namesList = new ArrayList<>();
        for (Map.Entry<String, Object> member : members.entrySet()) {

            Firebase fetchUserNames = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + member.getKey() + "/name");
            fetchUserNames.keepSynced(true);
            fetchUserNames.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    namesList.add(dataSnapshot.getValue().toString());
                    if (namesList.size() == members.size()) {
                        updateMapMembers();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }


    }

//    void updateUserProfileImage(String username) {
//        Firebase imageURLFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/userPhotoUri");
//        imageURLFirebase.keepSynced(true);
//        imageURLFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                memberProfileImageUrls.add(dataSnapshot.getValue().toString());
//                if (members.size() == memberProfileImageUrls.size()) {
//
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//    }

    void zoomFitMembers() {
        if (mMap != null) {
            zoomFit = true;
            updateMapMembers();
        }
    }

    void updateMapMembers() {

        Marker destinationMarker = null;
        mMap.clear();
        List<Marker> zoomFitCheckPointCoordinates = new ArrayList<>();
        if (!checkPointCoordinateMap.isEmpty()) {

            for (Map.Entry<String, Note> checkpointMapEntry : checkPointCoordinateMap.entrySet()) {

                Note note = checkpointMapEntry.getValue();
                Marker checkPointMarker = mMap.addMarker(new MarkerOptions().position(note.getLatlng()).title(note.getTitle()).snippet(note.getDesc()).icon(BitmapDescriptorFactory.fromBitmap(InitIcon.getCustomBitmapFromVectorDrawable(this, R.drawable.check_points_icon, 250, 250))));
                checkPointMarker.setTag(note.getKey());
                zoomFitCheckPointCoordinates.add(checkPointMarker);
            }
        }

        if (destinationIconBitmap != null) {
            String[] destCoordinates = eventInfo.getdLocation().split(",");


            destinationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(destCoordinates[0]), Double.parseDouble(destCoordinates[1]))).title("Destination Location").icon(BitmapDescriptorFactory.fromBitmap(destinationIconBitmap)).snippet(eventInfo.getdLocationDesc()));

            destinationMarker.setTag(Constants.DESTINATION_LOCATION_TAG);
        }
        memberLocationMarkers = new HashMap<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int i = 1;
        int memberPositionTracker = -1;
        if (!(members == null && members.isEmpty())) {
            for (Map.Entry<String, Object> member : members.entrySet()) {
                ++memberPositionTracker;
                Bitmap markerBubbleBitmap = null;
                String[] coordinates = member.getValue().toString().split(",");

                Marker marker = null;
                IconGenerator iconGenerator = new IconGenerator(this);

                switch (i) {
                    case 1: {
                        iconGenerator.setStyle(IconGenerator.STYLE_RED);
                        break;
                    }
                    case 2: {
                        iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
                        break;
                    }
                    case 3: {
                        iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
                        break;
                    }
                    case 4: {
                        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
                        break;
                    }
                    case 5: {
                        iconGenerator.setStyle(IconGenerator.STYLE_PURPLE);
                        break;
                    }
                }

                markerBubbleBitmap = iconGenerator.makeIcon(namesList.get(memberPositionTracker));
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))).title(member.getKey()).icon(BitmapDescriptorFactory.fromBitmap(markerBubbleBitmap)));
                ++i;
                builder.include(marker.getPosition());
                memberLocationMarkers.put(member.getKey(), marker);
                if (i > 5) {
                    i = 1;
                }
            }
        }
        if (zoomFit) {
            if (destinationIconBitmap != null) {

                builder.include(destinationMarker.getPosition());
            }
            if (!zoomFitCheckPointCoordinates.isEmpty()) {
                for (Marker checkPointMarkers : zoomFitCheckPointCoordinates) {
                    builder.include(checkPointMarkers.getPosition());
                }
            }
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cameraUpdate);
            zoomFit = false;
        }
    }

    Bitmap generateIconfromProfileImage(int position) {
        Bitmap bitmap = null;
        IconGenerator iconGenerator = new IconGenerator(this);
        CircleImageView circleImageView = new CircleImageView(this);
        circleImageView.setLayoutParams(new ViewGroup.LayoutParams(170, 170));
        Picasso.with(this).load(Uri.parse(memberProfileImageUrls.get(position))).error(R.drawable.default_profile_image_icon).into(circleImageView);
        iconGenerator.setContentView(circleImageView);
        iconGenerator.setBackground(null);
        bitmap = iconGenerator.makeIcon();
        return bitmap;
    }

    void updateMemberLocation(DataSnapshot dataSnapshot) {
        Marker marker = (Marker) memberLocationMarkers.get(dataSnapshot.getKey().toString());
        String[] coordinates = dataSnapshot.getValue().toString().split(",");
        LatLng latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        if (marker != null) {
            marker.setPosition(latLng);
        }
    }


    void deleteCheckPoint(String key, String mapKey) {
        Firebase deleteCheckPointFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints/" + key);
        deleteCheckPointFirebase.removeValue();
        checkPointCoordinateMap.remove(mapKey);
        zoomFitMembers();
    }

    void showCheckPointAddOptionDialog(final LatLng latLng, String title) {
        final Dialog addCheckPointDialog = new Dialog(this, R.style.event_info_dialog_style);
        addCheckPointDialog.setContentView(R.layout.dialog_add_check_point);
        final EditText checkpointTitle = (EditText) addCheckPointDialog.findViewById(R.id.checkpoint_title_edit_text);
        ImageButton checkPointEditLocation = (ImageButton) addCheckPointDialog.findViewById(R.id.checkpoint_edit_location_image_button);
        ImageButton saveCheckPointButton = (ImageButton) addCheckPointDialog.findViewById(R.id.save_checkpoint_button);
        ImageButton cancelCheckPoint = (ImageButton) addCheckPointDialog.findViewById(R.id.cancel_add_checkpoint_image_button);
        final EditText checkPointDescription = (EditText) addCheckPointDialog.findViewById(R.id.checkpoint_description_edit_text);


        checkpointTitle.setText(title);
        cancelCheckPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCheckPointDialog.dismiss();
            }
        });
        saveCheckPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String desc = checkPointDescription.getText().toString();
                String title = checkpointTitle.getText().toString();

                if (!desc.isEmpty() || title.isEmpty() || latLng.toString().isEmpty() || latLng == null) {
                    saveCheckPoint(title, desc, latLng);
                    addCheckPointDialog.dismiss();
                }
            }
        });
        checkPointEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placePickerDialog();
            }
        });
        Window window = addCheckPointDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        addCheckPointDialog.setCanceledOnTouchOutside(true);
        addCheckPointDialog.show();
    }

    void saveCheckPoint(String title, String desc, LatLng latlng) {
        Note note = new Note();
        note.setTitle(title);
        note.setDesc(desc);
        note.setLatlng(latlng);
        Firebase saveCheckPoint = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints");
        Firebase newCheckPoint = saveCheckPoint.push();
        note.setKey(newCheckPoint.getKey());

        newCheckPoint.setValue(note);

    }

    void showCheckPointDialog(final String markerTag) {
        final Dialog showCheckPointDialog = new Dialog(this, R.style.event_info_dialog_style);
        showCheckPointDialog.setContentView(R.layout.dialog_show_checkpoint_layout);

        TextView titleEditText, descriptionEditText;
        ImageButton deleteCheckPointButton;
        ImageButton cancelCheckPointImageButton;
        cancelCheckPointImageButton = (ImageButton) showCheckPointDialog.findViewById(R.id.cancel_show_checkpoint_image_button);
        titleEditText = (TextView) showCheckPointDialog.findViewById(R.id.show_checkpoint_title_edit_text);
        descriptionEditText = (TextView) showCheckPointDialog.findViewById(R.id.show_checkpoint_description_edit_text);

        final Note note = checkPointCoordinateMap.get(markerTag);
        titleEditText.setText(note.getTitle());
        descriptionEditText.setText(note.getDesc());

        deleteCheckPointButton = (ImageButton) showCheckPointDialog.findViewById(R.id.delete_checkpoint_button);

        cancelCheckPointImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckPointDialog.dismiss();
            }
        });
        deleteCheckPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCheckPoint(note.getKey(), markerTag);
                showCheckPointDialog.dismiss();
            }
        });


        Window window = showCheckPointDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        showCheckPointDialog.setCanceledOnTouchOutside(true);
        showCheckPointDialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String markerTag = (String) marker.getTag();
        if (markerTag != null) {
            if (markerTag.equals(Constants.START_LOCATION_TAG) || markerTag.equals(Constants.DESTINATION_LOCATION_TAG)) {
                showStreetViewSnackBar(marker);
            } else {
                showCheckPointDialog(markerTag);
            }
        }
        return false;
    }

    void showStreetView(final Double latitude, final Double longitude) {
        Intent intent = new Intent(MapsActivity.this, StreetViewActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
//                } else {
//                    // TODO: show dialog to say streetview unavailable and finish() on OK
//                    showStreetViewNotAvailableSnackBar();
//                }
    }


    void showStreetViewNotAvailableSnackBar() {
        if (mMap != null) {
            mMap.setPadding(0, 250, 0, 200);
        }
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Oops! Street view isn't available here...", Snackbar.LENGTH_SHORT);
        snackbar.setCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (mMap != null) {
                    mMap.setPadding(0, 250, 0, 0);
                }
            }

            @Override
            public void onShown(Snackbar snackbar) {


            }
        });
        snackbar.show();
    }

    void showStreetViewSnackBar(final Marker marker) {
        if (mMap != null) {
            mMap.setPadding(0, 250, 0, 200);
        }
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "See how it looks?", Snackbar.LENGTH_LONG)
                .setAction("SEE IT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showStreetView(marker.getPosition().latitude, marker.getPosition().longitude);


                    }
                });
        snackbar.setCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (mMap != null) {
                    mMap.setPadding(0, 250, 0, 0);
                }
            }

            @Override
            public void onShown(Snackbar snackbar) {


            }
        });
        snackbar.setActionTextColor(Color.parseColor("#009688"));
        snackbar.show();

    }

    // version 2: ENDS HERE

}