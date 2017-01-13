package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.projects.shubhamkhandelwal.tisy.Classes.ActiveEventsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventDialogs;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.MovementTracker;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.squareup.picasso.Picasso;
import com.tiancaicc.springfloatingactionmenu.MenuItemView;
import com.tiancaicc.springfloatingactionmenu.OnMenuActionListener;
import com.tiancaicc.springfloatingactionmenu.SpringFloatingActionMenu;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    // variables
    // FAB sub-action button tags; to identify which sub-action button was clicked.

    public final static String CREATE_EVENT_TAG = "Create Event";
    public final static String JOIN_EVENT_TAG = "Join Event";
    public final static String ALL_EVENTS_TAG = "All Events";
    public final static String REQUESTS_TAG = "Requests";
    public final static String MY_ACCOUNT_TAG = "My Account";
    public final static String TRACK_TAG = "My Tracks";


    long activeEventCount; // number of active event of the user.
    long createdEventCount; // number of events created; count of no. of events the user is the admin of.
    long joinedEventCount; // number of events user has joined.

    String userPhotoUri; // user profile photo url.
    String username; // unique username of the user.

    // objects
    Intent intent; // common intent to perform intent actions.
    Dialog userAccountDialog; // reference for the user account dialog created; so that their appearance can be manipulated from outside the block.
    Firebase firebase; // reference for the firebase object.
    CoordinatorLayout coordinatorLayoutMainActivity; // reference of the coordinator layout view in the activity_main.xml.
    ImageButton centerFAB; // reference for center main FAB button.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * checks if the user has logged in or not.
         * if not, then call the login activity.
         */
        SharedPreferences loginCheck = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE);
        if (loginCheck.contains("login")) {
            startService(new Intent(getBaseContext(), LocationListenerService.class));
            showAllEventsDialog();
        } else {
            intent = new Intent(MainActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        // initialize the username variable from the preference
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        coordinatorLayoutMainActivity = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMainActivity); // initialize the view object.
        userAccountDialog = null; // initialize the user account information dialog.

        /**
         * create two FAB - center main FAB; user account information FAB
         * Steps to create a FAB:
         *      create view items
         *      create menu items
         *      create the menu with items
         * center FAB button:
         *      contains two view items-ImageButton and TextView; These two view are added inside a linear layout.
         */

//        // FAB for user account information view.
//        ImageView userAccountImageIcon = new ImageView(this); // Create an icon imageview.
//        userAccountImageIcon.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//        userAccountImageIcon.setImageResource(R.drawable.user_account_icon);
//
//        // create menu
//        FloatingActionButton floatingActionButton = new FloatingActionButton.Builder(this) //builder for the user account information FAB.
//                .setContentView(userAccountImageIcon)
//                .setBackgroundDrawable(R.drawable.floating_action_button_selector)
//                .build();
//        userAccountImageIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // initializes user information upon request to view the user information
//                initializeUserInformation();
//            }
//        });

        final com.melnykov.fab.FloatingActionButton fab = new com.melnykov.fab.FloatingActionButton(this);
        fab.setType(com.melnykov.fab.FloatingActionButton.TYPE_NORMAL);
        fab.setImageResource(R.drawable.option_main);
        fab.setColorPressedResId(R.color.customColor4);
        fab.setColorNormalResId(R.color.customColor7);
        fab.setColorRippleResId(R.color.customColor8);
        fab.setShadow(true);


        new SpringFloatingActionMenu.Builder(this)
                .fab(fab)
                //add menu item via addMenuItem(bgColor,icon,label,label color,onClickListener)
                //添加菜单按钮参数依次是背景颜色,图标,标签,标签的颜色,点击事件
                .addMenuItem(R.color.customColor2, R.drawable.all_event_main, ALL_EVENTS_TAG, R.color.customColor0,this)
                .addMenuItem(R.color.customColor3, R.drawable.create_main, CREATE_EVENT_TAG, R.color.customColor0,this)
                .addMenuItem(R.color.customColor5, R.drawable.requests_main, REQUESTS_TAG, R.color.customColor0,this)
                .addMenuItem(R.color.customColor6,  R.drawable.my_tracks_main, TRACK_TAG, R.color.customColor0,this)
                .addMenuItem(R.color.customColor4, R.drawable.default_profile_image_icon, MY_ACCOUNT_TAG, R.color.customColor0, this)
                //you can choose menu layout animation
                //设置动画类型
                .animationType(SpringFloatingActionMenu.ANIMATION_TYPE_TUMBLR)
                //setup reveal color while the menu opening
                //设置reveal效果的颜色
                .revealColor(R.color.colorPrimary)
                //set FAB location, only support bottom center and bottom right
                //设置FAB的位置,只支持底部居中和右下角的位置
                .gravity(Gravity.RIGHT | Gravity.BOTTOM)
                .onMenuActionListner(new OnMenuActionListener() {
                    @Override
                    public void onMenuOpen() {
                        //set FAB icon when the menu opened
                        //设置FAB的icon当菜单打开的时候
                        fab.setImageResource(R.drawable.option_main);
                    }

                    @Override
                    public void onMenuClose() {
                        //set back FAB icon when the menu closed
                        //设置回FAB的图标当菜单关闭的时候
                        fab.setImageResource(R.drawable.option_main);
                    }
                })
                .build();
    }



    @Override
    public void onClick(View view) {

        MenuItemView menuItemView = (MenuItemView) view;
        String label = menuItemView.getLabelTextView().getText().toString();


        if (label.equals(CREATE_EVENT_TAG)) {
            intent = new Intent(MainActivity.this, CreateEvent.class);
            startActivity(intent);
        }
        if (label.equals(TRACK_TAG)) {
            toTrackActivity();
        }
        if (label.equals(ALL_EVENTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_EVENTS);

        }
        if (label.equals(REQUESTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_REQUESTS);
        }
        if (label.equals(MY_ACCOUNT_TAG)) {
            initializeUserInformation();
        }
    }
    void toStreetViewActivity(){
        Intent intent = new Intent(MainActivity.this, StreetViewForLocationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void toTrackActivity(){
        Intent intent = new Intent(MainActivity.this, TrackActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    void showAllEventsDialog() {

        RecyclerView activeEventsRecyclerView;
        ActiveEventsRecyclerViewAdapter activeEventsRecyclerViewAdapter;

        activeEventsRecyclerView = (RecyclerView) findViewById(R.id.active_events_recycler_view);
        activeEventsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        activeEventsRecyclerView.setLayoutManager(linearLayoutManager);

        activeEventsRecyclerViewAdapter = new ActiveEventsRecyclerViewAdapter(this);
        activeEventsRecyclerView.setAdapter(activeEventsRecyclerViewAdapter);

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
        userAccountDialog.show();

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