package com.projects.shubhamkhandelwal.tisy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Login extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    // view elements
    Button loginButton;
    EditText usernameEditText, passwordEditText;
    String username, password;
    String name;
    // firebase reference object
    Firebase firebase;
    Intent intent;
    Uri userPhotoUrl;
    boolean noPhoto;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // Firebase context
        Firebase.setAndroidContext(this);
        noPhoto = false;
        startService(new Intent(getBaseContext(), InternetConnectionService.class));


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signInButton.setBackgroundColor(Color.parseColor("#4285F4"));
                        break;
                    case MotionEvent.ACTION_UP:
                        signInButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        break;
                }
                return false;
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


//
//        // initialize all the view items
//        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
//
//        //TODO: change the password from editText to passwordField
//        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
//        loginButton = (Button) findViewById(R.id.loginButton);
//
//
//        loginButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        loginButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        loginButton.setBackgroundColor(Color.parseColor("#0DFFFFFF"));
//                        break;
//                }
//                return false;
//            }
//        });
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                username = usernameEditText.getText().toString();
//                password = passwordEditText.getText().toString();
//
//                // check for empty fields
//                if (username.isEmpty() || password.isEmpty()) {
//
//                } else {
//                    userAction();
//                }
//            }
//        });
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(Login.this, "signed in", Toast.LENGTH_SHORT).show();
            actionOnSucess(acct.getEmail(), acct.getPhotoUrl(), acct.getDisplayName());
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    void actionOnSucess(String email, Uri photoUrl, String name) {
        String[] dotSplit = email.split("\\.");
        String tempUsername = dotSplit[0] + "-" + dotSplit[1];

        username = tempUsername.split("@")[0];

        if(name!= null){
            this.name = name;
        }
        if (photoUrl == null) {
            noPhoto = true;
        } else {
            noPhoto = false;
            userPhotoUrl = photoUrl;
        }
        Toast.makeText(Login.this, username, Toast.LENGTH_SHORT).show();
        userAction();
    }


    /*
     *checks if user exists or not
     * if exists the check for details and logs in
     * else, creates a new user
     */
    public void userAction() {
        // set the Firebase database reference
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);

        // listener for firebase
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // checks if the firebase reference (FIREBASE_USER_DETAILS) has the entered username in database
                if (dataSnapshot.hasChild(username)) {
                    storeSharedPreference();
                } else {
                    // TODO: ask for creation of new user; pop-up confirmation
                    save();
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // creates a new user in firebase
    public void save() {
        // has the password and the count (No. of events created)
        Map<String, Object> details = new HashMap<>();
        details.put("eventCount", 0);
        details.put("name", name);
        if (noPhoto) {
            details.put("userPhotoUri", noPhoto);
        } else {
            details.put("userPhotoUri", userPhotoUrl.toString());
        }
        // creating a new user
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username);
        // adding information to userDetails
        firebase.setValue(details, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

            }
        });
        storeSharedPreference();
    }

    // storing all the user information in SharedPreference
    void storeSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // to remove all the values from the SharedPreference
        //editor.clear();
        editor.putString("username", username);
        editor.putString("name", name);
        editor.apply();

        SharedPreferences loginPreferences = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPreferences.edit();
        loginEditor.putBoolean("login", true);
        loginEditor.apply();
        next();
    }

    // move to the next activity
    void next() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));

        intent = new Intent(Login.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));

    }
}
