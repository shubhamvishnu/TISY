package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by Shubham Khandelwal on 10/21/2016.
 */
public class SearchResultsRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultsRecyclerViewAdapter.SearchResultsRecyclerViewHolder> {
    Context context;
    LayoutInflater inflator;
    String name;
    List<String> nameList;
    String username;
    List<String> eventIdList;
    ProgressDialog progressDialog;

    public SearchResultsRecyclerViewAdapter(Context context, String name) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.name = name.trim().replaceAll("\\s{2,}", " ");
        Toast.makeText(context, "name :" + this.name, Toast.LENGTH_SHORT).show();
        eventIdList = new ArrayList<>();
        nameList = new ArrayList<>();
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        populateViewWithResults();
        initProgressDialog();
    }

    void initProgressDialog(){
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

    void populateViewWithResults() {
        nameList = new ArrayList<>();
        eventIdList = new ArrayList<>();
        final List<String> activeEventList = new ArrayList<>();
        Firebase searchResultFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);
        searchResultFirebase.keepSynced(true);
        searchResultFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // retrieve all the active events
                    for (DataSnapshot activeEventSnapshot : dataSnapshot.child(username).child("activeEvent").getChildren()) {
                        activeEventList.add(activeEventSnapshot.getKey());
                    }

                    // traversing through all the user details
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // make sure it is not the same user being searched
                        if(!Objects.equals(snapshot.getKey(), username)){

                            // finds any trace of the searched keyword in any of ther user
                            if (snapshot.getKey().contains(name) || snapshot.child("name").getValue().toString().contains(name) || Pattern.compile(Pattern.quote(snapshot.getKey()), Pattern.CASE_INSENSITIVE).matcher(name).find() || Pattern.compile(Pattern.quote(snapshot.child("name").getValue().toString()), Pattern.CASE_INSENSITIVE).matcher(name).find()) {

                                // checking if the invite hasn't already been sent to the user
                                Firebase sentRequestsFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId + "/");
                                sentRequestsFirebase.keepSynced(true);
                                sentRequestsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        // if the invite hasnt't been sent to the user
                                        if (!dataSnapshot.child(snapshot.getKey()).exists()) {

                                            Firebase userRequestsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + snapshot.getKey());
                                            userRequestsFirebase.keepSynced(true);
                                            userRequestsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    if (!dataSnapshot.child(snapshot.getKey()).exists()) {
                                                        if (!activeEventList.contains(snapshot.getKey())) {

                                                            int position = eventIdList.size();
                                                            eventIdList.add(snapshot.getKey());
                                                            nameList.add(snapshot.child("name").getValue().toString());
                                                            notifyItemInserted(position);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {

                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {

                                    }
                                });

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    @Override
    public SearchResultsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_search_option_check_row_layout, parent, false);
        SearchResultsRecyclerViewHolder viewHolder = new SearchResultsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SearchResultsRecyclerViewHolder holder, int position) {
        holder.nameTextView.setText(nameList.get(position));
        holder.eventIdTextView.setText(eventIdList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventIdList.size();
    }

    void sendRequest(final int position) {

        Firebase userIdCheckFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + eventIdList.get(position));
        userIdCheckFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Firebase sendRequestFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + Constants.currentEventId);
                    HashMap<String, Object> sendRequestUsername = new HashMap<String, Object>();
                    sendRequestUsername.put(eventIdList.get(position), username);
                    sendRequestFirebase.updateChildren(sendRequestUsername, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            Firebase userSentRequestUpdateFirebase = new Firebase(FirebaseReferences.FIREBASE_EVENT_SENT_REQUESTS + eventIdList.get(position));
                            HashMap<String, Object> userSentRequestUpdate = new HashMap<String, Object>();
                            userSentRequestUpdate.put(Constants.currentEventId, username);
                            userSentRequestUpdateFirebase.updateChildren(userSentRequestUpdate, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    eventIdList.remove(position);
                                    nameList.remove(position);
                                    notifyItemRemoved(position);
                                    if(progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    class SearchResultsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        TextView eventIdTextView;
        ImageView addMemberImageView;

        public SearchResultsRecyclerViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.search_option_choice_name_text_view);
            eventIdTextView = (TextView) itemView.findViewById(R.id.search_option_choice_event_id_text_view);
            addMemberImageView = (ImageView) itemView.findViewById(R.id.search_option_add_member_image_view);
            addMemberImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.search_option_add_member_image_view: {
                    int position = getPosition();
                    if (eventIdList.size() >  0 && position >= 0 && position < eventIdList.size()) {
                        progressDialog.show();
                        sendRequest(position);
                    }
                    break;
                }
            }
        }
    }
}