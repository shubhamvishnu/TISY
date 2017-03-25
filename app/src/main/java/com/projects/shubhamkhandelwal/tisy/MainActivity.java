package com.projects.shubhamkhandelwal.tisy;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.projects.shubhamkhandelwal.tisy.Classes.ActiveEventsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventDialogs;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.JoinEventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;
import com.tiancaicc.springfloatingactionmenu.MenuItemView;
import com.tiancaicc.springfloatingactionmenu.OnMenuActionListener;
import com.tiancaicc.springfloatingactionmenu.SpringFloatingActionMenu;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    // variables
    // FAB sub-action button tags; to identify which sub-action button was clicked.

    public final static String CREATE_EVENT_TAG = "Create event";
    public final static String ALL_EVENTS_TAG = "All events";
    public final static String SENT_REQUESTS_TAG = "Join an event";
    public final static String RECEIVED_REQUESTS_TAG = "Invites";
    public final static String MY_ACCOUNT_TAG = "My account";
    public final static String PLACES_TAG = "My places";
    public final static String SHARE_APP_TAG = "Share Tisy";


    ProgressDialog progressDialog;

    long activeEventCount; // number of active event of the user.
    long createdEventCount; // number of events created; count of no. of events the user is the admin of.
    long joinedEventCount; // number of events user has joined.

    String userPhotoUri; // user profile photo url.
    String username; // unique username of the user.
    GoogleApiClient mGoogleApiClient;
    // objects
    Intent intent; // common intent to perform intent actions.
    Dialog userAccountDialog; // reference for the user account dialog created; so that their appearance can be manipulated from outside the block.
    Firebase firebase; // reference for the firebase object.
    CoordinatorLayout coordinatorLayoutMainActivity; // reference of the coordinator layout view in the activity_main.xml.
    ImageButton centerFAB; // reference for center main FAB button.
    ImageView mainActivityBackgroundImageView;
    InterstitialAd mInterstitialPlacesAd;
    InterstitialAd mInterstitialCreateEventAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        mainActivityBackgroundImageView = (ImageView) findViewById(R.id.main_activity_backgound_image_view);

        //  Picasso.with(this).load(Uri.parse("https://maps.googleapis.com/maps/api/staticmap?center=28.6618976,77.2273958&scale=2&size="+640+"x"+640+"&zoom=4&key=AIzaSyDHngp3Jx-K8YZYSCNfdljE2gy5p8gcYQQ")).error(R.drawable.login_background).into(mainActivityBackgroundImageView);

        /**
         * checks if the user has logged in or not.
         * if not, then call the login activity.
         */
        SharedPreferences loginCheck = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE);
        if (loginCheck.contains("login")) {

           initMain();
            initLogout();
            //removeNotification();
            // initPlacesAdd();
            //initCreateEventAdd();
        } else {
            intent = new Intent(MainActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        // initialize the username variable from the preference
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        coordinatorLayoutMainActivity = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMainActivity); // initialize the view object.
        userAccountDialog = null;

        /**
         * create two FAB - center main FAB; user account information FAB
         * Steps to create a FAB:
         *      create view items
         *      create menu items
         *      create the menu with items
         * center FAB button:
         *      contains two view items-ImageButton and TextView; These two view are added inside a linear layout.
         */

        // FAB for user account information view.
       /* ImageView userAccountImageIcon = new ImageView(this); // Create an icon imageview.
        userAccountImageIcon.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        userAccountImageIcon.setImageResource(R.drawable.user_account_icon);

        // create menu
        FloatingActionButton floatingActionButton = new FloatingActionButton.Builder(this) //builder for the user account information FAB.
                .setContentView(userAccountImageIcon)
                .setBackgroundDrawable(R.drawable.floating_action_button_selector)
                .build();
        userAccountImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // initializes user information upon request to view the user information
                //initializeUserInformation();
                intent = new Intent(MainActivity.this, CreateEvent.class);
                startActivity(intent);
            }
        });
*/
        final com.melnykov.fab.FloatingActionButton fab = new com.melnykov.fab.FloatingActionButton(this);
        fab.setType(com.melnykov.fab.FloatingActionButton.TYPE_NORMAL);
        fab.setImageResource(R.drawable.edit_slocation_icon);
        fab.setColorPressedResId(R.color.main_activity_create_event);
        fab.setColorNormalResId(R.color.main_activity_create_event);
        fab.setColorRippleResId(R.color.main_activity_create_event);
        fab.setShadow(true);


        new SpringFloatingActionMenu.Builder(this)
                .fab(fab)
                //add menu item via addMenuItem(bgColor,icon,label,label color,onClickListener)
                //添加菜单按钮参数依次是背景颜色,图标,标签,标签的颜色,点击事件
                .addMenuItem(R.color.main_activity_create_event, R.drawable.create_event_icon, CREATE_EVENT_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_option_user_info, R.drawable.info_icon, MY_ACCOUNT_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_option_synergize, R.drawable.synergize_icon, SENT_REQUESTS_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_share_app_option, R.drawable.share_app_image_icon, SHARE_APP_TAG, android.R.color.white, this)

                .addMenuItem(R.color.main_activity_my_places_tag, R.drawable.my_places_location_marker_icon, PLACES_TAG, android.R.color.white, this)
                //  .addMenuItem(R.color.main_activity_option_invite, R.drawable.invite_icon, RECEIVED_REQUESTS_TAG, android.R.color.white,this)
                //you can choose menu layout animation
                //设置动画类型
                .animationType(SpringFloatingActionMenu.ANIMATION_TYPE_TUMBLR)
                //setup reveal color while the menu opening
                //设置reveal效果的颜色
                .revealColor(R.color.colorPrimaryDark)

                //set FAB location, only support bottom center and bottom right
                //设置FAB的位置,只支持底部居中和右下角的位置
                .gravity(Gravity.RIGHT | Gravity.BOTTOM)
                .onMenuActionListner(new OnMenuActionListener() {
                    @Override
                    public void onMenuOpen() {
                        //set FAB icon when the menu opened
                        //设置FAB的icon当菜单打开的时候
                        fab.setImageResource(R.drawable.close_icon);
                    }

                    @Override
                    public void onMenuClose() {
                        //set back FAB icon when the menu closed
                        //设置回FAB的图标当菜单关闭的时候
                        fab.setImageResource(R.drawable.edit_slocation_icon);
                    }
                })
                .build();

    }
    void initLogout(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }
void initMain(){
    if(!Constants.LOCATION_NOTIFICATION_SERVICE_STATUS){
        startService(new Intent(getBaseContext(), LocationListenerService.class));
    }
    if(!Constants.CHAT_NOTIFICATION_SERVICE_STATUS) {
        startService(new Intent(getBaseContext(), ChatNotificationService.class));
    }
    if(!Constants.REQUEST_NOTIFICATION_SERVICE_STATUS){
        startService(new Intent(getBaseContext(), RequestNotificationService.class));
    }
    showAllEventsDialog();
    initProgressDialog();
}
    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loging out!");
        progressDialog.setMessage("Working on it...");
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

    void removeNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.UNREAD_CHATS_NOTIFICATION_ID);
    }

    @Override
    public void onClick(View view) {

        MenuItemView menuItemView = (MenuItemView) view;
        String label = menuItemView.getLabelTextView().getText().toString();

        if (label.equals(CREATE_EVENT_TAG)) {
            toCreateEvent();

        }
        if (label.equals(PLACES_TAG)) {

            toTrackActivity();
            //checkAdDisplayStatus();
        }
        if (label.equals(ALL_EVENTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_EVENTS);
        }
        if (label.equals(RECEIVED_REQUESTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_RECEIVED_REQUESTS);
        }
        if (label.equals(SENT_REQUESTS_TAG)) {
           toJoinEventActivity();
        }
        if (label.equals(MY_ACCOUNT_TAG)) {
            initializeUserInformation();
        }
        if(label.equals(SHARE_APP_TAG)){
           // shareAppDialogOption();
            shareApp("Download tisy from play store using this link:");
        }
    }

    void toJoinEventActivity(){
        Intent intent = new Intent(MainActivity.this, JoinEventActvity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void shareAppDialogOption(){
        final Dialog shareAppDialogOptions = new Dialog(this, R.style.event_info_dialog_style); // initialize the dialog object.
        shareAppDialogOptions.setContentView(R.layout.dialog_share_app_layout); // initialize the dialog layout; xml layout;

        final EditText shareEventMessageEditText, shareEventIDEditText;
        ImageButton shareImageButton;

        shareEventMessageEditText = (EditText) shareAppDialogOptions.findViewById(R.id.share_event_message);
        shareEventIDEditText = (EditText) shareAppDialogOptions.findViewById(R.id.share_event_id);

        shareImageButton = (ImageButton) shareAppDialogOptions.findViewById(R.id.share_image_button);

        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareEventID = shareEventIDEditText.getText().toString();
                String shareEventMessage = shareEventMessageEditText.getText().toString();
                String shareMessage;
                if(shareEventID == null || shareEventMessage == null || shareEventID.isEmpty() || shareEventMessage.isEmpty()){
                   shareMessage = "Download Tisy from play store using this link:\nhttps://play.google.com/store/apps/details?id=com.shubham.hidy.mpshl=en.";

                }else {
                    shareMessage = "Download Tisy from play store using this link:\nhttps://play.google.com/store/apps/details?id=com.shubham.hidy.mpshl=en.\n\n" + shareEventMessage+ "\n"+shareEventID ;
                }
                shareAppDialogOptions.dismiss();
                shareApp(shareMessage);

            }
        });


        Window window = shareAppDialogOptions.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        shareAppDialogOptions.setCanceledOnTouchOutside(true);
        shareAppDialogOptions.show();

    }
    void shareApp(String message){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        MainActivity.this.startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }
    void toCreateEvent() {
        intent = new Intent(MainActivity.this, CreateEvent.class);
        startActivity(intent);
    }


    void toStreetViewActivity() {
        Intent intent = new Intent(MainActivity.this, StreetViewForLocationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void toTrackActivity() {
        Intent intent = new Intent(MainActivity.this, TrackActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showAllEventsDialog();
    }

    void showAllEventsDialog() {
        final RecyclerView activeEventsRecyclerView = (RecyclerView) findViewById(R.id.active_events_recycler_view);

        Firebase checkForEvent = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
        checkForEvent.keepSynced(true);
        checkForEvent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    activeEventsRecyclerView.setVisibility(View.VISIBLE);
                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.no_active_event_relative_layout);
                    relativeLayout.setVisibility(View.INVISIBLE);

                    activeEventsRecyclerView.setHasFixedSize(true);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                    activeEventsRecyclerView.setLayoutManager(linearLayoutManager);

                    ActiveEventsRecyclerViewAdapter activeEventsRecyclerViewAdapter = new ActiveEventsRecyclerViewAdapter(MainActivity.this);
                    activeEventsRecyclerView.setAdapter(activeEventsRecyclerViewAdapter);
                } else {
                    activeEventsRecyclerView.setVisibility(View.INVISIBLE);
                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.no_active_event_relative_layout);
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }


    /**
     * fetches information about the user.
     * after fetching-user information dialog is shown.
     */
    void initializeUserInformation() {

        Firebase userInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        userInfoFirebase.keepSynced(true);
        userInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeEventCount = 0; // total number of events user is a part of currently.
                createdEventCount = 0; // total number of events user has created.
                joinedEventCount = 0; // total number of events user has joined.
                userPhotoUri = new String(); // holds the profile photo image url.

                userPhotoUri = dataSnapshot.child("userPhotoUri").getValue().toString();
                activeEventCount = dataSnapshot.child("activeEvent").getChildrenCount();

                for (DataSnapshot children : dataSnapshot.child("activeEvent").getChildren()) {
                    if (children.getValue().toString().equals("created")) {
                        ++createdEventCount;
                    } else if (children.getValue().toString().equals("joined")) {
                        ++joinedEventCount;
                    }
                }

                // call to show the user information dialog after initialization.
                showInformationDialog();
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    // show the user information dialog
    void showInformationDialog() {
        userAccountDialog = new Dialog(this, R.style.event_info_dialog_style); // initialize the dialog object.
        userAccountDialog.setContentView(R.layout.dialog_user_account_layout); // initialize the dialog layout; xml layout;

        // initialize the view items in the dialog.
        TextView nameEditText = (TextView) userAccountDialog.findViewById(R.id.name_text_view);
        TextView userIdEditText = (TextView) userAccountDialog.findViewById(R.id.user_id_text_view);
        TextView activeEventscountTextView = (TextView) userAccountDialog.findViewById(R.id.active_events_count_text_view);
        TextView createdEventsCountTextView = (TextView) userAccountDialog.findViewById(R.id.created_events_count_text_view);
        TextView joinedEventsCountTextView = (TextView) userAccountDialog.findViewById(R.id.joined_events_count_text_view);
        CircleImageView profileImageView = (CircleImageView) userAccountDialog.findViewById(R.id.profile_image_circle_image_view);
        Button logoutButton = (Button) userAccountDialog.findViewById(R.id.logout_button);

        Picasso.with(this).load(Uri.parse(userPhotoUri)).error(R.drawable.default_profile_image_icon).into(profileImageView);
        SharedPreferences userInfoPreference = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE);
        nameEditText.setText(userInfoPreference.getString("name", null));
        userIdEditText.setText(userInfoPreference.getString("username", null));

        activeEventscountTextView.setText(String.valueOf(activeEventCount));
        createdEventsCountTextView.setText(String.valueOf(createdEventCount));
        joinedEventsCountTextView.setText(String.valueOf(joinedEventCount));

        Window window = userAccountDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        userAccountDialog.setCanceledOnTouchOutside(true);
        userAccountDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                userAccountDialog = null;
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAccountDialog.dismiss();
                progressDialog.show();
                logout();
            }
        });
        userAccountDialog.show();

    }

    void logout() {



        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                       loggedout();
                        // [END_EXCLUDE]
                    }
                });


  /*
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                    .clearApplicationUserData(); // note: it has a return value!
            loggedout();
        } else {
            loggedout();
        }
*/

    }

    void loggedout() {
        getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.CHATS_READ_COUNT, MODE_PRIVATE).edit().clear().apply();

        SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(MainActivity.this);
        sqLiteDatabaseConnection.emptyTable();

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}