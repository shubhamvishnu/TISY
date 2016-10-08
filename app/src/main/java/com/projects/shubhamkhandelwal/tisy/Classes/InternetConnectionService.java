package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Shubham Khandelwal on 8/28/2016.
 */
public class InternetConnectionService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!checkInternetConnection()) {
            Toast.makeText(InternetConnectionService.this, "no internet connection", Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }
    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
