package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.projects.shubhamkhandelwal.tisy.MapsActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 8/30/2016.
 */
public class ChatsRecyclerViewAdpater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static int VIEW_TYPE_USER_MESSAGE = 1;
    public static int VIEW_TYPE_OTHER_MESSAGE = 2;
    public List<EventChat> eventChatList = new ArrayList<>();
    Context context;
    Firebase firebase;
    LinearLayoutManager layoutManager;
    String currentUsername;
    private LayoutInflater inflator;


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

    void createSharedPreference() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.currentEventId+SharedPreferencesName.CHATS_READ_COUNT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // to remove all the values from the SharedPreference
        //editor.clear();
        editor.putInt("chats_read", 0);
        editor.apply();

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
    public int getItemViewType(int position) {
        if (eventChatList.get(position).getUsername().equals(currentUsername)) {
            return VIEW_TYPE_USER_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_OTHER_MESSAGE) {
            View view = inflator.inflate(R.layout.recycler_view_chats_other_member_message_recycler_view_row_layout, parent, false);
            ChatsOtherMemberMessageRecyclerViewHolder viewHolder = new ChatsOtherMemberMessageRecyclerViewHolder(view);
            return viewHolder;
        } else {
            View view = inflator.inflate(R.layout.recycler_view_chats_user_member_message_row_layout, parent, false);
            ChatsUserMemberMessageRecyclerViewHolder viewHolder = new ChatsUserMemberMessageRecyclerViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String username = eventChatList.get(position).getUsername();
        String message = eventChatList.get(position).getMessage();
        if (getItemViewType(position) == VIEW_TYPE_OTHER_MESSAGE) {
            ((ChatsOtherMemberMessageRecyclerViewHolder) holder).usernameOtherMemberMessageTextView.setText(username);
            ((ChatsOtherMemberMessageRecyclerViewHolder) holder).messageOtherMemberMessageTextView.setText(message);
        }else{
            ((ChatsUserMemberMessageRecyclerViewHolder) holder).messageUserMemberMessageTextView.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return eventChatList.size();
    }

    class ChatsOtherMemberMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView usernameOtherMemberMessageTextView;
        TextView messageOtherMemberMessageTextView;

        public ChatsOtherMemberMessageRecyclerViewHolder(View itemView) {
            super(itemView);
            usernameOtherMemberMessageTextView = (TextView) itemView.findViewById(R.id.username_other_member_message_text_view);
            messageOtherMemberMessageTextView = (TextView) itemView.findViewById(R.id.message_other_member_message_text_view);
        }
    }

    class ChatsUserMemberMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView messageUserMemberMessageTextView;

        public ChatsUserMemberMessageRecyclerViewHolder(View itemView) {
            super(itemView);
            messageUserMemberMessageTextView = (TextView) itemView.findViewById(R.id.message_user_member_message_text_view);
        }
    }
}
