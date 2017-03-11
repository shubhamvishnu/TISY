package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.MainActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shubham Khandelwal on 2/26/2017.
 */
public class ChatNotificationService extends Service {
    String username;
    Firebase firebase;
    List<String> activeEventList;
    SQLiteDatabaseConnection sqLiteDatabaseConnection;
    Map<String, Object> unreadEventChatCountMap;
    int eventsChatsCheckedCount = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constants.CHAT_NOTIFICATION_SERVICE_STATUS = true;
        username = getBaseContext().getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);
        sqLiteDatabaseConnection = new SQLiteDatabaseConnection(this);
        init();
        return START_STICKY;
    }

    void init() {

        activeEventList = new ArrayList<>();
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username + "/activeEvent");
        firebase.keepSynced(true);
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeEventList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    activeEventList.add(snapshot.getKey());
                }
                checkForExistence();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void checkForExistence() {

        for (int i = 0; i < activeEventList.size(); i++) {
            if (!sqLiteDatabaseConnection.checkForEvent(activeEventList.get(i))) {
                sqLiteDatabaseConnection.insertRow(activeEventList.get(i), 0);
            }
        }
        checkForUnreadChat();
    }

    void checkForUnreadChat() {

        unreadEventChatCountMap = new HashMap<>();
        eventsChatsCheckedCount = 0;
        for (int i = 0; i < activeEventList.size(); i++) {
            final int index = i;
            Firebase chatCountFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + activeEventList.get(index) +"/chats");
            chatCountFirebase.keepSynced(true);
            chatCountFirebase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    long totalChatChildren = dataSnapshot.getChildrenCount();
                    int chatReadCount = sqLiteDatabaseConnection.getCount(activeEventList.get(index));
                    if ((int)totalChatChildren > chatReadCount) {
                        unreadEventChatCountMap.put(activeEventList.get(index), ((int)totalChatChildren - chatReadCount));
                        chatNotification();
                    }


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

    }
void chatNotification(){
    if (unreadEventChatCountMap.size() > 0) {
        showUnreadChatsNotification();
    }
}
    void showUnreadChatsNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.multimedia_chat_icon);
        mBuilder.setLargeIcon(InitIcon.getCustomBitmapFromVectorDrawable(this, R.drawable.multimedia_chat_icon, 300,300));
        mBuilder.setContentTitle("Tisy");
        StringBuffer notificationContent = new StringBuffer();

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Message from " + unreadEventChatCountMap.size() + " event(s)");


        for(Map.Entry<String, Object> entry : unreadEventChatCountMap.entrySet()){

            inboxStyle.addLine(entry.getValue() + " from " + entry.getKey() +". \n");
        }
        mBuilder.setStyle(inboxStyle);
        mBuilder.setContentText("Message from " + unreadEventChatCountMap.size() + " event(s)");
        mBuilder.setAutoCancel(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// notificationID allows you to update the notification later on.
        mNotificationManager.notify(Constants.UNREAD_CHATS_NOTIFICATION_ID, mBuilder.build());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Constants.CHAT_NOTIFICATION_SERVICE_STATUS = false;
    }
}
