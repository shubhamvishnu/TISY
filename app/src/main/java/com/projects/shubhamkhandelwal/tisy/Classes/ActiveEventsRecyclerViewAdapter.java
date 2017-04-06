package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.MainActivity;
import com.projects.shubhamkhandelwal.tisy.MapsActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shubham Khandelwal on 9/22/2016.
 */
public class ActiveEventsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static int VIEW_TYPE_ACTIVE_EVENT = 1;
    public static int VIEW_TYPE_RECEIVED_REQUESTS = 2;
    List<ActiveEventInfo> activeEventIds = new ArrayList<>();
    List<ReceivedRequests> receviedRequestsList;
    Firebase firebase;
    Context context;
    String username;
    ProgressDialog progressDialog;
    private LayoutInflater inflator;

    public ActiveEventsRecyclerViewAdapter(android.content.Context context) {
        this.context = context;
        receviedRequestsList = new ArrayList<>();
        inflator = LayoutInflater.from(context);
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        initProgressDialog();
        loadAllActiveEvents();

    }

    void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("making changes...");
        progressDialog.setMessage("Working on it!");
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

    void loadAllActiveEvents() {
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
        firebase.keepSynced(true);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getValue().toString().equals("false")) {
                    final String activeEventId = dataSnapshot.getKey();
                    final String association = dataSnapshot.getValue().toString();
                    Firebase eventInfo = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + activeEventId);
                    eventInfo.keepSynced(true);
                    eventInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int position = activeEventIds.size();
                            final String title = dataSnapshot.child("title").getValue().toString();
                            final long numberOfRequests = dataSnapshot.child("requested").getChildrenCount();
                            if (numberOfRequests > 0) {
                                for (DataSnapshot childrenSnapshot : dataSnapshot.child("requested").getChildren()) {
                                    ReceivedRequests requestsDetails = new ReceivedRequests(childrenSnapshot.getKey(), childrenSnapshot.getValue().toString(), activeEventId);
                                    receviedRequestsList.add(requestsDetails);
                                }
                            }

                            //final String request = String.valueOf(numberOfRequests);
                            final String timeCreated = dataSnapshot.child("time").getValue().toString();
                            // final String memberCount = String.valueOf(dataSnapshot.child("members").getChildrenCount());
                            final List<String> memberList = new ArrayList<String>();
                            for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                                memberList.add(snapshot.getKey());
                            }
                            EventInfo eventInfo = new EventInfo();
                            eventInfo.setdLocation(dataSnapshot.child("info").child("dLocation").getValue().toString());
                            eventInfo.setdLocationDesc(dataSnapshot.child("info").child("dLocationDesc").getValue().toString());
                            ActiveEventInfo activeEventInfo = new ActiveEventInfo(title, association, activeEventId, eventInfo, timeCreated, memberList);

                            activeEventInfo.setAdmin(dataSnapshot.child("admin").getValue().toString());
                            activeEventInfo.setdIconResourceId(Integer.parseInt(dataSnapshot.child("dIcon").getValue().toString()));
                            activeEventIds.add(activeEventInfo);
                            notifyItemInserted(position);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

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

    @Override
    public int getItemViewType(int position) {
        if (position < receviedRequestsList.size()) {
            return VIEW_TYPE_RECEIVED_REQUESTS;
        } else {
            return VIEW_TYPE_ACTIVE_EVENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ACTIVE_EVENT) {
            View view = inflator.inflate(R.layout.recycler_view_all_active_events_row_layout, parent, false);
            ActiveEventsRecyclerViewHolder viewHolder = new ActiveEventsRecyclerViewHolder(view);
            return viewHolder;
        } else {
            View view = inflator.inflate(R.layout.recycler_view_received_requests_recycler_view_row_layout, parent, false);
            ActiveEventReceivedRequestRecyclerViewHolder viewHolder = new ActiveEventReceivedRequestRecyclerViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_ACTIVE_EVENT) {
            int finalPosition = position - receviedRequestsList.size();

            SpannableString content = new SpannableString(activeEventIds.get(finalPosition).getTitle());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

            ((ActiveEventsRecyclerViewHolder) holder).activeEventTitleTextView.setText(content);
            ((ActiveEventsRecyclerViewHolder) holder).activeEventAssociation.setText(activeEventIds.get(finalPosition).getAssociation());
            ((ActiveEventsRecyclerViewHolder) holder).activeEventIdTextView.setText(activeEventIds.get(finalPosition).getEventId());

            final EventInfo info = activeEventIds.get(finalPosition).getEventInfo();


            //((ActiveEventsRecyclerViewHolder) holder).activeEventdLocationDesc.setText(info.getdLocationDesc());
            //((ActiveEventsRecyclerViewHolder) holder).activeEventRequests.setText(activeEventIds.get(finalPosition).getRequests());
            //((ActiveEventsRecyclerViewHolder) holder).activeEventTimeCreated.setText(activeEventIds.get(finalPosition).getTimeCreated());

            ((ActiveEventsRecyclerViewHolder) holder).activeMembersRecyclerViewAdapter = new ActiveMembersRecyclerViewAdapter(context, activeEventIds.get(finalPosition).getMemberList());
            ((ActiveEventsRecyclerViewHolder) holder).activeEventMemberRecyclerView.setAdapter(((ActiveEventsRecyclerViewHolder) holder).activeMembersRecyclerViewAdapter);

//        String[] destCoordinates = info.getdLocation().split(",");
//        String[] startCoordinates = info.getsLocation().split(",");

            Picasso.with(context).load(Uri.parse("https://maps.googleapis.com/maps/api/staticmap?location=" + info.getdLocation() + "&size=600x600&maptype=roadmap&markers=color:blue%7Clabel:D%7C" + info.getdLocation() + "&key=AIzaSyDHngp3Jx-K8YZYSCNfdljE2gy5p8gcYQQ")).networkPolicy(NetworkPolicy.OFFLINE).into(((ActiveEventsRecyclerViewHolder) holder).activeEventCardImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(context).load(Uri.parse("https://maps.googleapis.com/maps/api/staticmap?location=" + info.getdLocation() + "&size=600x600&maptype=roadmap&markers=color:blue%7Clabel:D%7C" + info.getdLocation() + "&key=AIzaSyDHngp3Jx-K8YZYSCNfdljE2gy5p8gcYQQ")).error(R.drawable.chennai_anna_nagar_static_map).into(((ActiveEventsRecyclerViewHolder) holder).activeEventCardImageView);
                }
            });
        } else {
            ((ActiveEventReceivedRequestRecyclerViewHolder) holder).userIdTextView.setText(receviedRequestsList.get(position).getUsername());
            ((ActiveEventReceivedRequestRecyclerViewHolder) holder).descTextView.setText(receviedRequestsList.get(position).getDescription());
            ((ActiveEventReceivedRequestRecyclerViewHolder) holder).adminEventID.setText(receviedRequestsList.get(position).getEventID());


        }
    }
    void deleteRequest(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS +receviedRequestsList.get(position).getEventID() + "/requested/" + receviedRequestsList.get(position).getUsername());
        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + receviedRequestsList.get(position).getUsername() + "/" + receviedRequestsList.get(position).getEventID());
                firebase.removeValue(new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        receviedRequestsList.remove(position);
                        notifyItemRemoved(position);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });

            }
        });
    }

    void addMember(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + receviedRequestsList.get(position).getEventID() + "/requested/" + receviedRequestsList.get(position).getUsername());
        firebase.keepSynced(true);
        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + receviedRequestsList.get(position).getEventID() + "/members/");
                firebase.keepSynced(true);
                final Map<String, Object> updateMember = new HashMap<String, Object>();
                updateMember.put(receviedRequestsList.get(position).getUsername(), "0.0,0.0");
                firebase.updateChildren(updateMember, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + receviedRequestsList.get(position).getUsername() + "/" + receviedRequestsList.get(position).getEventID());
                        firebase.keepSynced(true);
                        firebase.removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + receviedRequestsList.get(position).getUsername() + "/activeEvent");
                                Map<String, Object> updateUserDetails = new HashMap<>();
                                updateUserDetails.put(receviedRequestsList.get(position).getEventID(), "joined");
                                firebase.keepSynced(true);
                                firebase.updateChildren(updateUserDetails, new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        receviedRequestsList.remove(position);
                                        notifyItemRemoved(position);
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });
    }



    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    @Override
    public int getItemCount() {
        return (activeEventIds.size() + receviedRequestsList.size());
    }

    class ActiveEventReceivedRequestRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //CircleImageView circleImageView;
        TextView userIdTextView, descTextView, adminEventID;
        ImageButton acceptRequestImageButton, rejectRequestImageButton;
        View view;

        public ActiveEventReceivedRequestRecyclerViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            //circleImageView = (CircleImageView) itemView.findViewById(R.id.profile_image_circle_image_button);
            userIdTextView = (TextView) itemView.findViewById(R.id.received_request_username_text_view);
            descTextView = (TextView) itemView.findViewById(R.id.received_request_desc_text_view);
            adminEventID = (TextView) itemView.findViewById(R.id.received_request_for_user_id);

            acceptRequestImageButton = (ImageButton) itemView.findViewById(R.id.add_user_image_button);
            rejectRequestImageButton = (ImageButton) itemView.findViewById(R.id.delete_user_image_button);

            acceptRequestImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkInternetConnection()) {
                        progressDialog.show();

                        addMember(getPosition());
                    }else{
                        Toast.makeText(context, "Oops! no internet connection...", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            rejectRequestImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkInternetConnection()) {
                        progressDialog.show();
                        deleteRequest(getPosition());
                    }else{
                        Toast.makeText(context, "Oops! no internet connection...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {

        }
    }

    class ActiveEventsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView activeEventIdTextView;

        //TextView activeEventdLocationDesc;
        TextView activeEventTitleTextView;
        //TextView activeEventRequests;
        //TextView activeEventTimeCreated;
        TextView activeEventAssociation;

        RecyclerView activeEventMemberRecyclerView;
        ActiveMembersRecyclerViewAdapter activeMembersRecyclerViewAdapter;
        ImageView activeEventCardImageView;


        View view;

        public ActiveEventsRecyclerViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            activeEventIdTextView = (TextView) itemView.findViewById(R.id.active_event_id_text_view);

            //activeEventdLocationDesc = (TextView) itemView.findViewById(R.id.active_event_dLocationDesc);
            activeEventTitleTextView = (TextView) itemView.findViewById(R.id.active_event_title_text_view);
           //activeEventRequests = (TextView) itemView.findViewById(R.id.active_event_requests);
            //activeEventTimeCreated = (TextView) itemView.findViewById(R.id.active_event_time_created);
            activeEventAssociation = (TextView) itemView.findViewById(R.id.active_event_association_text_view);

            activeEventMemberRecyclerView = (RecyclerView) itemView.findViewById(R.id.active_event_member_recycler_view);
            activeEventCardImageView = (ImageView) itemView.findViewById(R.id.active_event_card_background_image_view);

            activeEventMemberRecyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            activeEventMemberRecyclerView.setLayoutManager(layoutManager);

            activeEventIdTextView.setOnClickListener(this);
           // activeEventdLocationDesc.setOnClickListener(this);
            activeEventTitleTextView.setOnClickListener(this);
            //activeEventRequests.setOnClickListener(this);
            //activeEventTimeCreated.setOnClickListener(this);
            activeEventAssociation.setOnClickListener(this);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ActiveEventInfo activeEventInfo = activeEventIds.get(getPosition()-receviedRequestsList.size());
            Constants.currentEventId = activeEventInfo.getEventId();
            if (activeEventInfo.getAdmin().equals(username)) {
                Constants.eventAdmin = true;
            } else {
                Constants.eventAdmin = false;
            }
            Constants.dIconResourceId = activeEventInfo.getdIconResourceId();
            Intent intent = new Intent(context, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
