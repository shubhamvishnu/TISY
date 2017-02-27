package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 2/27/2017.
 */
public class EventMemberViewRecyclerViewAdapter extends RecyclerView.Adapter<EventMemberViewRecyclerViewAdapter.EventMembersViewRecyclerViewHolder> {
    List<String> memberList;
    List<LatLng> memberCoordinates;
    Context context;
    private LayoutInflater inflator;
    GoogleMap googleMap;
    public EventMemberViewRecyclerViewAdapter(Context context, GoogleMap googleMap) {
        inflator = LayoutInflater.from(context);
        this.context = context;
        memberList = new ArrayList<>();
        memberCoordinates = new ArrayList<>();
        this.googleMap = googleMap;
        init();

    }

    void init() {
        Firebase initMembersFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        initMembersFirebase.keepSynced(true);
        initMembersFirebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int position = memberList.size();

                memberList.add(dataSnapshot.getKey());
                String[] coordinate = dataSnapshot.getValue().toString().split(",");
                LatLng latLng = new LatLng(Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1]));
                memberCoordinates.add(latLng);
                notifyItemInserted(position);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int index = memberList.indexOf(dataSnapshot.getKey());
                memberList.remove(index);
                memberCoordinates.remove(index);
                notifyItemRemoved(index);
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
    public EventMembersViewRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_active_event_members_row_layout, parent, false);
        EventMembersViewRecyclerViewHolder viewHolder = new EventMembersViewRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventMembersViewRecyclerViewHolder holder, int position) {
        holder.memberTextView.setText(memberList.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    class EventMembersViewRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView memberTextView;

        public EventMembersViewRecyclerViewHolder(View itemView) {
            super(itemView);
            memberTextView = (TextView) itemView.findViewById(R.id.active_event_members_text_view);
            memberTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

}
