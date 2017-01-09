package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shubham Khandelwal on 8/15/2016.
 */
public class RequestsRecyclerAdapter extends RecyclerView.Adapter<RequestsRecyclerAdapter.RequestsRecyclerViewHolder> {
    List<RequestsDetails> requestDetails = new ArrayList<>();
    List<String> profileURLsList = new ArrayList<>();
    Firebase firebase;
    Context context;
    private LayoutInflater inflator;

    public RequestsRecyclerAdapter(android.content.Context context, List<RequestsDetails> requestDetails) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.requestDetails = requestDetails;
        fetchProfileURLS();
    }

    void fetchProfileURLS() {
        profileURLsList = new ArrayList<>();
        for (int i = 0; i < requestDetails.size(); i++) {
            final Firebase profileFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + requestDetails.get(i).getUsername() + "/userPhotoUri/");
            profileFirebase.keepSynced(true);
            profileFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    profileURLsList.add(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public RequestsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_requests_row_layout, parent, false);
        RequestsRecyclerViewHolder viewHolder = new RequestsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RequestsRecyclerViewHolder holder, final int position) {
        holder.usernameTextView.setText(requestDetails.get(position).getUsername());
        holder.descriptionTextView.setText(requestDetails.get(position).getDescription());
        Picasso.with(context).load(Uri.parse(profileURLsList.get(position))).error(R.drawable.default_profile_image_icon).into(holder.profileImageCircleImageView);
    }

    @Override
    public int getItemCount() {
        return profileURLsList.size();
    }


    void deleteRequest(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/requested/" + requestDetails.get(position).getUsername());
        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + requestDetails.get(position).getUsername() + "/" + Constants.currentEventId);
                firebase.removeValue(new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        requestDetails.remove(position);
                        profileURLsList.remove(position);
                        notifyItemRemoved(position);

                    }
                });

            }
        });
    }

    void addMember(final int position) {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/requested/" + requestDetails.get(position).getUsername());
        firebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members/");
                final Map<String, Object> updateMember = new HashMap<String, Object>();
                updateMember.put(requestDetails.get(position).getUsername(), "0.0,0.0");
                firebase.updateChildren(updateMember, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + requestDetails.get(position).getUsername() + "/" + Constants.currentEventId);
                        firebase.removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + requestDetails.get(position).getUsername() + "/activeEvent");
                                Map<String, Object> updateUserDetails = new HashMap<>();
                                updateUserDetails.put(Constants.currentEventId, "joined");
                                firebase.updateChildren(updateUserDetails, new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        requestDetails.remove(position);
                                        profileURLsList.remove(position);
                                        notifyItemRemoved(position);
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });
    }

    class RequestsRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView descriptionTextView;
        Button addUserButton;
        Button deleteUserButton;
        CircleImageView profileImageCircleImageView;

        public RequestsRecyclerViewHolder(View itemView) {
            super(itemView);
            usernameTextView = (TextView) itemView.findViewById(R.id.username_recycler_view_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_recycler_view_text_view);
            addUserButton = (Button) itemView.findViewById(R.id.add_user_recycler_view_button);
            deleteUserButton = (Button) itemView.findViewById(R.id.delete_user_recycler_view_button);
            profileImageCircleImageView = (CircleImageView) itemView.findViewById(R.id.profile_image_recycler_circle_image_view);
            deleteUserButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            deleteUserButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                            break;
                        case MotionEvent.ACTION_UP:
                            deleteUserButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
                            break;
                    }
                    return false;
                }
            });
            addUserButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            addUserButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                            break;
                        case MotionEvent.ACTION_UP:
                            addUserButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
                            break;
                    }
                    return false;
                }
            });
            addUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getPosition();
                    if (position >= 0 && requestDetails.size() > 0 && position < requestDetails.size()) {
                        addMember(position);
                    }
                }
            });
            deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getPosition();
                    if (position >= 0 && requestDetails.size() > 0 && position < requestDetails.size()) {
                        deleteRequest(position);
                    }
                }
            });
        }
    }
}
