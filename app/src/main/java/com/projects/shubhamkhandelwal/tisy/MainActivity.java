package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
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
    public static final String MAIN_ACITIVITY_TAG = "MainActivity";
    public final static String CREATE_EVENT_TAG = "create_event";
    public final static String JOIN_EVENT_TAG = "send_requests";
    public final static String ALL_EVENTS_TAG = "all_events";
    public final static String REQUESTS_TAG = "requests";
    public final static String RECEIVED_REQUEST_TAG = "received_requests";
    long activeEventCount;
    long createdEventCount;
    long joinedEventCount;
    String userPhotoUri;

    Intent intent;
    // make sure eventListener is not null
    int count = 0;
    // Firebase Object reference
    Firebase firebase;
    CoordinatorLayout coordinatorLayoutMainActivity;
    ImageButton centerFAB;

    /*
         listener for user status (active; inactive; pending)
         remove the listener on OnDestroy
      */
    /*
     *requests from all the users and to send a new request
     *contains username; and request description
     */
    Map<String, Object> requestEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Context
        Firebase.setAndroidContext(this);

        // initialize users state; inactive variable

        count = 0;
        userPhotoUri = new String();
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
        coordinatorLayoutMainActivity = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMainActivity);

        centerFAB = (ImageButton) findViewById(R.id.center_fab);
        centerFAB.setColorFilter(getResources().getColor(R.color.colorAccent));


        ImageView userAccountImageIcon = new ImageView(this); // Create an icon
        userAccountImageIcon.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        userAccountImageIcon.setImageResource(R.drawable.default_profile_image_icon);
        FloatingActionButton floatingActionButton = new FloatingActionButton.Builder(this)
                .setContentView(userAccountImageIcon)
                .setBackgroundDrawable(R.drawable.floating_action_button_selector)
                .build();
        userAccountImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeUserInformation();
            }
        });
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .attachTo(floatingActionButton)
                .build();


        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));


        // create event
        LinearLayout subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(120, 120));


        CircleImageView createEventCircleButton = new CircleImageView(this);
        createEventCircleButton.setImageResource(R.drawable.add_icon);
        createEventCircleButton.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

        TextView createEventTextView = new TextView(this);
        createEventTextView.setText("Create");
        createEventTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        createEventTextView.setTextSize(8);
        createEventTextView.setGravity(Gravity.CENTER);

        subActionFABLinearLayout.addView(createEventCircleButton);
        subActionFABLinearLayout.addView(createEventTextView);


        SubActionButton createEventSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        createEventSubActionButton.setTag(CREATE_EVENT_TAG);
        createEventSubActionButton.setOnClickListener(this);

        // requests
        LinearLayout sendsubActionFABLinearLayout = new LinearLayout(this);
        sendsubActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        sendsubActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(120, 120));

        CircleImageView sendRequestCircleButton = new CircleImageView(this);
        sendRequestCircleButton.setImageResource(R.drawable.requests_icon);
        sendRequestCircleButton.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

        TextView joinEventTextView = new TextView(this);
        joinEventTextView.setText("Join");
        joinEventTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        joinEventTextView.setTextSize(8);
        joinEventTextView.setGravity(Gravity.CENTER);
        sendsubActionFABLinearLayout.addView(sendRequestCircleButton);
        sendsubActionFABLinearLayout.addView(joinEventTextView);

        SubActionButton sendRequestSubActionButton = itemBuilder.setContentView(sendsubActionFABLinearLayout).build();
        sendRequestSubActionButton.setTag(JOIN_EVENT_TAG);
        sendRequestSubActionButton.setOnClickListener(this);


        // all event requests
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(120, 120));

        CircleImageView allActiveEventsCircleButton = new CircleImageView(this);
        allActiveEventsCircleButton.setImageResource(R.drawable.all_events_active_icon);
        allActiveEventsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        TextView allEventsTextView = new TextView(this);
        allEventsTextView.setText("Events");
        allEventsTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        allEventsTextView.setTextSize(8);
        allEventsTextView.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.addView(allActiveEventsCircleButton);
        subActionFABLinearLayout.addView(allEventsTextView);

        SubActionButton allEventsSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        allEventsSubActionButton.setTag(ALL_EVENTS_TAG);
        allEventsSubActionButton.setOnClickListener(this);


        // all sent requests
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(120, 120));

        CircleImageView requestsCircleButton = new CircleImageView(this);
        requestsCircleButton.setImageResource(R.drawable.join_event_icon);
        requestsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        TextView requestsTextView = new TextView(this);
        requestsTextView.setText("Requests");
        requestsTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        requestsTextView.setTextSize(8);
        requestsTextView.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.addView(requestsCircleButton);
        subActionFABLinearLayout.addView(requestsTextView);

        SubActionButton requestsSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        requestsSubActionButton.setTag(REQUESTS_TAG);
        requestsSubActionButton.setOnClickListener(this);


        // all received requests
        subActionFABLinearLayout = new LinearLayout(this);
        subActionFABLinearLayout.setOrientation(LinearLayout.VERTICAL);
        subActionFABLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(120, 120));

        CircleImageView receivedRequestsCircleButton = new CircleImageView(this);
        receivedRequestsCircleButton.setImageResource(R.drawable.request_received_icon);
        receivedRequestsCircleButton.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

        TextView recevivedTextView = new TextView(this);
        recevivedTextView.setText("Received");
        recevivedTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        recevivedTextView.setTextSize(8);
        recevivedTextView.setGravity(Gravity.CENTER);
        subActionFABLinearLayout.addView(receivedRequestsCircleButton);
        subActionFABLinearLayout.addView(recevivedTextView);

        SubActionButton receivedRequestSubActionButton = itemBuilder.setContentView(subActionFABLinearLayout).build();
        receivedRequestSubActionButton.setTag(RECEIVED_REQUEST_TAG);
        receivedRequestSubActionButton.setOnClickListener(this);

        // set fab layout
        FrameLayout.LayoutParams subActionButtonParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        createEventSubActionButton.setLayoutParams(subActionButtonParams);
        sendRequestSubActionButton.setLayoutParams(subActionButtonParams);
        allEventsSubActionButton.setLayoutParams(subActionButtonParams);
        requestsSubActionButton.setLayoutParams(subActionButtonParams);
        receivedRequestSubActionButton.setLayoutParams(subActionButtonParams);

        FloatingActionMenu circleMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(0) // A whole circle!
                .setEndAngle(360)
                .setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large))
                .addSubActionView(createEventSubActionButton)
                .addSubActionView(sendRequestSubActionButton)
                .addSubActionView(allEventsSubActionButton)
                .addSubActionView(requestsSubActionButton)
                .addSubActionView(receivedRequestSubActionButton)
                .attachTo(centerFAB)
                .build();
    }

    void initializeUserInformation() {
        activeEventCount = 0;
        createdEventCount = 0;
        joinedEventCount = 0;
        userPhotoUri = new String();
        Firebase userInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        userInfoFirebase.keepSynced(true);
        userInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userPhotoUri = dataSnapshot.child("userPhotoUri").getValue().toString();
                activeEventCount = dataSnapshot.child("activeEvent").getChildrenCount();
                if (activeEventCount != 0) {
                    if (dataSnapshot.getValue().toString().equals("created")) {
                        ++createdEventCount;
                    } else if (dataSnapshot.getValue().toString().equals("joined")) {
                        ++joinedEventCount;
                    }
                }
                if ((createdEventCount + joinedEventCount) == activeEventCount) {
                    showInformationDialog();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void showInformationDialog() {
        final Dialog userAccountDialog = new Dialog(this, R.style.event_info_dialog_style);
        userAccountDialog.setContentView(R.layout.dialog_user_account_layout);

        TextView nameEditText = (TextView) userAccountDialog.findViewById(R.id.name_text_view);
        TextView userIdEditText = (TextView) userAccountDialog.findViewById(R.id.user_id_text_view);
        TextView activeEventscountTextView, createdEventsCountTextView, joinedEventsCountTextView;
        TextView statusTextView;
        CircleImageView profileImageView;
        activeEventscountTextView = (TextView) userAccountDialog.findViewById(R.id.active_events_count_text_view);
        createdEventsCountTextView = (TextView) userAccountDialog.findViewById(R.id.created_events_count_text_view);
        joinedEventsCountTextView = (TextView) userAccountDialog.findViewById(R.id.joined_events_count_text_view);
        statusTextView = (TextView) userAccountDialog.findViewById(R.id.status_text_view);
        profileImageView = (CircleImageView) userAccountDialog.findViewById(R.id.profile_image_circle_image_view);

        Picasso.with(this).load(Uri.parse(userPhotoUri)).error(R.drawable.default_profile_image_icon).into(profileImageView);

        SharedPreferences userInfoPreference = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE);
        String name = userInfoPreference.getString("name", null);
        String userId = userInfoPreference.getString("username", null);

        nameEditText.setText(name);
        userIdEditText.setText(userId);


        activeEventscountTextView.setText(String.valueOf(activeEventCount));
        createdEventsCountTextView.setText(String.valueOf(createdEventCount));
        joinedEventsCountTextView.setText(String.valueOf(joinedEventCount));

        statusTextView.setText("available");

        Window window = userAccountDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        userAccountDialog.setCanceledOnTouchOutside(true);
        userAccountDialog.show();
    }

    void setDIconId() {
        Firebase dIconFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/dIcon");
        dIconFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.dIconResourceId = Integer.parseInt(dataSnapshot.getValue().toString());
                toMapsActivity();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void toMapsActivity() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String joinEventId = dialogJoinEventIdEditText.getText().toString();
                final String joinEventDesc = dialogJoinDescEditText.getText().toString();

                // check for empty condition; send request calling sendRequest(String, String)
                if (joinEventId.isEmpty() || joinEventDesc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "enter the details", Toast.LENGTH_SHORT).show();
                } else {
                    otherUserEventCheck(joinEventId, joinEventDesc);

                    dialog.dismiss();
                }
            }
        });
        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    void otherUserEventCheck(final String requestEventId, final String requestEventDesc) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "/activeEvent");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(requestEventId).exists()) {
                    String message = "You are already a part of the event";
                    showRequestActionSnackBar(message, false);
                } else {
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
                    requestEvent = new HashMap<String, Object>();
                    requestEvent.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), requestEventDesc);
                    firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + requestEventId + "/requested");
                    firebase.updateChildren(requestEvent, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
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

    // puts another event request into the event's requested database
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
        snackbar.setActionTextColor(Color.parseColor("#F7BF8E"));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        finish();
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

}
