package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.MainActivity;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 3/12/2017.
 */
public class RequestNotificationService extends Service {
    String username;
    SQLiteDatabaseConnection sqLiteDatabaseConnection;
    List<RequestedUserListClass> requestedUserList;
    List<String> activeEventList;
    Firebase firebase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constants.REQUEST_NOTIFICATION_SERVICE_STATUS = true;
        username = getBaseContext().getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

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
                checkForNotification();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void checkForNotification() {
        requestedUserList = new ArrayList<>();
        for (int i = 0; i < activeEventList.size(); i++) {
            final int index = i;
            Firebase chatCountFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + activeEventList.get(index) + "/requested");
            chatCountFirebase.keepSynced(true);
            chatCountFirebase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<String> usernames = new ArrayList<String>();
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            usernames.add(requestSnapshot.getKey());
                        }
                        RequestedUserListClass requestedUserListClass = new RequestedUserListClass(usernames, activeEventList.get(index));
                        requestedUserList.add(requestedUserListClass);
                        checkForRequests();
                    }


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
    void checkForRequests(){
        if(requestedUserList.size()>0){
            showRequestNotification();
        }
    }
    void showRequestNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.invite_icon);
        mBuilder.setLargeIcon(InitIcon.getCustomBitmapFromVectorDrawable(this, R.drawable.invite_icon, 300,300));
        mBuilder.setAutoCancel(true);
        mBuilder.setContentTitle("Tisy");
        mBuilder.setContentText("Your events have join requests");
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle(requestedUserList.size() + " events have received requests");


        for(int i = 0; i < requestedUserList.size(); i++){
            inboxStyle.addLine(requestedUserList.get(i).getRequestedUsernameList().size() + " for "+requestedUserList.get(i).getEventID() + ".\n");
        }
        mBuilder.setStyle(inboxStyle);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
    @Override
    public void onDestroy() {
        Constants.REQUEST_NOTIFICATION_SERVICE_STATUS = false;
        super.onDestroy();
    }

    class RequestedUserListClass {
        List<String> requestedUsernameList;
        String eventID;

        public RequestedUserListClass(List<String> requestedUsernameList, String eventID) {
            this.requestedUsernameList = requestedUsernameList;
            this.eventID = eventID;
        }

        public List<String> getRequestedUsernameList() {
            return requestedUsernameList;
        }

        public void setRequestedUsernameList(List<String> requestedUsernameList) {
            this.requestedUsernameList = requestedUsernameList;
        }

        public String getEventID() {
            return eventID;
        }

        public void setEventID(String eventID) {
            this.eventID = eventID;
        }
    }
}
