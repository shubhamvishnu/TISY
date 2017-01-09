package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

/**
 * Created by Shubham Khandelwal on 9/25/2016.
 */
public class JoinEventRequestsRecyclerViewAdapter extends RecyclerView.Adapter<JoinEventRequestsRecyclerViewAdapter.JoinEventRequestsRecyclerViewHolder> {
    List<JoinEventInfo> joinEventIds = new ArrayList();
    Firebase firebase;
    Context context;
    String username;
    private LayoutInflater inflator;

    public JoinEventRequestsRecyclerViewAdapter(android.content.Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        username = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        initializeRequests();
    }

    void initializeRequests() {
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + username);
        firebase.keepSynced(true);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int position = joinEventIds.size();
                JoinEventInfo joinEventInfo = new JoinEventInfo(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                joinEventIds.add(joinEventInfo);
                notifyItemInserted(position);
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
    public JoinEventRequestsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.dialog_show_user_requests_row_layout, parent, false);
        JoinEventRequestsRecyclerViewHolder viewHolder = new JoinEventRequestsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(JoinEventRequestsRecyclerViewHolder holder, int position) {
        holder.eventIdTextView.setText(joinEventIds.get(position).getEventId());
        holder.descriptionRequestTextView.setText(joinEventIds.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return joinEventIds.size();
    }


    class JoinEventRequestsRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView eventIdTextView;
        ImageButton removeRequestImageButton;
        TextView descriptionRequestTextView;

        public JoinEventRequestsRecyclerViewHolder(View itemView) {
            super(itemView);
            eventIdTextView = (TextView) itemView.findViewById(R.id.event_id_title_text_view);
            descriptionRequestTextView = (TextView) itemView.findViewById(R.id.description_request_text_view);
            removeRequestImageButton = (ImageButton) itemView.findViewById(R.id.remove_request_image_button);
            removeRequestImageButton.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.remove_request_image_button: {
                    final int position = getPosition();
                    if (joinEventIds.size() >  0 && position >= 0 && position < joinEventIds.size()) {
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setTitle("Removing");
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
                    progressDialog.show();

                        Firebase removeMemberRequestFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + joinEventIds.get(position).getEventId() + "/requested/" + username);
                        removeMemberRequestFirebase.removeValue(new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                                Firebase removeRequestFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + username + "/" + joinEventIds.get(position).getEventId());
                                removeRequestFirebase.removeValue(new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        joinEventIds.remove(position);
                                        notifyItemRemoved(position);
                                        progressDialog.dismiss();
                                    }
                                });

                            }
                        });
                    }
                    break;
                }
            }
        }
    }
}
