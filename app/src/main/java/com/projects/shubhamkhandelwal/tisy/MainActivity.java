package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.MobileAds;
import com.projects.shubhamkhandelwal.tisy.Classes.ActiveEventsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import com.tiancaicc.springfloatingactionmenu.MenuItemView;
import com.tiancaicc.springfloatingactionmenu.OnMenuActionListener;
import com.tiancaicc.springfloatingactionmenu.SpringFloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    // variables
    // FAB sub-action button tags; to identify which sub-action button was clicked.

    public final static String CREATE_EVENT_TAG = "Create event";
    public final static String SENT_REQUESTS_TAG = "Join an event";
    public final static String MY_ACCOUNT_TAG = "My account";
    public final static String PLACES_TAG = "My places";
    public final static String SHARE_APP_TAG = "Share Tisy";

    List<String> activeEventList;
    ProgressDialog progressDialog;


    String username; // unique username of the user.

    // objects
    Intent intent; // common intent to perform intent actions.


    CoordinatorLayout coordinatorLayoutMainActivity; // reference of the coordinator layout view in the activity_main.xml.

    ImageView mainActivityBackgroundImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2840079713824644~7949777217");


        mainActivityBackgroundImageView = (ImageView) findViewById(R.id.main_activity_backgound_image_view);

        //  Picasso.with(this).load(Uri.parse("https://maps.googleapis.com/maps/api/staticmap?center=28.6618976,77.2273958&scale=2&size="+640+"x"+640+"&zoom=4&key=AIzaSyDHngp3Jx-K8YZYSCNfdljE2gy5p8gcYQQ")).error(R.drawable.login_background).into(mainActivityBackgroundImageView);

        /**
         * checks if the user has logged in or not.
         * if not, then call the login activity.
         */
        SharedPreferences loginCheck = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE);
        if (loginCheck.contains("login")) {

           initMain();
            initDatabase();

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



        final com.melnykov.fab.FloatingActionButton fab = new com.melnykov.fab.FloatingActionButton(this);
        fab.setType(com.melnykov.fab.FloatingActionButton.TYPE_NORMAL);
        fab.setImageResource(R.drawable.edit_slocation_icon);
        fab.setColorPressedResId(R.color.main_activity_create_event);
        fab.setColorNormalResId(R.color.main_activity_create_event);
        fab.setColorRippleResId(R.color.main_activity_create_event);
        fab.setShadow(true);


        new SpringFloatingActionMenu.Builder(this)
                .fab(fab)

                .addMenuItem(R.color.main_activity_create_event, R.drawable.create_event_icon, CREATE_EVENT_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_option_user_info, R.drawable.user_info_image_icon, MY_ACCOUNT_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_option_synergize, R.drawable.synergize_icon, SENT_REQUESTS_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_share_app_option, R.drawable.share_app_image_icon, SHARE_APP_TAG, android.R.color.white, this)
                .addMenuItem(R.color.main_activity_my_places_tag, R.drawable.my_places_location_marker_icon, PLACES_TAG, android.R.color.white, this)

                .animationType(SpringFloatingActionMenu.ANIMATION_TYPE_TUMBLR)

                .revealColor(R.color.colorPrimaryDark)


                .gravity(Gravity.RIGHT | Gravity.BOTTOM)
                .onMenuActionListner(new OnMenuActionListener() {
                    @Override
                    public void onMenuOpen() {

                        fab.setImageResource(R.drawable.close_icon);
                    }

                    @Override
                    public void onMenuClose() {

                        fab.setImageResource(R.drawable.edit_slocation_icon);
                    }
                })
                .build();

    }
void initDatabase(){


    Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
    firebase.keepSynced(true);
    firebase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            activeEventList = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                activeEventList.add(snapshot.getKey());
            }
            checkForExistence(activeEventList);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    });
}

    void checkForExistence(List<String> activeEventList) {
        SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(this);
        for (int i = 0; i < activeEventList.size(); i++) {
            if (!sqLiteDatabaseConnection.checkForEvent(activeEventList.get(i))) {
                sqLiteDatabaseConnection.insertRow(activeEventList.get(i), 0);
            }
        }

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
        }
        if (label.equals(SENT_REQUESTS_TAG)) {
           toJoinEventActivity();
        }
        if (label.equals(MY_ACCOUNT_TAG)) {
           showUserInfo();
        }
        if(label.equals(SHARE_APP_TAG)){
            shareApp("Download tisy from play store using this link:");
        }
    }
    void showUserInfo(){
        Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void toJoinEventActivity(){
        Intent intent = new Intent(MainActivity.this, JoinEventActvity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void toSearchEventActivity(){
        Intent intent = new Intent(MainActivity.this, SearchEventActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
        final FloatingTextButton createEventFtb = (FloatingTextButton) findViewById(R.id.create_ftb_button);
        final FloatingTextButton joinEventFtb = (FloatingTextButton) findViewById(R.id.join_request_ftb_button);
        final FloatingTextButton helpFtb = (FloatingTextButton) findViewById(R.id.help_main_ftb_button);
        final Firebase checkForEvent = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
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
                    checkForFirstTime();

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        createEventFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCreateEvent();
            }
        });

        joinEventFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSearchEventActivity();
            }
        });

        helpFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIntroDialog();

            }
        });
    }
void checkForFirstTime(){
    SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE);
    if (sharedPreferences.contains("main_navigation_help")) {

    } else {
        showIntroDialog();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("main_navigation_help", "done");
        editor.apply();
    }
}
    void showIntroDialog(){
        final Dialog introDialog = new Dialog(this, R.style.event_info_dialog_style); // initialize the dialog object.
        introDialog.setContentView(R.layout.dialog_intro_layout); // initialize the dialog layout; xml layout;

        FloatingTextButton createFtb = (FloatingTextButton) introDialog.findViewById(R.id.create_intro_ftb);
        FloatingTextButton joinFtb = (FloatingTextButton) introDialog.findViewById(R.id.join_intro_ftb);
        FloatingTextButton myPlacesFtb = (FloatingTextButton) introDialog.findViewById(R.id.my_places_intro_ftb);
        Button doneButton = (Button) introDialog.findViewById(R.id.done_intro_button);
        createFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introDialog.dismiss();
                toCreateEvent();
            }
        });
        joinFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introDialog.dismiss();
                toSearchEventActivity();
            }
        });
        myPlacesFtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introDialog.dismiss();
                toTrackActivity();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introDialog.dismiss();
            }
        });

        Window window = introDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        introDialog.setCanceledOnTouchOutside(true);
        introDialog.show();
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


}