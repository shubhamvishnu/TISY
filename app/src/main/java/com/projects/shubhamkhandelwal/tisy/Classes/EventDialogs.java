package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventDialogs {
    List<String> members;
    String username;

    public void showDialog(Context context, int type) {
        final Dialog dialog = new Dialog(context, R.style.event_dialogs);
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        if (type == Constants.TYPE_ALL_EVENTS) {
            dialog.setContentView(R.layout.activity_all_events_actvity);
            showAllEventsDialog(context, dialog);
        } else if (type == Constants.TYPE_SENT_REQUESTS) {
            dialog.setContentView(R.layout.dialog_requests_layout);

        } else if (type == Constants.TYPE_DELETE_MEMBERS) {
            dialog.setContentView(R.layout.dialog_delete_event_members_layout);
            showMembersDialog(context, dialog);
        } else if (type == Constants.TYPE_RECEIVED_REQUESTS) {
            dialog.setContentView(R.layout.dialog_received_requests_layout);
            showReceivedRequestDialog(context, dialog);
        }

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);


    }

    void showMembersDialog(final Context context, final Dialog dialog) {
        members = new ArrayList<>();
        Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!Objects.equals(snapshot.getKey(), username)) {
                            members.add(snapshot.getKey());
                        }
                    }
                    initDeleteEventMemberRecyclerView(context, members, dialog);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    void initDeleteEventMemberRecyclerView(final Context context, List<String> members, final Dialog dialog) {
        RecyclerView deleteEventMemberRecyclerView;
        EventMembersRecyclerViewAdapater eventMembersRecyclerViewAdapater;

        deleteEventMemberRecyclerView = (RecyclerView) dialog.findViewById(R.id.delete_event_members_recycler_view);
        deleteEventMemberRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        deleteEventMemberRecyclerView.setLayoutManager(linearLayoutManager);

        eventMembersRecyclerViewAdapater = new EventMembersRecyclerViewAdapater(context, members);
        deleteEventMemberRecyclerView.setAdapter(eventMembersRecyclerViewAdapater);
    }

    void showReceivedRequestDialog(final Context context, final Dialog dialog) {
        // received requests

        final RecyclerView receivedRequestRecyclerView = (RecyclerView) dialog.findViewById(R.id.dialog_received_requests_recycler_view);
        final TextView invitesTextView = (TextView) dialog.findViewById(R.id.inivites_text_view);
        final LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.no_invites_layout);
        Firebase checkInvites = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + username);
        checkInvites.keepSynced(true);
        checkInvites.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    linearLayout.setVisibility(View.INVISIBLE);
                    invitesTextView.setVisibility(View.VISIBLE);
                    receivedRequestRecyclerView.setVisibility(View.VISIBLE);

                    ReceivedRequestsRecyclerViewAdapter receivedRequestsRecyclerViewAdapter;

                    receivedRequestRecyclerView.setHasFixedSize(true);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
                    receivedRequestRecyclerView.setLayoutManager(linearLayoutManager);

                    receivedRequestsRecyclerViewAdapter = new ReceivedRequestsRecyclerViewAdapter(context);
                    receivedRequestRecyclerView.setAdapter(receivedRequestsRecyclerViewAdapter);
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                    invitesTextView.setVisibility(View.INVISIBLE);
                    receivedRequestRecyclerView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });



    }


    void showRequests(final Context context, final Dialog dialog) {


//        // received requests
//
//        RecyclerView receivedRequestRecyclerView;
//        ReceivedRequestsRecyclerViewAdapter receivedRequestsRecyclerViewAdapter;
//
//        receivedRequestRecyclerView = (RecyclerView) dialog.findViewById(R.id.dialog_received_requests_recycler_view);
//        receivedRequestRecyclerView.setHasFixedSize(true);
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
//        receivedRequestRecyclerView.setLayoutManager(linearLayoutManager);
//
//        receivedRequestsRecyclerViewAdapter = new ReceivedRequestsRecyclerViewAdapter(context);
//        receivedRequestRecyclerView.setAdapter(receivedRequestsRecyclerViewAdapter);

        // sent requests


    }



    /*
    void showReceviedRequests(Context context, final Dialog dialog){
        ImageButton exitButton;
        RecyclerView receivedRequestRecyclerView;

        ReceivedRequestsRecyclerViewAdapter receivedRequestsRecyclerViewAdapter;
        exitButton = (ImageButton) dialog.findViewById(R.id.received_requests_back_arrow_image_button);
        receivedRequestRecyclerView= (RecyclerView) dialog.findViewById(R.id.dialog_received_request_recycler_view);
        receivedRequestRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        receivedRequestRecyclerView.setLayoutManager(linearLayoutManager);

        receivedRequestsRecyclerViewAdapter = new ReceivedRequestsRecyclerViewAdapter(context);
        receivedRequestRecyclerView.setAdapter(receivedRequestsRecyclerViewAdapter);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }*/

    void showAllEventsDialog(Context context, final Dialog dialog) {
        ImageButton exitImageButton;
        RecyclerView activeEventsRecyclerView;
        ActiveEventsRecyclerViewAdapter activeEventsRecyclerViewAdapter;
        exitImageButton = (ImageButton) dialog.findViewById(R.id.all_events_back_arrow_image_button);
        activeEventsRecyclerView = (RecyclerView) dialog.findViewById(R.id.active_events_recycler_view);
        activeEventsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        activeEventsRecyclerView.setLayoutManager(linearLayoutManager);

        activeEventsRecyclerViewAdapter = new ActiveEventsRecyclerViewAdapter(context);
        activeEventsRecyclerView.setAdapter(activeEventsRecyclerViewAdapter);

        exitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

}