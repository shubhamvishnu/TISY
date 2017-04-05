package com.projects.shubhamkhandelwal.tisy;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
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
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatsRecyclerViewAdpater;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventChat;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.Note;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsDetails;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestsRecyclerAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SearchResultsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SentEventJoinRequestRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.projects.shubhamkhandelwal.tisy.Classes.TimeStamp;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.yalantis.com.foldingtabbar.FoldingTabBar;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    public static final int REQUEST_ACCESS_WRITE_STORAGE = 2;
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

    boolean fabOptionsClicked = false;
    int emoticon = 0;

    // View Objects
    CoordinatorLayout coordinatorLayout;

    // event infomation variables
    List<String> memberProfileImageUrls; // profile Image URL of every member in the event
    List<String> lastSeenInfo;
    List<String> memberProfileName; // member name for users in the event

    // search variables
    String nameSearch; // name/username/eventID searched for by the user
    RecyclerView searchOptionChoiceRecyclerView; // search option recyclerview
    SearchResultsRecyclerViewAdapter searchResultsRecyclerViewAdapter; // search option recyclerview adapter

    ProgressDialog progressDialog;

    InterstitialAd mInterstitialAd;
    boolean editDestinationLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2840079713824644~7949777217");
        initServices();

        // initialize the GoogleMaps with the activity's context. To create custom icons for the markers.
        MapsInitializer.initialize(getApplicationContext());

        // initializing objects and variables
        // initalizing view objects
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);


        // initializing variable
        //initializing String variables
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        nameSearch = new String();

        editDestinationLocation = false;

        zoomFit = false;

        // initializing Class Objects
        eventInfo = new EventInfo();

        // initializing Collection Objects

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


        FoldingTabBar tabBar = (FoldingTabBar) findViewById(R.id.folding_tab_bar);
        tabBar.setOnFoldingItemClickListener(new FoldingTabBar.OnFoldingItemSelectedListener() {
            @Override
            public boolean onFoldingItemSelected(@NotNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.ftb_shutter: {
                        checkForWriteStoragePermission();
                        break;
                    }
                    case R.id.ftb_chat: {
                        showChatsDialog();
                        break;
                    }
                    case R.id.ftb_more: {
                        checkGPS();
                        break;
                    }
                    case R.id.ftb_zoom_fit: {
                        LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showGPSAlert();
                        } else {
                            zoomFitMembers();
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
                        mMap.setPadding(0, 120, 0, 0);
                    }
                    fabOptionsClicked = false;
                }else{
                    if(mMap !=null){
                        mMap.setPadding(0, 120, 0, 320);
                        fabOptionsClicked = true;
                    }
                }

            }
        });
        tabBar.setBackground(getResources().getDrawable(R.drawable.active_members_recycler_view_item_background));


        init();
        initAdd();
        initProgressDialog();
    }

    void checkForWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkWritePermission();
        } else {

            progressDialog.show();
            captureScreen();
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

    void checkWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // toast the reason why we need the permission
                    showWritePermissionAlert();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_WRITE_STORAGE);
            }
        }
    }

    void showWritePermissionAlert() {
        Alerter.create(MapsActivity.this)
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

    void initAdd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2840079713824644/6953624816");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                checkCount();
            }
        });

        requestNewInterstitial();
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

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = this.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    void initServices() {
        if (!Constants.LOCATION_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), LocationListenerService.class));
        }
        if (!Constants.CHAT_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), ChatNotificationService.class));
        }
        if (!Constants.REQUEST_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), RequestNotificationService.class));
        }
    }

    void checkCount() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            exitMapEvent();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
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
                toEventInfoActivity();
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
            Alerter.create(this)
                    .setText("Oops! no internet connection...")
                    .setBackgroundColor(R.color.colorAccent)
                    .show();

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
                if (checkInternetConnection() && (!place.getLatLng().toString().isEmpty())) {
                    showCheckPointAddOptionDialog(place.getLatLng(), place.getName().toString());
                } else {
                    Alerter.create(this)
                            .setText("Oops! no internet connection...")
                            .setBackgroundColor(R.color.colorAccent)
                            .show();
                }
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


    void toEventInfoActivity() {
        Intent intent = new Intent(MapsActivity.this, EventInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
                    Toast.makeText(MapsActivity.this, "We would love to hear your thoughts!", Toast.LENGTH_SHORT).show();
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
                Alerter.create(MapsActivity.this)
                        .setText("Thank you for your thoughts!")
                        .setBackgroundColor(R.color.colorPrimaryDark)
                        .show();
            }
        });
    }

    void showChatsDialog() {

        ImageButton backArrowImageView;
        final Dialog chatsDialog = new Dialog(this, R.style.chat_dialog_style);

        chatsDialog.setContentView(R.layout.recycler_view_chats_layout);

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

        chatsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateChatsReadCount();
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

    void updateChatsReadCount() {

        Firebase chatsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        chatsFirebase.keepSynced(true);
        chatsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long childCount = dataSnapshot.getChildrenCount();
                SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(MapsActivity.this);
                int count = sqLiteDatabaseConnection.getCount(Constants.currentEventId);
                if (count < childCount) {
                    sqLiteDatabaseConnection.updateCount(Constants.currentEventId, (int) childCount);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void sendMessage(final String message) {
        SQLiteDatabaseConnection sqlLiteDatabaseConnection = new SQLiteDatabaseConnection(this);
        int count = sqlLiteDatabaseConnection.getCount(Constants.currentEventId);
        sqlLiteDatabaseConnection.updateCount(Constants.currentEventId, ++count);
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
        mMap.setPadding(0, 120, 0, 0);
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
        userlocationAction();
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);


    }

    void initializeMembers() {
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


    void openGPSSettings() {
        if (mMap != null) {
            mMap.setPadding(0, 120, 0, 0);
        }
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

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
        if(checkInternetConnection()){

        userExit();}else{
            Alerter.create(this)
                    .setText("Oops! No internet connection...")
                    .setBackgroundColor(R.color.colorAccent)
                    .show();
        }
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
                checkCount();
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

            showPermissionAlert();

        } else {
            mMap.setMyLocationEnabled(true);
            // listener for change in location of the user
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if (location.getAccuracy() <= 10 && location.getAccuracy() != 0.0) {

                        checkNearCheckPoint(location);
                        updateUserCurrentLocation(location);
                    } else {
                        updateStatus();
                        updateMyLocation(location);
                    }
                }
            });
            changeInLocation();
        }


    }
    void updateStatus(){
        Firebase updateLastKnowStatus = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username);
        updateLastKnowStatus.keepSynced(true);
        Map<String, Object> lastSeenMap = new HashMap<>();
        lastSeenMap.put("lastSeen", TimeStamp.getLastSeen());
        updateLastKnowStatus.updateChildren(lastSeenMap);

    }


    void showPermissionAlert() {
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
        Alerter.create(this)
                .setTitle("Checkpoint reached!")
                .setDuration(5000)
                .setBackgroundColor(R.color.colorAccent)
                .show();
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 500 milliseconds
        v.vibrate(500);
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



    void updateMemberLocation(DataSnapshot dataSnapshot) {
        Marker marker = (Marker) memberLocationMarkers.get(dataSnapshot.getKey().toString());
        String[] coordinates = dataSnapshot.getValue().toString().split(",");
        LatLng latLng = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        if (marker != null) {
            marker.setPosition(latLng);
        }
    }

    void updateMyLocation(Location location) {

        Marker marker = (Marker) memberLocationMarkers.get(username);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
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
                //showStreetViewSnackBar(marker);
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
    }

    public class EventMemberViewRecyclerViewAdapter extends RecyclerView.Adapter<EventMemberViewRecyclerViewAdapter.EventMembersViewRecyclerViewHolder> {
        List<String> memberList;
        List<LatLng> memberCoordinates;
        Context context;
        GoogleMap googleMap;
        private LayoutInflater inflator;

        public EventMemberViewRecyclerViewAdapter(Context context, GoogleMap googleMap) {
            inflator = LayoutInflater.from(context);
            this.context = context;
            memberList = new ArrayList<>();
            memberCoordinates = new ArrayList<>();
            this.googleMap = googleMap;
            init();

        }

        void init() {
            Firebase initMembersFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
            initMembersFirebase.keepSynced(true);
            initMembersFirebase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    int position = memberList.size();

                    memberList.add(dataSnapshot.getKey());
                    String[] coordinate = dataSnapshot.getValue().toString().split(",");
                    LatLng latLng = new LatLng(Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1]));
                    memberCoordinates.add(latLng);
                    notifyItemInserted(position);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    int index = memberList.indexOf(dataSnapshot.getKey());
                    memberList.remove(index);
                    memberCoordinates.remove(index);
                    notifyItemRemoved(index);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        @Override
        public EventMembersViewRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflator.inflate(R.layout.recycler_view_active_event_members_row_layout, parent, false);
            EventMembersViewRecyclerViewHolder viewHolder = new EventMembersViewRecyclerViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(EventMembersViewRecyclerViewHolder holder, int position) {
            holder.memberTextView.setText(memberList.get(position));
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        class EventMembersViewRecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView memberTextView;

            public EventMembersViewRecyclerViewHolder(View itemView) {
                super(itemView);
                memberTextView = (TextView) itemView.findViewById(R.id.active_event_members_text_view);
                memberTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mMap != null) {
                            CameraUpdate center =
                                    CameraUpdateFactory.newLatLng(memberCoordinates.get(getPosition()));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                            mMap.moveCamera(center);
                            mMap.animateCamera(zoom);
                        }
                    }
                });
            }
        }

    }

    // version 2: ENDS HERE

}