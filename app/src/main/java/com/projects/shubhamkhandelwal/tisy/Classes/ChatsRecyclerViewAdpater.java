package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.vision.text.Text;
import com.projects.shubhamkhandelwal.tisy.MapsActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.Inflater;

/**
 * Created by Shubham Khandelwal on 8/30/2016.
 */
public class ChatsRecyclerViewAdpater extends RecyclerView.Adapter<ChatsRecyclerViewAdpater.ChatsRecyclerViewHolder> {
    Context context;
    private LayoutInflater inflator;
    public List<EventChat> eventChatList = new ArrayList<>();
    Firebase firebase;
    public static int currentPosition;
    LinearLayoutManager layoutManager;
    String currentUsername;
    int chatsReadCount = 0;


    public ChatsRecyclerViewAdpater(android.content.Context context, LinearLayoutManager linearLayoutManager) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.eventChatList = new ArrayList<>();
        Firebase.setAndroidContext(context);
        layoutManager = linearLayoutManager;
        currentUsername = context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null);
        createSharedPreference();
        setEventChatListener();
    }
    void createSharedPreference(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPreferencesName.CHATS_READ_COUNT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // to remove all the values from the SharedPreference
        //editor.clear();
        editor.putInt("chats_read", 0);
        editor.apply();

    }
    void updateChatCountSharedPreference(int count){
        MapsActivity.numberOfReadChats = count;
        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPreferencesName.CHATS_READ_COUNT, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("chats_read", count).apply();
    }
    void setEventChatListener() {

        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/chats");
        firebase.keepSynced(true);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("listenerChildAdded", "listener called");
                EventChat chat = dataSnapshot.getValue(EventChat.class);
                int position = eventChatList.size();
                eventChatList.add(chat);
                notifyItemInserted(position);
                layoutManager.scrollToPosition(position);
                ++chatsReadCount;
                updateChatCountSharedPreference(chatsReadCount);
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
    public ChatsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_chats_row_layout, parent, false);
        ChatsRecyclerViewHolder viewHolder = new ChatsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatsRecyclerViewHolder holder, int position) {
            holder.usernameTextView.setText(eventChatList.get(position).getUsername());
            holder.messageTextView.setText(eventChatList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return eventChatList.size();
    }

    class ChatsRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView messageTextView;
        TextView leftSideBorderTextView;


        public ChatsRecyclerViewHolder(View itemView) {
            super(itemView);
            usernameTextView = (TextView) itemView.findViewById(R.id.username_chat_recycler_view_text_view);
            messageTextView = (TextView) itemView.findViewById(R.id.message_chat_recycler_view_text_view);
            leftSideBorderTextView = (TextView) itemView.findViewById(R.id.left_side_message_border_text_view);
        }


    }
}
