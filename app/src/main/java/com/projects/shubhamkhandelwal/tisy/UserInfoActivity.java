package com.projects.shubhamkhandelwal.tisy;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    long activeEventCount; // number of active event of the user.
    long createdEventCount; // number of events created; count of no. of events the user is the admin of.
    long joinedEventCount; // number of events user has joined.
    GoogleApiClient mGoogleApiClient;
    String userPhotoUri; // user profile photo url.


    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        FacebookSdk.sdkInitialize(getApplicationContext());
        initializeUserInformation();
        initLogout();
        initProgressDialog();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * fetches information about the user.
     * after fetching-user information dialog is shown.
     */
    void initializeUserInformation() {

        Firebase userInfoFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        userInfoFirebase.keepSynced(true);
        userInfoFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeEventCount = 0; // total number of events user is a part of currently.
                createdEventCount = 0; // total number of events user has created.
                joinedEventCount = 0; // total number of events user has joined.

                userPhotoUri = dataSnapshot.child("userPhotoUri").getValue().toString();
                activeEventCount = dataSnapshot.child("activeEvent").getChildrenCount();

                for (DataSnapshot children : dataSnapshot.child("activeEvent").getChildren()) {
                    if (children.getValue().toString().equals("created")) {
                        ++createdEventCount;
                    } else if (children.getValue().toString().equals("joined")) {
                        ++joinedEventCount;
                    }
                }

                // call to show the user information dialog after initialization.
                showInformationDialog();
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    // show the user information dialog
    void showInformationDialog() {
        // initialize the view items in the dialog.
        TextView nameEditText = (TextView) findViewById(R.id.name_text_view);
        TextView userIdEditText = (TextView) findViewById(R.id.user_id_text_view);
        TextView activeEventscountTextView = (TextView) findViewById(R.id.active_events_count_text_view);
        TextView createdEventsCountTextView = (TextView) findViewById(R.id.created_events_count_text_view);
        TextView joinedEventsCountTextView = (TextView) findViewById(R.id.joined_events_count_text_view);
        Button aboutUsButton = (Button) findViewById(R.id.about_us_button);
        final CircleImageView profileImageView = (CircleImageView) findViewById(R.id.profile_image_circle_image_view);
        Button logoutButton = (Button) findViewById(R.id.logout_button);
        Picasso.with(this).load(Uri.parse(userPhotoUri)).networkPolicy(NetworkPolicy.OFFLINE).into(profileImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(UserInfoActivity.this).load(Uri.parse(userPhotoUri)).error(R.drawable.default_profile_image_icon).into(profileImageView);
            }
        });

        //adding Typeface
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "tisy_logo_font.ttf");
//        aboutUsButton.setTypeface(typeface);

        SharedPreferences userInfoPreference = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE);
        nameEditText.setText(userInfoPreference.getString("name", null));
        userIdEditText.setText(userInfoPreference.getString("username", null));

        activeEventscountTextView.setText(String.valueOf(activeEventCount));
        createdEventsCountTextView.setText(String.valueOf(createdEventCount));
        joinedEventsCountTextView.setText(String.valueOf(joinedEventCount));

        aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toAboutUs();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showConfirmationDialog();

            }
        });

    }

    void showConfirmationDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        progressDialog.show();
                        logout();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void toAboutUs() {
        Intent intent = new Intent(UserInfoActivity.this, AboutUsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void logout() {


        String type = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE).getString("login_type", null);
        if (type != null) {
            if (type.equals(Constants.LOGIN_TYPE_GOOGLE)) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                loggedout();
                            }
                        });
            } else if (type.equals(Constants.LOGIN_TYPE_FACEBOOK)) {
                LoginManager.getInstance().logOut();
                loggedout();
            }
        }


    }

    void initLogout() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    void loggedout() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        stopService(new Intent(getBaseContext(), ChatNotificationService.class));
        stopService(new Intent(getBaseContext(), LocationListenerService.class));
        stopService(new Intent(getBaseContext(), RequestNotificationService.class));

        getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SharedPreferencesName.CHATS_READ_COUNT, MODE_PRIVATE).edit().clear().apply();

        SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(UserInfoActivity.this);
        sqLiteDatabaseConnection.emptyTable();

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loging out!");
        progressDialog.setMessage("Working on it...");
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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
