package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventDialogs;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    // variables
    // FAB sub-action button tags; to identify which sub-action button was clicked.
    public static final String MAIN_ACITIVITY_TAG = "MainActivity";
    public final static String CREATE_EVENT_TAG = "create_event";
    public final static String JOIN_EVENT_TAG = "send_requests";
    public final static String ALL_EVENTS_TAG = "all_events";
    public final static String REQUESTS_TAG = "requests";
    public final static String RECEIVED_REQUEST_TAG = "received_requests";

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
            if (loginCheck.getBoolean("login", true)) {
                startService(new Intent(getBaseContext(), InternetConnectionService.class));
            }
        } else {
            stopService(new Intent(getBaseContext(), InternetConnectionService.class));
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

        // FAB for user account information view.
        ImageView userAccountImageIcon = new ImageView(this); // Create an icon imageview.
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
                initializeUserInformation();
            }
        });

        // create menu with items
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this) // attaching the user account information FAB to the builder.
                .attachTo(floatingActionButton)
                .build();

        // Custom FAB for showing options.
        centerFAB = (ImageButton) findViewById(R.id.center_fab); // initialize the center FAB main button.
        centerFAB.setColorFilter(getResources().getColor(R.color.colorAccent));

        // Custom FAB menu.
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        itemBuilder.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));

        // layout for the menu items.
        LinearLayout subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // create event menu item
        ImageView createEventCircleButton = new ImageView(this); // image view for the menu item.
        createEventCircleButton.setImageResource(R.drawable.add_icon);
        createEventCircleButton.setAdjustViewBounds(true);
        createEventCircleButton.setBackground(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));
        createEventCircleButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        createEventCircleButton.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        createEventCircleButton.setPadding(50, 50, 50, 50);

        TextView createEventTextView = new TextView(this); // text view for the menu item.
        createEventTextView.setText("Create Event");
        createEventTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        createEventTextView.setTextSize(16);
        createEventTextView.setGravity(Gravity.CENTER);
        createEventTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        createEventTextView.setLayoutParams(new ViewGroup.LayoutParams(500, 150));

        // add the views to the layout for the menu items.
        subActionFABLinearLayout.addView(createEventCircleButton);
        subActionFABLinearLayout.addView(createEventTextView);

        // add the layout as an item having the created views. The linear layout acts as a sub-action menu item.
        SubActionButton createEventSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        createEventSubActionButton.setTag(CREATE_EVENT_TAG);
        createEventSubActionButton.setOnClickListener(this);

        //join event menu item.
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        ImageView sendRequestCircleButton = new ImageView(this);
        sendRequestCircleButton.setAdjustViewBounds(true);
        sendRequestCircleButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sendRequestCircleButton.setBackground(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));
        sendRequestCircleButton.setImageResource(R.drawable.requests_icon);
        sendRequestCircleButton.setPadding(50, 50, 50, 50);
        sendRequestCircleButton.setLayoutParams(new ViewGroup.LayoutParams(200, 200));


        TextView joinEventTextView = new TextView(this);
        joinEventTextView.setText("Join Event");
        joinEventTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        joinEventTextView.setTextSize(16);
        joinEventTextView.setGravity(Gravity.CENTER);
        joinEventTextView.setLayoutParams(new ViewGroup.LayoutParams(500, 150));

        subActionFABLinearLayout.addView(sendRequestCircleButton);
        subActionFABLinearLayout.addView(joinEventTextView);

        SubActionButton sendRequestSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        sendRequestSubActionButton.setTag(JOIN_EVENT_TAG);
        sendRequestSubActionButton.setOnClickListener(this);


        // all events menu item.
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView allActiveEventsCircleButton = new ImageView(this);
        allActiveEventsCircleButton.setAdjustViewBounds(true);
        allActiveEventsCircleButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        allActiveEventsCircleButton.setImageResource(R.drawable.all_events_active_icon);
        allActiveEventsCircleButton.setBackground(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));
        allActiveEventsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        allActiveEventsCircleButton.setPadding(50, 50, 50, 50);

        TextView allEventsTextView = new TextView(this);
        allEventsTextView.setText("All Events");
        allEventsTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        allEventsTextView.setTextSize(16);
        allEventsTextView.setGravity(Gravity.CENTER);
        allEventsTextView.setLayoutParams(new ViewGroup.LayoutParams(500, 150));
        subActionFABLinearLayout.addView(allActiveEventsCircleButton);
        subActionFABLinearLayout.addView(allEventsTextView);

        SubActionButton allEventsSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        allEventsSubActionButton.setTag(ALL_EVENTS_TAG);
        allEventsSubActionButton.setOnClickListener(this);


        // all sent requests menu item.
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView requestsCircleButton = new ImageView(this);
        requestsCircleButton.setAdjustViewBounds(true);
        requestsCircleButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        requestsCircleButton.setImageResource(R.drawable.join_event_icon);
        requestsCircleButton.setBackground(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));
        requestsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        requestsCircleButton.setPadding(50, 50, 50, 50);

        TextView requestsTextView = new TextView(this);
        requestsTextView.setText("Sent Requests");
        requestsTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        requestsTextView.setTextSize(16);
        requestsTextView.setGravity(Gravity.CENTER);
        requestsTextView.setLayoutParams(new ViewGroup.LayoutParams(500, 150));

        subActionFABLinearLayout.addView(requestsCircleButton);
        subActionFABLinearLayout.addView(requestsTextView);

        SubActionButton requestsSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        requestsSubActionButton.setTag(REQUESTS_TAG);
        requestsSubActionButton.setOnClickListener(this);


        // all received requests menu item.
        LinearLayout receivedRequestLinearLayout = new LinearLayout(this);
        receivedRequestLinearLayout.setOrientation(LinearLayout.VERTICAL);
        receivedRequestLinearLayout.setGravity(Gravity.CENTER);
        receivedRequestLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        ImageView receivedRequestsCircleButton = new ImageView(this);
        receivedRequestsCircleButton.setAdjustViewBounds(true);
        receivedRequestsCircleButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        receivedRequestsCircleButton.setImageResource(R.drawable.request_received_icon);
        receivedRequestsCircleButton.setBackground(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));
        receivedRequestsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        receivedRequestsCircleButton.setPadding(50, 50, 50, 50);

        TextView recevivedTextView = new TextView(this);
        recevivedTextView.setText("Received Requests");
        recevivedTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        recevivedTextView.setTextSize(16);
        recevivedTextView.setLayoutParams(new ViewGroup.LayoutParams(500, 150));
        recevivedTextView.setGravity(Gravity.CENTER);

        receivedRequestLinearLayout.addView(receivedRequestsCircleButton);
        receivedRequestLinearLayout.addView(recevivedTextView);

        SubActionButton receivedRequestSubActionButton = itemBuilder.setContentView(receivedRequestLinearLayout).build();
        receivedRequestSubActionButton.setTag(RECEIVED_REQUEST_TAG);
        receivedRequestSubActionButton.setOnClickListener(this);

        // add items to custom FAB menu.
        FloatingActionMenu circleMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(0) // A whole circle!
                .setEndAngle(360)
                .setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large))
                .addSubActionView(createEventSubActionButton, 500, 400)
                .addSubActionView(sendRequestSubActionButton, 500, 400)
                .addSubActionView(allEventsSubActionButton, 500, 400)
                .addSubActionView(requestsSubActionButton, 500, 400)
                .addSubActionView(receivedRequestSubActionButton, 500, 400)
                .attachTo(centerFAB)
                .build();
    }

    @Override
    public void onClick(View view) {
        if (view.getTag().equals(CREATE_EVENT_TAG)) {
            stopService(new Intent(getBaseContext(), InternetConnectionService.class));
            intent = new Intent(MainActivity.this, CreateEvent.class);
            startActivity(intent);
        }
        if (view.getTag().equals(JOIN_EVENT_TAG)) {
            joinEventDialog();
        }
        if (view.getTag().equals(ALL_EVENTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_EVENTS);
        }
        if (view.getTag().equals(REQUESTS_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_REQUESTS);
        }
        if (view.getTag().equals(RECEIVED_REQUEST_TAG)) {
            new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_RECEIVED_REQUESTS);
        }
    }

    /**
     * fetches information about the user.
     * after fetching-user information dialog is shown.
     */
    void initializeUserInformation() {
        activeEventCount = 0; // total number of events user is a part of currently.
        createdEventCount = 0; // total number of events user has created.
        joinedEventCount = 0; // total number of events user has joined.
        userPhotoUri = new String(); // holds the profile photo image url.

        Firebase userInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        userInfoFirebase.keepSynced(true);
        userInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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

    // custom dialog to send request to join another event
    void joinEventDialog() {

        final EditText dialogJoinEventIdEditText;
        final EditText dialogJoinDescEditText;
        final Button joinEventButton;
        final Dialog dialog = new Dialog(this, R.style.dialog_join_request);

        // custom dialog layout
        dialog.setContentView(R.layout.dialog_join_event_layout);

        // initializing view elements for the custom dialog
        dialogJoinEventIdEditText = (EditText) dialog.findViewById(R.id.dialog_join_event_id);
        dialogJoinDescEditText = (EditText) dialog.findViewById(R.id.dialog_join_event_desc);
        joinEventButton = (Button) dialog.findViewById(R.id.joinEventButton);

        // onclick listener for the button
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String joinEventId = dialogJoinEventIdEditText.getText().toString();
                final String joinEventDesc = dialogJoinDescEditText.getText().toString();

                // check for empty condition; send request calling sendRequest(String, String)
                if (joinEventId.isEmpty() || joinEventDesc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "enter the details", Toast.LENGTH_SHORT).show();
                } else {
                    // check if the user is already a part of the event.
                    otherUserEventCheck(joinEventId, joinEventDesc);
                    dialog.dismiss();
                }
            }
        });

        // button touch feed back
        joinEventButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        joinEventButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        joinEventButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
                        break;
                }
                return false;
            }
        });
        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * checks if the user is already a part of the event.
     * show snackbar if already a part; else send request to join.
     */
    void otherUserEventCheck(final String requestEventId, final String requestEventDesc) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "/activeEvent/" + requestEventId);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String message = "You are already a part of the event";
                    showRequestActionSnackBar(message, false);
                } else {

                    // sends request to join the event
                    sendRequest(requestEventId, requestEventDesc);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // sends the request to join an event
    void sendRequest(final String requestEventId, final String requestEventDesc) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + requestEventId);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // checks if the eventID is valid; i.e if it exists
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> updateEvent = new HashMap<String, Object>();
                    updateEvent.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), requestEventDesc);
                    firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + requestEventId + "/requested");
                    firebase.updateChildren(updateEvent, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            // send request for the user to join the event
                            setValues(requestEventId, requestEventDesc);
                        }
                    });

                } else {
                    String message = "Event does not exist";
                    showRequestActionSnackBar(message, false);

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * adds request into the event's requested database
     * @param rID : holds the event unique id
     * @param rDesc : holds the description along with the request
     */
    void setValues(final String rID, final String rDesc) {

        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        Map<String, Object> request = new HashMap<>();
        request.put(rID, rDesc);
        firebase.updateChildren(request, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                String message = "Request sent";
                showRequestActionSnackBar(message, true);
            }
        });
    }

    /**
     * show snackbar
     * @param message : message to be displayed
     * @param available : if true, shows the action to join the event; else does not show action button.
     */
    void showRequestActionSnackBar(String message, boolean available) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayoutMainActivity, message, Snackbar.LENGTH_LONG);
        if (!available) {
            snackbar.setAction("JOIN", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    joinEventDialog();
                }
            });
        }
        snackbar.setActionTextColor(Color.parseColor("#FFFFFF"));
        snackbar.show();
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
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        finish();
    }

}