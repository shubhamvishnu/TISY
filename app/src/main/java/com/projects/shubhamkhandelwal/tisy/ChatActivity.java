package com.projects.shubhamkhandelwal.tisy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatsRecyclerViewAdpater;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventChat;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends FragmentActivity {
    // chat variables
    RecyclerView eventChatsRecyclerView; // chats view recyclerview
    ChatsRecyclerViewAdpater chatsRecyclerViewAdapter; // chats view recyclerview adapter
    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton backArrowImageView;
        backArrowImageView = (ImageButton) findViewById(R.id.back_arrow_image_button_chat_dialog);

        eventChatsRecyclerView = (RecyclerView) findViewById(R.id.event_chats_recycler_view);
        eventChatsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        eventChatsRecyclerView.setLayoutManager(linearLayoutManager);

        chatsRecyclerViewAdapter = new ChatsRecyclerViewAdpater(getApplicationContext(), linearLayoutManager);
        eventChatsRecyclerView.setAdapter(chatsRecyclerViewAdapter);

        final EditText chatsMessageEditText = (EditText) findViewById(R.id.chatsMessageEditText);
        final ImageButton chatsSendButton = (ImageButton) findViewById(R.id.chatsSendButton);
        final int color = Color.parseColor("#666666");
        chatsSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chatsSendButton.setColorFilter(color);
                        break;
                    case MotionEvent.ACTION_UP:
                        chatsSendButton.setColorFilter(Color.TRANSPARENT);
                        break;
                }
                return false;
            }
        });

        chatsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageEditText = chatsMessageEditText.getText().toString();
                if (messageEditText.isEmpty()) {
                } else {
                    sendMessage(messageEditText);
                    chatsMessageEditText.setText("");
                }
            }
        });


        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChatsReadCount();
                toMaps();
            }
        });


    }

    void updateChatsReadCount() {

        Firebase chatsFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        chatsFirebase.keepSynced(true);
        chatsFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long childCount = dataSnapshot.getChildrenCount();
                SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(ChatActivity.this);
                int count = sqLiteDatabaseConnection.getCount(Constants.currentEventId);
                if (count < childCount) {
                    sqLiteDatabaseConnection.updateCount(Constants.currentEventId, (int) childCount);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void sendMessage(final String message) {
        SQLiteDatabaseConnection sqlLiteDatabaseConnection = new SQLiteDatabaseConnection(this);
        int count = sqlLiteDatabaseConnection.getCount(Constants.currentEventId);
        sqlLiteDatabaseConnection.updateCount(Constants.currentEventId, ++count);
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        EventChat chat = new EventChat(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), message);
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), message);
        firebase.push().setValue(chat);
        updateChatsReadCount();
    }

    @Override
    public void onBackPressed() {
        updateChatsReadCount();
        toMaps();
    }

    void toMaps() {

        Intent intent = new Intent(ChatActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (!Constants.CHAT_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), ChatNotificationService.class));
        }
        finish();
    }
}
