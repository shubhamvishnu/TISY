package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventDialogs;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    public static final String MAIN_ACITIVITY_TAG = "MainActivity";
    public final static String CREATE_EVENT_TAG = "create_event";
    public final static String JOIN_EVENT_TAG = "send_requests";
    public final static String ALL_EVENTS_TAG = "all_events";
    public final static String REQUESTS_TAG = "requests";


    Intent intent;
    // make sure eventListener is not null
    int count = 0;
    // Firebase Object reference
    Firebase firebase;
    CoordinatorLayout coordinatorLayoutMainActivity;
    //    Button createEventButton;
//    Button joinEventButton;
//    Button allActiveEventsButton;
//    Button allRequestsButton;
    CircleImageView centerFAB;

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

//        createEventButton = (Button) findViewById(R.id.create_event_button);
//        joinEventButton = (Button) findViewById(R.id.join_event_button);
//        allActiveEventsButton = (Button) findViewById(R.id.all_active_events_button);
//        allRequestsButton = (Button) findViewById(R.id.all_requests_button);
//
//
//        allActiveEventsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_EVENTS);
//            }
//        });
//        allRequestsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new EventDialogs().showDialog(MainActivity.this, Constants.TYPE_ALL_REQUESTS);
//            }
//        });
//        joinEventButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        joinEventButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        joinEventButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
//                        break;
//                }
//                return false;
//            }
//        });
//        joinEventButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                joinEventDialog();
//            }
//        });
//        createEventButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        createEventButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        createEventButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
//                        break;
//                }
//                return false;
//            }
//        });
//        createEventButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopService(new Intent(getBaseContext(), InternetConnectionService.class));
//
//                intent = new Intent(MainActivity.this, CreateEvent.class);
//                startActivity(intent);
//
//            }
//        });
        centerFAB = (CircleImageView) findViewById(R.id.center_fab);
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.floating_sub_action_button_selector));

        CircleImageView createEventCircleButton = new CircleImageView(this);
        createEventCircleButton.setImageResource(R.drawable.add_icon);

        SubActionButton createEventSubActionButton = itemBuilder.setContentView(createEventCircleButton).build();
        createEventSubActionButton.setTag(CREATE_EVENT_TAG);
        createEventSubActionButton.setOnClickListener(this);

        CircleImageView sendRequestCircleButton = new CircleImageView(this);
        sendRequestCircleButton.setImageResource(R.drawable.join_event_icon);

        SubActionButton sendRequestSubActionButton = itemBuilder.setContentView(sendRequestCircleButton).build();
        sendRequestSubActionButton.setTag(JOIN_EVENT_TAG);
        sendRequestSubActionButton.setOnClickListener(this);

        CircleImageView allActiveEventsCircleButton = new CircleImageView(this);
        allActiveEventsCircleButton.setImageResource(R.drawable.all_events_active_icon);

        SubActionButton allEventsSubActionButton = itemBuilder.setContentView(allActiveEventsCircleButton).build();
        allEventsSubActionButton.setTag(ALL_EVENTS_TAG);
        allEventsSubActionButton.setOnClickListener(this);

        CircleImageView requestsCircleButton = new CircleImageView(this);
        requestsCircleButton.setImageResource(R.drawable.requests_icon);

        SubActionButton requestsSubActionButton = itemBuilder.setContentView(requestsCircleButton).build();
        requestsSubActionButton.setTag(REQUESTS_TAG);
        requestsSubActionButton.setOnClickListener(this);

        FloatingActionMenu circleMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(0) // A whole circle!
                .setEndAngle(360)
                .setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large))
                .addSubActionView(createEventSubActionButton)
                .addSubActionView(sendRequestSubActionButton)
                .addSubActionView(allEventsSubActionButton)
                .addSubActionView(requestsSubActionButton)
                .attachTo(centerFAB)
                .build();
    }


    /*
        Constants.currentEventId
        sets the currently ongoing event id, so that it can be accessed throughtout the application
     */
    void setEventId() {
       /* firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "/activeEvent");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.currentEventId = dataSnapshot.getValue().toString();
                setAdminState(Constants.currentEventId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });*/
    }

    void setAdminState(String id) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + id + "/admin");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null).equals(dataSnapshot.getValue())) {
                    Constants.eventAdmin = true;
                } else {
                    Constants.eventAdmin = false;
                }
                setDIconId();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
    }

}
