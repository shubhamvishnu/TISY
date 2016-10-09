package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.net.Uri;
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
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shubham Khandelwal on 10/9/2016.
 */
public class SentEventJoinRequestRecyclerViewAdapter extends RecyclerView.Adapter<SentEventJoinRequestRecyclerViewAdapter.SentEventJoinRequestRecyclerViewHolder> {
    Context context;
    String username;
    List<String> sentRequestsList;
    List<String> imageUrlList;
    Firebase firebase;
    Firebase imageUrlFirebase;
    private LayoutInflater inflator;

    public SentEventJoinRequestRecyclerViewAdapter(android.content.Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        sentRequestsList = new ArrayList<>();
        imageUrlList = new ArrayList<>();
        loadRequestsSent();
    }

    void loadRequestsSent() {
        firebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId);
        firebase.keepSynced(true);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = dataSnapshot.getKey();
                sentRequestsList.add(userId);
                loadImage(userId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int position = sentRequestsList.indexOf(dataSnapshot.getKey());
                sentRequestsList.remove(position);
                imageUrlList.remove(position);
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

    void loadImage(String username) {
        imageUrlFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/userPhotoUri");
        imageUrlFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int position = imageUrlList.size();
                    imageUrlList.add(dataSnapshot.getValue().toString());
                    notifyItemInserted(position);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public SentEventJoinRequestRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_sent_event_join_request_row_layout, parent, false);
        SentEventJoinRequestRecyclerViewHolder viewHolder = new SentEventJoinRequestRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SentEventJoinRequestRecyclerViewHolder holder, int position) {
        holder.usernameTitleTextView.setText(sentRequestsList.get(position));
        Picasso.with(context).load(Uri.parse(imageUrlList.get(position))).error(R.drawable.start_location_icon).into(holder.userRequestProfileImageButton);
    }

    @Override
    public int getItemCount() {
        if (sentRequestsList.size() == imageUrlList.size()) {
            return sentRequestsList.size();
        }
        return 0;
    }

    void removeRequest(final int position) {
        Firebase removeRequest = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId +"/"+ sentRequestsList.get(position));
        removeRequest.removeValue();
    }


    public class SentEventJoinRequestRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView usernameTitleTextView;
        CircleImageView userRequestProfileImageButton;
        ImageButton removeSentRequestImageButton;

        public SentEventJoinRequestRecyclerViewHolder(View itemView) {
            super(itemView);
            usernameTitleTextView = (TextView) itemView.findViewById(R.id.username_title_text_view);
            userRequestProfileImageButton = (CircleImageView) itemView.findViewById(R.id.user_request_profile_image_button);
            removeSentRequestImageButton = (ImageButton) itemView.findViewById(R.id.remove_sent_request_image_button);
            removeSentRequestImageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.remove_sent_request_image_button: {
                    removeRequest(getPosition());
                    break;
                }
            }
        }
    }
}
