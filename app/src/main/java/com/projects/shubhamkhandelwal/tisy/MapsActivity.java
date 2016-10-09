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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatsRecyclerViewAdpater;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventChat;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfoRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsDetails;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsRecyclerAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SentEventJoinRequestRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String MAPS_ACTIVITY_TAG = "MapsActivity";
    public static final int REQUEST_PERMISSION_SETTINGS = 1;
    public static boolean zoomFit;
    public static Dialog chatsDialog;
    public static int numberOfRequests = 0;
    public static int numberOfUnreadChats = 0;
    public static int numberOfReadChats = 0;

    // TODO: could make this a class later
    String admin;
    String username;
    Map<String, Object> members;
    List<RequestsDetails> joinRequests;
    RecyclerView eventRequestRecyclerView;
    RequestsRecyclerAdapter requestsRecyclerAdapter;
    RecyclerView eventChatsRecyclerView;
    ChatsRecyclerViewAdpater chatsRecyclerViewAdapter;
    //Object for eventInfo
    EventInfo eventInfo;
    GoogleMap mMap;
    Firebase firebase;
    ImageButton eventInfoImageButton;
    ImageButton allIconsInOneImageButton;
    List<String> membersList;
    List<String> memberCoordinate;
    List<String> memberProfileImageUrls;
    String adminValue;
    int movement = 1;
    CoordinatorLayout coordinatorLayout;
    String startLocationTextView;
    String destLocationTextView;
    String eventDescription;
    Map<String, Object> memberLocationMarkers;
    Dialog requestsDialog;
    ChildEventListener chatsChildEventListener;
    Firebase unreadChatsFirebase;
    int chatNotificationCount;
    Bitmap destinationIconBitmap;
    int memberUriCount;

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(32,
                32, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setTitle("TISY");
        //Firebase context
        Firebase.setAndroidContext(this);
        MapsInitializer.initialize(getApplicationContext());
        chatNotificationCount = 0;

        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        // intializing admin variable
        admin = new String();
        adminValue = new String();

        // intializing EventInfo Object
        eventInfo = new EventInfo();
        // initalizing the requests List Object
        joinRequests = new ArrayList<>();

        zoomFit = false;

        movement = 1;
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
        memberProfileImageUrls = new ArrayList<>();

        eventInfoImageButton = (ImageButton) findViewById(R.id.eventInfoImageButton);
        allIconsInOneImageButton = (ImageButton) findViewById(R.id.allInOneIcon);
        memberLocationMarkers = new HashMap<>();

        startService(new Intent(getBaseContext(), InternetConnectionService.class));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (Constants.currentEventId == null) {
            backToMain();
        } else {
            requestOptionOnClick();
            unreadChats();
            loadDestinationIcon();
        }

        //TODO: change them later
        // previous color #666666


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
                showAllInOneDialog();
            }
        });

        eventInfoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventInfoDialog();
            }
        });
    }

    /*  void loadEventInfo(){
          FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
          StorageReference imageStorageReference = firebaseStorage.getReferenceFromUrl("gs://fir-trio.appspot.com/" + Constants.currentEventId + "/dIcon");

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
    public void loadDestinationIcon() {

        switch (Constants.dIconResourceId) {
            case 1: {

                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_walking);
                changeBitMapColor(destinationIconBitmap);
                break;
            }
            case 2: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_swimming);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 3: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_spa);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 4: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_gym);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 5: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_drinks);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 6: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_casino);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 7: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination__zoo);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 8: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_amusement_park);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 9: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bowling_alley);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 10: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_aquarium);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 11: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_night_club);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 12: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_running);
                changeBitMapColor(destinationIconBitmap);

                break;

            }
            case 13: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_football);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 14: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_gaming);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 15: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_bicycle);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 16: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_cafe);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 17: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_restaurant);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 18: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_dinning);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 19: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pizza);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 20: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hotel);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 21: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_university);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 23: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_library);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 24: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_museum);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 25: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_beauty_salon);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 26: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_school);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 27: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_home);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 28: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_stadium);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 29: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_park);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 30: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pharmacy);
                changeBitMapColor(destinationIconBitmap);

                break;

            }

            case 31: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hospital);
                changeBitMapColor(destinationIconBitmap);

                break;

            }
            case 32: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_worship);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 33: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_mall);
                changeBitMapColor(destinationIconBitmap);


                break;
            }

            case 34: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_book_store);
                changeBitMapColor(destinationIconBitmap);


                break;
            }
            case 35: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_convenience_store);
                changeBitMapColor(destinationIconBitmap);


                break;
            }
            case 36: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_liquor_store);
                changeBitMapColor(destinationIconBitmap);


                break;
            }
            case 37: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_laundry);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 38: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_print_shop);
                changeBitMapColor(destinationIconBitmap);

                break;
            }
            case 39: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_grocery_store);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 40: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_parking);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 41: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_airport);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 42: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_train_station);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 43: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bus_station);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 44: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_subway_station);
                changeBitMapColor(destinationIconBitmap);

                break;
            }

            case 45: {
                destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_tram);
                changeBitMapColor(destinationIconBitmap);

                break;
            }


        }
    }

    void changeBitMapColor(Bitmap myBitmap) {

        Paint pnt = new Paint();
        Bitmap myBit = myBitmap;

        Canvas myCanvas = new Canvas(myBit);
        int myColor = myBit.getPixel(0, 0);

        // Set the colour to replace.
        // TODO: change color later
        ColorFilter filter = new LightingColorFilter(myColor, Color.parseColor("#900C3F"));

        pnt.setColorFilter(filter);

        // Draw onto new bitmap. result Bitmap is newBit
        myCanvas.drawBitmap(myBit, 0, 0, pnt);

        destinationIconBitmap = myBit;
        initializeEventInfo();
    }

    void initializeEventInfo() {
        Firebase eventInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/info");
        eventInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String da = dataSnapshot.child("sLocation").toString();
                Log.d("mapsssssssss", da);
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

    void showAllInOneDialog() {

        final Dialog allInOneDialog = new Dialog(this, R.style.event_info_dialog_style);
        allInOneDialog.setContentView(R.layout.dialog_all_in_one_layout);

        ImageButton requestIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_request_icon);
        ImageButton chatIconImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_chat_icon);
        ImageButton zoomFitImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_zoom_fit_icon);
        ImageButton addNewMemberImageButton = (ImageButton) allInOneDialog.findViewById(R.id.dialog_add_new_member);
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
        Window window = allInOneDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        allInOneDialog.setCanceledOnTouchOutside(true);
        allInOneDialog.show();


    }
    void sendMemberRequest(){

        Dialog sendMemberRequestDialog = new Dialog(this, R.style.event_info_dialog_style);
        sendMemberRequestDialog.setContentView(R.layout.dialog_send_request_from_event_layout);

        final EditText sendJoinRequestEventIdEditText;
        final Button sendJoinRequestButton;
        RecyclerView eventJoinRequestSendRecyclerView;
        sendJoinRequestEventIdEditText = (EditText) sendMemberRequestDialog.findViewById(R.id.dialog_send_join_request_edit_text);
        sendJoinRequestButton = (Button) sendMemberRequestDialog.findViewById(R.id.dialog_send_join_request_button);
        sendJoinRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userId = sendJoinRequestEventIdEditText.getText().toString();
                if(userId == null || userId.isEmpty()){
                    //TODO: show snackbar here
                }else{
                    if(userId.equals(username)){
                        //TODO: show snackbar here
                        sendJoinRequestEventIdEditText.setText("");
                    }else{
                        Firebase userIdCheckFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + userId);
                        userIdCheckFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Firebase sendRequestFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS);
                                    HashMap<String, Object> sendRequestUsername = new HashMap<String, Object>();
                                    sendRequestUsername.put(userId, Constants.currentEventId);
                                    sendRequestFirebase.updateChildren(sendRequestUsername, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            sendJoinRequestEventIdEditText.setText("");
                                            //TODO: showsnackbar here
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


    void backToMain() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    void eventInfoDialog() {
        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
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
                for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                    membersList.add(snapshot.getKey());
                    memberCoordinate.add(snapshot.getValue().toString());
                }
                //showEventInfoDialog();
                loadProfileImageUrls();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void loadProfileImageUrls() {
        memberProfileImageUrls = new ArrayList<>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("TISY");
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        memberUriCount = 0;
        for (String name : membersList) {
            Log.d("memberList", membersList.size() + " : " + name);
            firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + name);
            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("memberList", dataSnapshot.getValue().toString());
                    ++memberUriCount;
                    memberProfileImageUrls.add(dataSnapshot.child("userPhotoUri").getValue().toString());
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
        RecyclerView eventInfoMembersRecyclerView;


        Dialog eventInfoDialog = new Dialog(this, R.style.event_info_dialog_style);
        eventInfoDialog.setContentView(R.layout.dialog_event_info_layout);

        eventIdDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.event_id_info_text_view);
        startLocationDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.start_location_desc_text_view);
        destLocationDialogTextView = (TextView) eventInfoDialog.findViewById(R.id.dest_location_desc_text_view);
        eventDescriptionTextView = (TextView) eventInfoDialog.findViewById(R.id.event_desc_text_view);

        eventInfoMembersRecyclerView = (RecyclerView) eventInfoDialog.findViewById(R.id.members_recycler_view);
        eventInfoMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        eventIdDialogTextView.setText(Constants.currentEventId);
        startLocationDialogTextView.setText(startLocationTextView);
        destLocationDialogTextView.setText(destLocationTextView);
        eventDescriptionTextView.setText(eventDescription);

        eventInfoMembersRecyclerView.setHasFixedSize(true);
        EventInfoRecyclerViewAdapter adapter = new EventInfoRecyclerViewAdapter(getApplicationContext(), membersList, memberCoordinate, memberProfileImageUrls);
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
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesName.CHATS_READ_COUNT, MODE_PRIVATE);
        if (sharedPreferences.contains("chats_read")) {
            numberOfReadChats = sharedPreferences.getInt("chats_read", 0);
        }
        unreadChatsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        chatsChildEventListener = unreadChatsFirebase.addChildEventListener(new ChildEventListener() {
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
        eventIdTextView.setText(Constants.currentEventId);

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
        GPSEnabledCheck();

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
    void requestOptionOnClick() {
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
                Log.d("requests", joinRequests.toString());
                Log.d("requests", "" + numberOfRequests);
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

        List<RequestsDetails> request = joinRequests;
        // TODO: remove all the requests for that event from the requests database
        requestsDialog = new Dialog(this, R.style.dialog_sent_request_detail);
        requestsDialog.setContentView(R.layout.recycler_view_requests_layout);

        final Button closeEventRequestDialogButton = (Button) requestsDialog.findViewById(R.id.close_event_requests_dialog_button);
        eventRequestRecyclerView = (RecyclerView) requestsDialog.findViewById(R.id.event_requests_recycler_view);
        eventRequestRecyclerView.setHasFixedSize(true);

        requestsRecyclerAdapter = new RequestsRecyclerAdapter(getApplicationContext(), request);
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

                    if (!checkInternetConnection()) {
                        String message = "No internet connection";
                        showIneternetConnectionSnackBar(message);

                    } else {
                        updateUserCurrentLocation(location);
                    }

                }
            });
            changeInLocation();
        }


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
        Firebase membersUpdateFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
        membersUpdateFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members = new HashMap<>();
                if (dataSnapshot.child("members").exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                        members.put(snapshot.getKey().toString(), snapshot.getValue().toString());
                    }
                    zoomFit = true;
                    updateMapMembers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

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

        if (destinationIconBitmap != null) {
            String[] destCoordinates = eventInfo.getdLocation().split(",");
            String[] startCoordinates = eventInfo.getsLocation().split(",");
            startMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(startCoordinates[0]), Double.parseDouble(startCoordinates[1]))).title("Start Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.start_location_icon)).snippet(eventInfo.getsLocationDesc()));
            destinationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(destCoordinates[0]), Double.parseDouble(destCoordinates[1]))).title("Destination Location").icon(BitmapDescriptorFactory.fromBitmap(destinationIconBitmap)).snippet(eventInfo.getdLocationDesc()));
        }
        memberLocationMarkers = new HashMap<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int i = -1;

        for (Map.Entry<String, Object> member : members.entrySet()) {
            String[] coordinates = member.getValue().toString().split(",");
            // generate random number
            int color = Color.parseColor(Constants.colorPalette[++i]);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);

            // TODO: create new variable for storing the latlng values
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))).title(member.getKey()).icon(BitmapDescriptorFactory.defaultMarker(hsv[0])));
            builder.include(marker.getPosition());
            memberLocationMarkers.put(member.getKey(), marker);
            if (i > 12) {
                i = -1;
            }
        }
        if (zoomFit) {
            if (destinationIconBitmap != null) {
                builder.include(startMarker.getPosition());
                builder.include(destinationMarker.getPosition());
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


    void updateMemberLocation(DataSnapshot dataSnapshot) {

        Marker marker = (Marker) memberLocationMarkers.get(dataSnapshot.getKey().toString());
        String[] coordinates = dataSnapshot.getValue().toString().split(",");
        LatLng latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        if (marker != null) {
            marker.setPosition(latLng);
        }
    }


    // version 2: ENDS HERE

}