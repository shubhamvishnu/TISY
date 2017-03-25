package com.projects.shubhamkhandelwal.tisy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.JoinEventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.JoinEventRequestsRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import java.util.ArrayList;
import java.util.List;

public class JoinEventActvity extends FragmentActivity {
    String username;
    List<JoinEventInfo> joinEventIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_actvity);
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        initFAB();
        showSentJoinRequests();

    }

    void initFAB(){
        ImageView newRequestImageIcon = new ImageView(this); // Create an icon imageview.
        newRequestImageIcon.setBackgroundColor(getResources().getColor(R.color.main_activity_create_event));
        newRequestImageIcon.setImageResource(R.drawable.create_event_icon);

        // create menu
//        FloatingActionButton floatingActionButton = new FloatingActionButton.Builder(this) //builder for the user account information FAB.
//                .setContentView(newRequestImageIcon)
//                .setBackgroundDrawable(R.drawable.join_event_fab_background_selector)
//                .build();
//
//        newRequestImageIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toSearchEventActivity();
//            }
//        });
        final com.melnykov.fab.FloatingActionButton fab = new com.melnykov.fab.FloatingActionButton(this);
        fab.setType(com.melnykov.fab.FloatingActionButton.TYPE_NORMAL);
        fab.setImageResource(R.drawable.create_event_icon);
        fab.setColorPressedResId(R.color.main_activity_create_event);
        fab.setColorNormalResId(R.color.main_activity_create_event);
        fab.setColorRippleResId(R.color.main_activity_create_event);
        fab.setShadow(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(JoinEventActvity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showSentJoinRequests() {


        final LinearLayout noRequestLinearLayout = (LinearLayout) findViewById(R.id.no_request_layout);
        final TextView sentRequestsTextView = (TextView) findViewById(R.id.sent_requests_text_view);

        final RecyclerView joinEventRequestsRecyclerView = (RecyclerView) findViewById(R.id.dialog_sent_requests_recycler_view);

        Firebase checkRequests = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_REQUESTS + username);
        checkRequests.keepSynced(true);
        checkRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    joinEventIds = new ArrayList<JoinEventInfo>();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        JoinEventInfo joinEventInfo = new JoinEventInfo(snapshot.getKey(), snapshot.getValue().toString());
                        joinEventIds.add(joinEventInfo);
                    }

                    noRequestLinearLayout.setVisibility(View.INVISIBLE);
                    sentRequestsTextView.setVisibility(View.VISIBLE);
                    joinEventRequestsRecyclerView.setVisibility(View.VISIBLE);
                    JoinEventRequestsRecyclerViewAdapter joinEventRequestsRecyclerViewAdapter;


                    joinEventRequestsRecyclerView.setHasFixedSize(true);

                    LinearLayoutManager linearLayoutManagerSentRequests = new LinearLayoutManager(JoinEventActvity.this);
                    joinEventRequestsRecyclerView.setLayoutManager(linearLayoutManagerSentRequests);

                    joinEventRequestsRecyclerViewAdapter = new JoinEventRequestsRecyclerViewAdapter(JoinEventActvity.this, joinEventIds);
                    joinEventRequestsRecyclerView.setAdapter(joinEventRequestsRecyclerViewAdapter);


                } else {

                    noRequestLinearLayout.setVisibility(View.VISIBLE);
                    sentRequestsTextView.setVisibility(View.INVISIBLE);
                    joinEventRequestsRecyclerView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
    void toSearchEventActivity(){
        Intent intent = new Intent(JoinEventActvity.this, SearchEventActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(JoinEventActvity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
