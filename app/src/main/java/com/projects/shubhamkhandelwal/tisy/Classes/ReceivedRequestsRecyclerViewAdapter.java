package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shubham Khandelwal on 10/9/2016.
 */
public class ReceivedRequestsRecyclerViewAdapter extends RecyclerView.Adapter<ReceivedRequestsRecyclerViewAdapter.ReceivedRequestsRecyclerViewHolder> {
    Firebase firebase;
    Context context;
    List<String> eventIdList;
    String username;
    private LayoutInflater inflator;

    public ReceivedRequestsRecyclerViewAdapter(Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        eventIdList = new ArrayList<>();
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        loadRequests();
    }

    void loadRequests() {
        firebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + username);
        firebase.keepSynced(true);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int position = eventIdList.size();
                eventIdList.add(dataSnapshot.getKey());
                notifyItemInserted(position);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int position = eventIdList.indexOf(dataSnapshot.getKey());
                eventIdList.remove(position);
                notifyItemRemoved(position);
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
    public ReceivedRequestsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_received_request_row_layout, parent, false);
        ReceivedRequestsRecyclerViewHolder viewHolder = new ReceivedRequestsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReceivedRequestsRecyclerViewHolder holder, int position) {
        holder.eventIdTextView.setText(eventIdList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventIdList.size();
    }

    void removeRequest(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + eventIdList.get(position) + "/" + username);
        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + username + "/" + eventIdList.get(position));
                firebase.removeValue(new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                    }
                });
            }
        });
    }

    void addUser(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + eventIdList.get(position) + "/members");
        final Map<String, Object> updateMember = new HashMap<String, Object>();
        updateMember.put(username, "0.0,0.0");
        firebase.updateChildren(updateMember, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
                Map<String, Object> updateUserDetails = new HashMap<>();
                updateUserDetails.put(eventIdList.get(position), "joined");
                firebase.updateChildren(updateUserDetails, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        removeRequest(position);

                    }
                });
            }
        });
    }

    public class ReceivedRequestsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView eventIdTextView;
        ImageButton acceptRequestImageButton, declineRequestImageButton;

        public ReceivedRequestsRecyclerViewHolder(View itemView) {
            super(itemView);
            eventIdTextView = (TextView) itemView.findViewById(R.id.event_id_requested_text_view);
            acceptRequestImageButton = (ImageButton) itemView.findViewById(R.id.accept_request_image_button);
            declineRequestImageButton = (ImageButton) itemView.findViewById(R.id.decline_request_image_button);
            acceptRequestImageButton.setOnClickListener(this);
            declineRequestImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.accept_request_image_button: {
                    int position = getPosition();
                    if (position >= 0 && eventIdList.size() > 0) {
                        addUser(position);
                    }
                    break;
                }
                case R.id.decline_request_image_button: {
                    int position = getPosition();
                    if (position >= 0 && eventIdList.size() > 0 && position < eventIdList.size()) {
                        removeRequest(position);
                    }
                    break;
                }
            }
        }
    }
}
