package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
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
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.projects.shubhamkhandelwal.tisy.Classes.ChatsRecyclerViewAdpater;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventChat;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfoRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationLog;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // PlacePicker variables
    public static final int REQUEST_PERMISSION_SETTINGS = 1; // used for the permission setting intent
    public static final int PLACE_PICKER_REQUEST = 1; // used for the place picker intent
    // chat related variables
    public static Dialog chatsDialog; // chats dialog object reference
    public static int numberOfRequests = 0; // number of join event request received
    public static int numberOfUnreadChats = 0; // count of number of unread chat messages
    public static int numberOfReadChats = 0; // count of number of chat messages read
    public static boolean zoomFit; // if true: fits the specified LatLng into the view
    RecyclerView eventChatsRecyclerView; // chats view recyclerview
    ChatsRecyclerViewAdpater chatsRecyclerViewAdapter; // chats view recyclerview adapter
    Firebase unreadChatsFirebase; // Firebase reference to the event chat
    int chatNotificationCount; // number of unread messages in the event chat
    // map event variables
    Map<String, Object> members; // members (usernames) in the event
    List<String> namesList; // names of members in the event
    Map<String, Object> memberLocationMarkers; // contains map of (username, marker object) to reference marker positions (LatLng) for every member
    Bitmap destinationIconBitmap; // holds the destination icon
    // checkpoint variables
    Map<String, Object> checkPointCoordinateMap; // contains all the checkpoints in the map; id and it's position (LatLng)
    boolean isCheckPointEdit; // if true, editing the checkpoint
    int checkPointMakrerEditPosition; // the unique id for the checkpoint
    List<String> checkPointsReached; // checkpoints crossed (reached) by the user
    // received request variables
    Dialog requestsDialog; //  received requests dialog object
    List<RequestsDetails> joinRequests; // received requests; username list
    RecyclerView eventRequestRecyclerView; // received requests recyclerview
    RequestsRecyclerAdapter requestsRecyclerAdapter; // received requests recyclerview adapter
    EventInfo eventInfo; // EventInfo class Object; All the basic event information
    GoogleMap mMap; // GoogleMap Object
    Firebase firebase; // Firebase Object
    // View Objects
    ImageButton eventInfoImageButton; // Event information button
    ImageButton allIconsInOneImageButton; // other map related option button
    CoordinatorLayout coordinatorLayout;
    // event infomation variables
    List<String> membersList; // members (username) in the event
    List<String> memberCoordinate; // coordinates (LatLng) of the members in the event
    List<String> memberProfileImageUrls; // profile Image URL of every member in the event
    List<String> memberProfileName; // member name for users in the event
    String timeStamp; // date and time of when the event was created
    String eventTitle; // title of the event
    String startLocationTextView; // start location description of the event
    String destLocationTextView; // destination location description of the event
    String eventDescription; // event description
    int memberUriCount; // number of URL's fetched of the members
    // search variables
    String nameSearch; // name/username/eventID searched for by the user
    RecyclerView searchOptionChoiceRecyclerView; // search option recyclerview
    SearchResultsRecyclerViewAdapter searchResultsRecyclerViewAdapter; // search option recyclerview adapter
    PlacePicker.IntentBuilder builder; // PlacePicker Intent builder

    String username; // to access the username variable throughout the activity

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
        eventInfoImageButton = (ImageButton) findViewById(R.id.eventInfoImageButton);
        allIconsInOneImageButton = (ImageButton) findViewById(R.id.allInOneIcon);

        // initializing variable
        //initializing String variables
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        nameSearch = new String();

        // initializing integer variables
        checkPointMakrerEditPosition = 0;
        chatNotificationCount = 0;
        isCheckPointEdit = false;
        zoomFit = false;

        // initializing Class Objects
        eventInfo = new EventInfo();

        // initializing Collection Objects
        joinRequests = new ArrayList<>();
        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
        memberProfileImageUrls = new ArrayList<>();
        memberProfileName = new ArrayList<>();
        checkPointsReached = new ArrayList<>();
        memberLocationMarkers = new HashMap<>();

        // TODO: change the service functions later; and optimize their usage
        // start background service
        startService(new Intent(getBaseContext(), InternetConnectionService.class));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        // callback is triggered when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // requests received notification
        unreadRequestsInit();

        // TODO: remove it
        unreadChats();

        // initialize the desitination icon
        destinationIconInit();

        // initialzie the checkpoints in the map
        checkPointsInit();

        // set view object color
        eventInfoImageButton.setColorFilter(Color.parseColor("#0C70A5"));
        allIconsInOneImageButton.setColorFilter(Color.parseColor("#666666"));

        eventInfoImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        eventInfoImageButton.setColorFilter(Color.parseColor("#084D73"));
                        break;
                    case MotionEvent.ACTION_UP:
                        eventInfoImageButton.setColorFilter(Color.parseColor("#0C70A5"));
                        break;
                }
                return false;
            }
        });

        allIconsInOneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // show other options dialog
                showAllInOneDialog();
            }
        });

        eventInfoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show event information dialog
                eventInfoDialog();
            }
        });
    }

    // initialize all the checkpoints for the event
    void checkPointsInit() {
        checkPointCoordinateMap = new HashMap<>();
        Firebase loadCheckPointsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints");
        loadCheckPointsFirebase.keepSynced(true);
        loadCheckPointsFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                checkPointCoordinateMap.put(dataSnapshot.getKey(), dataSnapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(MapsActivity.this, "marker edited" + dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                checkPointCoordinateMap.put(dataSnapshot.getKey(), dataSnapshot.getValue());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                checkPointCoordinateMap.remove(dataSnapshot.getKey());
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
        switch (Constants.dIconResourceId) {
            case 1: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_walking);

                break;
            }
            case 2: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_swimming);


                break;
            }
            case 3: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_spa);


                break;
            }
            case 4: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_gym);


                break;
            }
            case 5: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_drinks);


                break;
            }
            case 6: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_casino);


                break;
            }
            case 7: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination__zoo);


                break;
            }
            case 8: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_amusement_park);


                break;
            }
            case 9: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bowling_alley);


                break;
            }
            case 10: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_aquarium);


                break;
            }
            case 11: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_night_club);


                break;
            }
            case 12: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_running);


                break;

            }
            case 13: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_football);


                break;
            }
            case 14: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_gaming);


                break;
            }
            case 15: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_bicycle);


                break;
            }
            case 16: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_cafe);


                break;
            }
            case 17: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_restaurant);


                break;
            }
            case 18: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_dinning);


                break;
            }
            case 19: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pizza);


                break;
            }

            case 20: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hotel);


                break;
            }
            case 21: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_university);


                break;
            }
            case 23: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_library);


                break;
            }
            case 24: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_museum);


                break;
            }
            case 25: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_beauty_salon);


                break;
            }
            case 26: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_school);


                break;
            }
            case 27: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_home);


                break;
            }
            case 28: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_stadium);


                break;
            }
            case 29: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_park);


                break;
            }
            case 30: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pharmacy);


                break;

            }

            case 31: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hospital);


                break;

            }
            case 32: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_worship);


                break;
            }
            case 33: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_mall);


                break;
            }

            case 34: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_book_store);


                break;
            }
            case 35: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_convenience_store);


                break;
            }
            case 36: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_liquor_store);


                break;
            }
            case 37: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_laundry);


                break;
            }
            case 38: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_print_shop);


                break;
            }
            case 39: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_grocery_store);


                break;
            }

            case 40: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_parking);


                break;
            }

            case 41: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_airport);


                break;
            }

            case 42: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_train_station);


                break;
            }

            case 43: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bus_station);


                break;
            }

            case 44: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_subway_station);


                break;
            }

            case 45: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_tram);


                break;
            }


        }

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
                eventInfo.setsLocation(dataSnapshot.child("sLocation").getValue().toString());
                eventInfo.setsLocationDesc(dataSnapshot.child("sLocationDesc").getValue().toString());
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

        final Dialog allInOneDialog = new Dialog(this, R.style.event_info_dialog_style);
        allInOneDialog.setContentView(R.layout.dialog_all_in_one_layout);

        ImageButton requestIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_request_icon);
        ImageButton chatIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_chat_icon);
        ImageButton zoomFitImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_zoom_fit_icon);
        ImageButton addNewMemberImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_add_new_member);
        ImageButton addNewCheckPointImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_add_new_checkpoint);
        ImageButton changeMapStyleImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_change_mode_icon);
        TextView chatUnreadMessageCountTextView = (TextView) allInOneDialog.findViewById(R.id.chat_unread_message_count);

        int count = getUreadChatCount();
        if (count == 0) {
            chatUnreadMessageCountTextView.setVisibility(View.INVISIBLE);
        } else {
            chatUnreadMessageCountTextView.setVisibility(View.VISIBLE);
            chatUnreadMessageCountTextView.setText(String.valueOf(count));
        }
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


    void showMapStyleOptionsDialog() {

        final Dialog mapStyleDialog = new Dialog(this, R.style.event_info_dialog_style);
        mapStyleDialog.setContentView(R.layout.dialog_map_style_option_layout);
        Button defaultModeButton, aubergineModeButton, retroModeButton, darkModeButton, nightModeButton, silverModeButton;

        defaultModeButton = (Button) mapStyleDialog.findViewById(R.id.default_mode);
        aubergineModeButton = (Button) mapStyleDialog.findViewById(R.id.aubergine_mode);
        nightModeButton = (Button) mapStyleDialog.findViewById(R.id.night_mode);
        retroModeButton = (Button) mapStyleDialog.findViewById(R.id.retro_mode);
        darkModeButton = (Button) mapStyleDialog.findViewById(R.id.dark_mode);
        silverModeButton = (Button) mapStyleDialog.findViewById(R.id.silver_mode);

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
        isCheckPointEdit = false;
        placePickerDialog();
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
                if (isCheckPointEdit) {
                    if (checkPointMakrerEditPosition != 0) {
                        editCheckPointPosition(checkPointMakrerEditPosition, place.getLatLng().latitude, place.getLatLng().longitude);
                        isCheckPointEdit = false;
                        checkPointMakrerEditPosition = 0;
                    }
                } else {
                    saveCheckPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                }
            }
        }
    }

    void editCheckPointPosition(int position, Double latitude, Double longitude) {
        Firebase saveCheckPoint = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints");
        HashMap<String, Object> checkPointFirebaseMap = new HashMap<>();
        checkPointFirebaseMap.put(String.valueOf(position), latitude + "," + longitude);
        saveCheckPoint.updateChildren(checkPointFirebaseMap);
    }

    void saveCheckPoint(Double latitude, Double longitude) {
        Firebase saveCheckPoint = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/checkPoints");
        HashMap<String, Object> checkPointFirebaseMap = new HashMap<>();
        checkPointFirebaseMap.put(String.valueOf(checkPointCoordinateMap.size() + 1), latitude + "," + longitude);
        saveCheckPoint.updateChildren(checkPointFirebaseMap);
    }

    void sendMemberRequest() {

        final Dialog sendMemberRequestDialog = new Dialog(this, R.style.event_info_dialog_style);
        sendMemberRequestDialog.setContentView(R.layout.dialog_send_request_from_event_layout);

        final EditText sendJoinRequestEventIdEditText;
        final Button sendJoinRequestButton;
        final Button searchOptionChoiceButton;
        RecyclerView eventJoinRequestSendRecyclerView;
        sendJoinRequestEventIdEditText = (EditText) sendMemberRequestDialog.findViewById(R.id.dialog_send_join_request_edit_text);
        sendJoinRequestButton = (Button) sendMemberRequestDialog.findViewById(R.id.dialog_send_join_request_button);
        searchOptionChoiceButton = (Button) sendMemberRequestDialog.findViewById(R.id.search_option_choice_button);
        searchOptionChoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMemberRequestDialog.dismiss();
                showSearchOptionDialog();
            }
        });
        sendJoinRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userId = sendJoinRequestEventIdEditText.getText().toString();
                if (userId == null || userId.isEmpty()) {
                    //TODO: show snackbar here
                } else {
                    if (userId.equals(username)) {
                        //TODO: show snackbar here
                        sendJoinRequestEventIdEditText.setText("");
                    } else {
                        Firebase userIdCheckFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + userId);
                        userIdCheckFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Firebase sendRequestFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId);
                                    HashMap<String, Object> sendRequestUsername = new HashMap<String, Object>();
                                    sendRequestUsername.put(userId, username);
                                    sendRequestFirebase.updateChildren(sendRequestUsername, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            Firebase userSentRequestUpdateFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + userId);
                                            HashMap<String, Object> userSentRequestUpdate = new HashMap<String, Object>();
                                            userSentRequestUpdate.put(Constants.currentEventId, username);
                                            userSentRequestUpdateFirebase.updateChildren(userSentRequestUpdate, new Firebase.CompletionListener() {
                                                @Override
                                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                    sendJoinRequestEventIdEditText.setText("");
                                                    //TODO: showsnackbar here
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }
                }
            }
        });


        eventJoinRequestSendRecyclerView = (RecyclerView) sendMemberRequestDialog.findViewById(R.id.dialog_event_join_request_sent_recycler_view);
        eventJoinRequestSendRecyclerView.setLayoutManager(new LinearLayoutManager(sendMemberRequestDialog.getContext()));
        eventJoinRequestSendRecyclerView.setHasFixedSize(true);
        SentEventJoinRequestRecyclerViewAdapter adapter = new SentEventJoinRequestRecyclerViewAdapter(this);
        eventJoinRequestSendRecyclerView.setAdapter(adapter);

        Window window = sendMemberRequestDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        sendMemberRequestDialog.setCanceledOnTouchOutside(true);
        sendMemberRequestDialog.show();


    }

    void showSearchOptionDialog() {

        final EditText searchEditText;
        ImageButton searchButton;
        final Dialog searchOptionDialog = new Dialog(this, R.style.event_info_dialog_style);
        searchOptionDialog.setContentView(R.layout.dialog_search_option_layout);

        searchEditText = (EditText) searchOptionDialog.findViewById(R.id.search_option_choice_dialog_edit_text);
        searchButton = (ImageButton) searchOptionDialog.findViewById(R.id.search_option_choice_dialog_button);
        searchOptionChoiceRecyclerView = (RecyclerView) searchOptionDialog.findViewById(R.id.dialog_search_results_recycler_view);
        searchOptionChoiceRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(searchOptionDialog.getContext());
        searchOptionChoiceRecyclerView.setLayoutManager(linearLayoutManager);
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

    void backToMain() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
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
                    } else if (snapshot.getKey().equals("sLocationDesc")) {
                        startLocationTextView = snapshot.getValue().toString();
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
        memberProfileImageUrls = new ArrayList<>();
        memberProfileName = new ArrayList<>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching event details for you!");
        progressDialog.setCancelable(false);
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

                    if (membersList.size() == memberUriCount) {
                        progressDialog.dismiss();
                        showEventInfoDialog();
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
        TextView startLocationDialogTextView;
        TextView destLocationDialogTextView;
        TextView eventDescriptionTextView;
        TextView timeStampTextView;
        TextView titleTextView;
        RecyclerView eventInfoMembersRecyclerView;


        Dialog eventInfoDialog = new Dialog(this, R.style.event_info_dialog_style);
        eventInfoDialog.setContentView(R.layout.dialog_event_info_layout);

        eventIdDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.event_id_info_text_view);
        startLocationDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.start_location_desc_text_view);
        destLocationDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.dest_location_desc_text_view);
        eventDescriptionTextView = (TextView) eventInfoDialog.findViewById(R.id.event_desc_text_view);
        timeStampTextView = (TextView) eventInfoDialog.findViewById(R.id.time_stamp_text_view);
        titleTextView = (TextView) eventInfoDialog.findViewById(R.id.event_title_text_view);

        eventInfoMembersRecyclerView = (RecyclerView) eventInfoDialog.findViewById(R.id.members_recycler_view);
        eventInfoMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        titleTextView.setText(eventTitle);
        eventIdDialogTextView.setText(Constants.currentEventId);
        startLocationDialogTextView.setText(startLocationTextView);
        destLocationDialogTextView.setText(destLocationTextView);
        eventDescriptionTextView.setText(eventDescription);
        timeStampTextView.setText(timeStamp);

        eventInfoMembersRecyclerView.setHasFixedSize(true);
        EventInfoRecyclerViewAdapter adapter = new EventInfoRecyclerViewAdapter(this, eventInfoDialog, membersList, memberCoordinate, memberProfileImageUrls, memberProfileName);
        eventInfoMembersRecyclerView.setAdapter(adapter);

        Window window = eventInfoDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        eventInfoDialog.setCanceledOnTouchOutside(true);
        eventInfoDialog.show();

    }

    void unreadChats() {
        numberOfReadChats = 0;
        numberOfUnreadChats = 0;
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.currentEventId + SharedPreferencesName.CHATS_READ_COUNT, MODE_PRIVATE);
        if (sharedPreferences.contains("chats_read")) {
            numberOfReadChats = sharedPreferences.getInt("chats_read", 0);
        }
        unreadChatsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        unreadChatsFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("listenerForChats", "onChildAdded for unreadChars()");
                ++numberOfUnreadChats;
                if (numberOfUnreadChats > numberOfReadChats) {
                    if (chatsDialog == null) {
                        showUnreadChatsNotification(true);
                    }
                }
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

    int getUreadChatCount() {
        int noOfUnreadChats = 0;
        if (numberOfUnreadChats > numberOfReadChats) {
            noOfUnreadChats = numberOfUnreadChats - numberOfReadChats;
        }
        return noOfUnreadChats;
    }

    void showUnreadChatsNotification(boolean unread) {
        int noOfUnreadChats = numberOfUnreadChats - numberOfReadChats;
        ++chatNotificationCount;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (unread) {
            // TODO: start from here
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setSmallIcon(R.drawable.sample_notification_icon);
            notificationBuilder.setContentTitle("New message");
            notificationBuilder.setContentText("You have " + noOfUnreadChats + " unread messages from " + Constants.currentEventId);

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

            if (chatNotificationCount == 1) {
                Uri defaultNotificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(defaultNotificationSoundUri);
            }
            notificationBuilder.setAutoCancel(true);

            notificationManager.notify(Constants.CHATS_NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
        } else {
            chatNotificationCount = 0;
            notificationManager.cancel(Constants.CHATS_NOTIFICATION_ID);
        }

    }

    void showChatsDialog() {
        TextView eventIdTextView;
        ImageButton backArrowImageView;
        chatsDialog = new Dialog(this, R.style.chat_dialog_style);

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
        showUnreadChatsNotification(false);
        Log.d("listenerForChats", "listener removed");
        chatsDialog.show();
        chatsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                chatsDialog = null;
                unreadChats();
            }
        });
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
        initializeMapStyle();
        GPSEnabledCheck();

    }

    void initializeMapStyle() {
        SharedPreferences mapSharedPreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        int styleType = mapSharedPreference.getInt("style", 0);
        if (!(styleType == 0)) {
            setMapStyle(styleType);
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
        userlocationAction();
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

    }

    void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "enable location setting", Snackbar.LENGTH_INDEFINITE)
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

    // list of all the users requested to join this event
    void unreadRequestsInit() {
        joinRequests = new ArrayList<>();
        numberOfRequests = 0;
        Firebase requestsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/requested");
        requestsFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("requests", "onChildAddedCalled");
                RequestsDetails requestItem = new RequestsDetails(dataSnapshot.getKey().toString(), dataSnapshot.getValue().toString());
                joinRequests.add(requestItem);
                ++numberOfRequests;
                showRequestNotification(numberOfRequests, true);

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
                    showRequestNotification(numberOfRequests, false);
                    Log.d("requests", joinRequests.toString());
                    Log.d("requests", "" + numberOfRequests);
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

            notificationManager.notify(Constants.EVENT_REQUEST_NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());

        }
    }

    void showEventRequestDialog() {

//        List<RequestsDetails> request = joinRequests;
        // TODO: remove all the requests for that event from the requests database
        requestsDialog = new Dialog(this, R.style.dialog_sent_request_detail);
        requestsDialog.setContentView(R.layout.recycler_view_requests_layout);

        final Button closeEventRequestDialogButton = (Button) requestsDialog.findViewById(R.id.close_event_requests_dialog_button);
        eventRequestRecyclerView = (RecyclerView) requestsDialog.findViewById(R.id.event_requests_recycler_view);
        eventRequestRecyclerView.setHasFixedSize(true);

        requestsRecyclerAdapter = new RequestsRecyclerAdapter(getApplicationContext(), joinRequests);
        eventRequestRecyclerView.setAdapter(requestsRecyclerAdapter);
        eventRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
        showRequestNotification(0, false);
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
                        Firebase removeMember = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + snapshot.getKey().toString() + "/activeEvent/" + Constants.currentEventId);
                        removeMember.removeValue();
                    }
                    if (dataSnapshot.child("requested").exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.child("requested").getChildren()) {
                            Firebase removeRequest = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + snapshot.getKey().toString() + Constants.currentEventId);
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
            Firebase removeCurrentUser = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "/activeEvent/" + Constants.currentEventId);
            removeCurrentUser.removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    exitMapEvent();
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
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
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
        if (id == R.id.chat_event_icon) {
            showChatsDialog();
        }
        if (id == R.id.join_request_menu_item) {
            showEventRequestDialog();
        }

        return super.onOptionsItemSelected(item);
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
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
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
                mMap.setPadding(0, 0, 0, 200);
            }
            showPermissionSnackBar();
        } else {
            mMap.setMyLocationEnabled(true);
            // listener for change in location of the user
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    Firebase userLocationLogFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/locationLog");
                    userLocationLogFirebase.keepSynced(true);
                    LocationLog locationLog = new LocationLog();
                    locationLog.setLatitude(String.valueOf(location.getLatitude()));
                    locationLog.setLongitude(String.valueOf(location.getLongitude()));
                    userLocationLogFirebase.push().setValue(locationLog);

                    if (!checkInternetConnection()) {
                        String message = "No internet connection";
                        showIneternetConnectionSnackBar(message);
                    } else {
                        checkNearCheckPoint(location);
                        updateUserCurrentLocation(location);
                    }
                }
            });
            changeInLocation();
        }


    }

    void checkNearCheckPoint(Location location) {
        if (checkPointCoordinateMap == null || checkPointCoordinateMap.isEmpty()) {
        } else {
            for (Map.Entry<String, Object> checkpoint : checkPointCoordinateMap.entrySet()) {
                float[] distance = new float[2];
                String[] coordinate = checkpoint.getValue().toString().split(",");
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1]), distance);
                if (distance[0] <= 20) {
                    Toast.makeText(MapsActivity.this, " distance0:" + distance[0] + " distance1:" + distance[1], Toast.LENGTH_SHORT).show();

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

    void updateUserCurrentLocation(Location location) {
        Firebase updateUserCurrentLocationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        Map<String, Object> currentLocation = new HashMap<>();
        currentLocation.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), location.getLatitude() + "," + location.getLongitude());
        updateUserCurrentLocationFirebase.updateChildren(currentLocation);
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
        membersUpdateFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members = new HashMap<>();
                memberProfileImageUrls = new ArrayList<String>();
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
            eventMembersUpdate();
            updateMapMembers();
        }
    }

    void updateMapMembers() {
        Marker startMarker = null;
        Marker destinationMarker = null;
        mMap.clear();
        List<Marker> zoomFitCheckPointCoordinates = new ArrayList<>();
        if (!checkPointCoordinateMap.isEmpty()) {
            int checkPointCounter = 0;
            for (Map.Entry<String, Object> point : checkPointCoordinateMap.entrySet()) {
                ++checkPointCounter;
                String[] coordinate = point.getValue().toString().split(",");


                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.checkpoint_marker);
                Bitmap tempBitmap = bitmapDrawable.getBitmap();
                Bitmap checkPointBitmap = Bitmap.createScaledBitmap(tempBitmap, 120, 120, false);

                Marker checkPointMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1]))).title("checkpoint-" + checkPointCounter).icon(BitmapDescriptorFactory.fromBitmap(checkPointBitmap)));
                checkPointMarker.setTag(checkPointCounter);
                zoomFitCheckPointCoordinates.add(checkPointMarker);


            }
        }

        if (destinationIconBitmap != null) {
            String[] destCoordinates = eventInfo.getdLocation().split(",");
            String[] startCoordinates = eventInfo.getsLocation().split(",");
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.start_location_icon);
            Bitmap tempBitmap = bitmapDrawable.getBitmap();
            Bitmap startLocationBitmap = applyCustomBitmapColor(Bitmap.createScaledBitmap(tempBitmap, 120, 120, false), "#5d8aa8");
            destinationIconBitmap = applyCustomBitmapColor(destinationIconBitmap, "#5d8aa8");
            startMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(startCoordinates[0]), Double.parseDouble(startCoordinates[1]))).title("Start Location").icon(BitmapDescriptorFactory.fromBitmap(startLocationBitmap)).snippet(eventInfo.getsLocationDesc()));
            destinationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(destCoordinates[0]), Double.parseDouble(destCoordinates[1]))).title("Destination Location").icon(BitmapDescriptorFactory.fromBitmap(destinationIconBitmap)).snippet(eventInfo.getdLocationDesc()));
            startMarker.setTag(Constants.START_LOCATION_TAG);
            destinationMarker.setTag(Constants.DESTINATION_LOCATION_TAG);
        }
        memberLocationMarkers = new HashMap<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int i = 1;
        int memberPositionTracker = -1;
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

            markerBubbleBitmap = iconGenerator.makeIcon(namesList.get(memberPositionTracker).toString());
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))).title(member.getKey()).icon(BitmapDescriptorFactory.fromBitmap(markerBubbleBitmap)));
            ++i;
            builder.include(marker.getPosition());
            memberLocationMarkers.put(member.getKey(), marker);
            if (i > 5) {
                i = 1;
            }
        }
        if (zoomFit) {
            if (destinationIconBitmap != null) {
                builder.include(startMarker.getPosition());
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

    void showCheckPointEditOption(final int checkPointMark) {
        if (mMap != null) {
            mMap.setPadding(0, 0, 0, 200);
        }
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Checkpoint-" + checkPointMark, Snackbar.LENGTH_INDEFINITE)
                .setAction("EDIT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openCheckPointEditor(checkPointMark);
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

    void openCheckPointEditor(int position) {
        isCheckPointEdit = true;
        checkPointMakrerEditPosition = position;
        placePickerDialog();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer clickCount = (Integer) marker.getTag();
        if (clickCount != null) {
            if (clickCount == Constants.START_LOCATION_TAG | clickCount == Constants.DESTINATION_LOCATION_TAG) {
                showStreetViewSnackBar(marker);
            } else {
                for (int i = 1; i <= checkPointCoordinateMap.size(); i++) {
                    if (clickCount == i) {
                        Toast.makeText(MapsActivity.this, "i:" + i, Toast.LENGTH_SHORT).show();
                        showCheckPointEditOption(i);
                    }

                }
            }

        }
        return false;
    }

    void showStreetViewSnackBar(final Marker marker) {
        if (mMap != null) {
            mMap.setPadding(0, 0, 0, 200);
        }
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "See how it looks?", Snackbar.LENGTH_LONG)
                .setAction("SEE IT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MapsActivity.this, StreetViewActivity.class);
                        intent.putExtra("latitude", marker.getPosition().latitude);
                        intent.putExtra("longitude", marker.getPosition().longitude);
                        startActivity(intent);

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

    // version 2: ENDS HERE

}