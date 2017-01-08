package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.MapsActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 9/22/2016.
 */
public class ActiveEventsRecyclerViewAdapter extends RecyclerView.Adapter<ActiveEventsRecyclerViewAdapter.ActiveEventsRecyclerViewHolder> {
    List<ActiveEventInfo> activeEventIds = new ArrayList<>();

    Firebase firebase;
    Context context;
    String username;
    private LayoutInflater inflator;

    public ActiveEventsRecyclerViewAdapter(android.content.Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        loadAllActiveEvents();
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
                    eventInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int position = activeEventIds.size();
                            final String title = dataSnapshot.child("title").getValue().toString();
                            final long numberOfRequests = dataSnapshot.child("requested").getChildrenCount();
                            final String request = String.valueOf(numberOfRequests);
                            final String timeCreated = dataSnapshot.child("time").getValue().toString();
                            // final String memberCount = String.valueOf(dataSnapshot.child("members").getChildrenCount());
                            final List<String> memberList = new ArrayList<String>();
                            for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                                memberList.add(snapshot.getKey());
                            }
                            EventInfo eventInfo = new EventInfo();
                            eventInfo.setsLocation(dataSnapshot.child("info").child("sLocation").getValue().toString());
                            eventInfo.setsLocationDesc(dataSnapshot.child("info").child("sLocationDesc").getValue().toString());
                            eventInfo.setdLocation(dataSnapshot.child("info").child("dLocation").getValue().toString());
                            eventInfo.setdLocationDesc(dataSnapshot.child("info").child("dLocationDesc").getValue().toString());
                            ActiveEventInfo activeEventInfo = new ActiveEventInfo(title, association, activeEventId, eventInfo, request, timeCreated, memberList);

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
    public ActiveEventsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_all_active_events_row_layout, parent, false);
        ActiveEventsRecyclerViewHolder viewHolder = new ActiveEventsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActiveEventsRecyclerViewHolder holder, int position) {
        holder.activeEventTitleTextView.setText(activeEventIds.get(position).getTitle());
        holder.activeEventAssociation.setText(activeEventIds.get(position).getAssociation());
        holder.activeEventIdTextView.setText(activeEventIds.get(position).getEventId());

        EventInfo info = activeEventIds.get(position).getEventInfo();

        holder.activeEventsLocationDesc.setText(info.getsLocationDesc());
        holder.activeEventdLocationDesc.setText(info.getdLocationDesc());
        holder.activeEventRequests.setText(activeEventIds.get(position).getRequests());
        holder.activeEventTimeCreated.setText(activeEventIds.get(position).getTimeCreated());


        holder.activeMembersRecyclerViewAdapter = new ActiveMembersRecyclerViewAdapter(context, activeEventIds.get(position).getMemberList());
        holder.activeEventMemberRecyclerView.setAdapter(holder.activeMembersRecyclerViewAdapter);
    }

    @Override
    public int getItemCount() {
        return activeEventIds.size();
    }

    class ActiveEventsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView activeEventIdTextView;
        TextView activeEventsLocationDesc;
        TextView activeEventdLocationDesc;
        TextView activeEventTitleTextView;
        TextView activeEventRequests;
        TextView activeEventTimeCreated;
        TextView activeEventAssociation;
        RecyclerView activeEventMemberRecyclerView;
        ActiveMembersRecyclerViewAdapter activeMembersRecyclerViewAdapter;

        View view;

        public ActiveEventsRecyclerViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            activeEventIdTextView = (TextView) itemView.findViewById(R.id.active_event_id_text_view);
            activeEventsLocationDesc = (TextView) itemView.findViewById(R.id.active_event_sLocationDesc);
            activeEventdLocationDesc = (TextView) itemView.findViewById(R.id.active_event_dLocationDesc);
            activeEventTitleTextView = (TextView) itemView.findViewById(R.id.active_event_title_text_view);
            activeEventRequests = (TextView) itemView.findViewById(R.id.active_event_requests);
            activeEventTimeCreated = (TextView) itemView.findViewById(R.id.active_event_time_created);
            activeEventAssociation = (TextView) itemView.findViewById(R.id.active_event_association_text_view);

            activeEventMemberRecyclerView = (RecyclerView) itemView.findViewById(R.id.active_event_member_recycler_view);
            activeEventMemberRecyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            activeEventMemberRecyclerView.setLayoutManager(layoutManager);

            activeEventIdTextView.setOnClickListener(this);
            activeEventsLocationDesc.setOnClickListener(this);
            activeEventdLocationDesc.setOnClickListener(this);
            activeEventTitleTextView.setOnClickListener(this);
            activeEventRequests.setOnClickListener(this);
            activeEventTimeCreated.setOnClickListener(this);
            activeEventAssociation.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ActiveEventInfo activeEventInfo = activeEventIds.get(getPosition());
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
