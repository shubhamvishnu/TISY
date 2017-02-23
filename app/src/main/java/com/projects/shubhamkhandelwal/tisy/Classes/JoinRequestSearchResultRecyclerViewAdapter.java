package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Map;
import java.util.Objects;

/**
 * Created by Shubham Khandelwal on 12/1/2016.
 */
public class JoinRequestSearchResultRecyclerViewAdapter extends RecyclerView.Adapter<JoinRequestSearchResultRecyclerViewAdapter.SearchResultsRecyclerViewHolder> {
    Context context;
    LayoutInflater inflator;
    String name;
    List<String> nameList;
    String username;
    List<String> eventIdList;
    Firebase firebase;

    public JoinRequestSearchResultRecyclerViewAdapter(Context context, String name) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.name = name.trim().replaceAll("\\s{2,}", " ");
        Toast.makeText(context, "name :" + this.name, Toast.LENGTH_SHORT).show();
        eventIdList = new ArrayList<>();
        nameList = new ArrayList<>();
        firebase = null;
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        populateViewWithResults();
    }

    void populateViewWithResults() {
        nameList = new ArrayList<>();
        eventIdList = new ArrayList<>();
        final List<String> activeEventList = new ArrayList<>();
        Firebase searchResultFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);

        final Firebase requestsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + username);
        requestsFirebase.keepSynced(true);

        searchResultFirebase.keepSynced(true);
        searchResultFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // retrieving active events
                    for (DataSnapshot activeEventSnapshot : dataSnapshot.child(username).child("activeEvent").getChildren()) {
                        activeEventList.add(activeEventSnapshot.getKey());
                    }

                    // traversing through all the users
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // check if the name is not equal to username
                        if (!Objects.equals(snapshot.getKey(), username)) {

                            // check if the name has activeEvents
                            if (snapshot.child("activeEvent").hasChildren()) {

                                // iterate through the users events
                                for (final DataSnapshot eventSnapshot : snapshot.child("activeEvent").getChildren()) {

                                    // check if eventID is equal to the name searched
                                    if (eventSnapshot.getKey().contains(name) && Objects.equals(eventSnapshot.getValue().toString(), "created")) {

                                        requestsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    int count = 0;

                                                    // checking if the invite from that event isn't received
                                                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                                        if (Objects.equals(eventSnapshot.getKey(), snapshot1.getKey())) {
                                                            count++;
                                                        }
                                                    }
                                                    if (count == 0 && !activeEventList.contains(eventSnapshot.getKey())) {
                                                        int position = eventIdList.size();
                                                        eventIdList.add(eventSnapshot.getKey());
                                                        nameList.add(snapshot.child("name").getValue().toString());
                                                        notifyItemInserted(position);
                                                    }
                                                } else {
                                                    if (!activeEventList.contains(eventSnapshot.getKey())) {
                                                        int position = eventIdList.size();
                                                        eventIdList.add(eventSnapshot.getKey());
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

    void sendRequest(final String eventID) {
        final Dialog dialog = new Dialog(context, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_join_event_layout);
        TextView joinRequestIDTextView = (TextView) dialog.findViewById(R.id.join_request_event_id);
        final EditText joinRequestDescEditText = (EditText) dialog.findViewById(R.id.join_request_desc_edit_text);
        Button joinRequestSendButton = (Button) dialog.findViewById(R.id.join_request_send_button);
        joinRequestIDTextView.setText(eventID);
        joinRequestSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String desc = joinRequestDescEditText.getText().toString();
                if (!desc.isEmpty()) {
                    dialog.dismiss();
                    sendJoinRequest(eventID, desc);
                } else {
                    Toast.makeText(context, "Write Something!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }

    // sends the request to join an event
    void sendJoinRequest(final String requestEventId, final String requestEventDesc) {
        Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + requestEventId);
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // checks if the eventID is valid; i.e if it exists
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> updateEvent = new HashMap<String, Object>();
                    updateEvent.put(username, requestEventDesc);
                    final Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + requestEventId + "/requested");
                    firebase.keepSynced(true);
                    firebase.updateChildren(updateEvent, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            // send request for the user to join the event
                            setValues(requestEventId, requestEventDesc);
                        }
                    });

                } else {


                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * adds request into the event's requested database
     *
     * @param rID   : holds the event unique id
     * @param rDesc : holds the description along with the request
     */
    void setValues(final String rID, final String rDesc) {

        Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + username);
        firebase.keepSynced(true);
        Map<String, Object> request = new HashMap<>();
        request.put(rID, rDesc);
        firebase.updateChildren(request, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Toast.makeText(context, "Request sent successfully to " + rID, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "clicked item position " + getPosition() + eventIdList.get(getPosition()), Toast.LENGTH_SHORT).show();
                    sendRequest(eventIdList.get(getPosition()));
                    eventIdList.remove(getPosition());
                    notifyItemRemoved(getPosition());
                    break;
                }
            }
        }
    }

}
