package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfoRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.EventMembersRecyclerViewAdapater;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EventInfoActivity extends FragmentActivity {

    String username;
    Firebase firebase;


    PlacePicker.IntentBuilder builder;
    public static final int PLACE_PICKER_REQUEST = 1; // used for the place picker intent


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

    List<String> eventMemberList;

    boolean editDestinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        initInfo();
    }

    void initInfo() {
        editDestinationLocation = false;
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
                initEventInfo();
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
                        showDestinationLocationChangeDialog(place.getLatLng());

                }
            }
        }
    void showDestinationLocationChangeDialog(final LatLng latLng) {
        final Dialog destinationLocationChangeDialog = new Dialog(this, R.style.event_info_dialog_style);
        destinationLocationChangeDialog.setContentView(R.layout.dialog_edit_destination_location_layout);
        final TextView editDestinationEditText = (TextView) destinationLocationChangeDialog.findViewById(R.id.edit_destination_edit_text);
        ImageButton cancelEditDestinationImageButton = (ImageButton) destinationLocationChangeDialog.findViewById(R.id.cancel_edit_destination_image_button);
        ImageButton confirmEditDestinationImageButton = (ImageButton) destinationLocationChangeDialog.findViewById(R.id.confirm_edit_destination_button);

        cancelEditDestinationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationLocationChangeDialog.dismiss();
                initEventInfo();
            }
        });
        confirmEditDestinationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInternetConnection()) {
                    final String destinationDesc = editDestinationEditText.getText().toString();
                    if (!(destinationDesc == null || destinationDesc.isEmpty())) {
                        Firebase editDestinationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/info");
                        editDestinationFirebase.keepSynced(true);
                        Map<String, Object> editLocationMap = new HashMap<String, Object>();
                        editLocationMap.put("dLocation", String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
                        editLocationMap.put("dLocationDesc", destinationDesc);
                        editDestinationFirebase.updateChildren(editLocationMap, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                destinationLocationChangeDialog.dismiss();
                                initInfo();
                            }
                        });
                    } else {
                        Toast.makeText(EventInfoActivity.this, "enter a destination title", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EventInfoActivity.this, "no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Window window = destinationLocationChangeDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        destinationLocationChangeDialog.setCanceledOnTouchOutside(true);
        destinationLocationChangeDialog.show();

    }
    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


    void initEventInfo(){
        
            TextView eventIdDialogTextView;

            TextView destLocationDialogTextView;
            TextView eventDescriptionTextView;
            TextView timeStampTextView;
            TextView titleTextView;
            RecyclerView eventInfoMembersRecyclerView;
            ImageButton editMembersImageButton;
            Button editDestinationLocationButton;

        
            eventIdDialogTextView = (TextView) findViewById(R.id.event_id_info_text_view);

            destLocationDialogTextView = (TextView) findViewById(R.id.dest_location_desc_text_view);
            eventDescriptionTextView = (TextView) findViewById(R.id.event_desc_text_view);
            timeStampTextView = (TextView) findViewById(R.id.time_stamp_text_view);
            titleTextView = (TextView) findViewById(R.id.event_title_text_view);

            editDestinationLocationButton = (Button) findViewById(R.id.edit_destination_location_button);


            editMembersImageButton = (ImageButton) findViewById(R.id.editMembersImageButton);
            if (!Constants.eventAdmin) {
                editMembersImageButton.setVisibility(View.GONE);
                editDestinationLocationButton.setVisibility(View.GONE);
            }
            eventInfoMembersRecyclerView = (RecyclerView) findViewById(R.id.members_recycler_view);
            eventInfoMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            titleTextView.setText(eventTitle);
            eventIdDialogTextView.setText(Constants.currentEventId);


            destLocationDialogTextView.setText(destLocationTextView);
            eventDescriptionTextView.setText(eventDescription);
            timeStampTextView.setText(timeStamp);

            eventInfoMembersRecyclerView.setHasFixedSize(true);
            EventInfoRecyclerViewAdapter adapter = new EventInfoRecyclerViewAdapter(this, membersList, memberCoordinate, memberProfileImageUrls, memberProfileName, lastSeenInfo);
            eventInfoMembersRecyclerView.setAdapter(adapter);

            editDestinationLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                         placePickerDialog();
                    } else {
                        Toast.makeText(EventInfoActivity.this, "Turn on GPS", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            editMembersImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //    new EventDialogs().showDialog(MapsActivity.this, Constants.TYPE_DELETE_MEMBERS);
                    showMembersDialog();
                }
            });
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

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                initInfo();
            }
        });
        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EventInfoActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
