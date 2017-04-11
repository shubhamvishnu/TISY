package com.projects.shubhamkhandelwal.tisy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Login extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    // view elements

    String username;
    String serviceName;
    String name;
    // firebase reference object
    Firebase firebase;
    Intent intent;
    Uri userPhotoUrl;
    boolean noPhoto;
    String type;
    String email;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    CallbackManager callbackManager;
    LoginButton fbloginButton;
    ProgressDialog progressDialog;
    TextView mAppName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Firebase context
        Firebase.setAndroidContext(this);
        initProgressDialog();

        noPhoto = false;



        mAppName = (TextView) findViewById(R.id.app_name);

        //adding Typeface
        Typeface typeface = Typeface.createFromAsset(getAssets(), "tisy_logo_font.ttf");
        mAppName.setTypeface(typeface);


        callbackManager = CallbackManager.Factory.create();
        fbloginButton = (LoginButton) findViewById(R.id.login_button);
        fbloginButton.setReadPermissions(Arrays.asList("email"));

        fbloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    final String name = profile.getName();
                    final Uri uri = profile.getProfilePictureUri(200, 200);

                    // void actionOnSucess(String email, Uri photoUrl, String name) {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Bundle bFacebookData = getFacebookData(object);
                            String email = bFacebookData.getString("email", null);
                            if (email == null || email.isEmpty()) {
                                LoginManager.getInstance().logOut();
                                showEmailAlert();
                            } else {
                                actionOnSucess(email, uri, name, Constants.LOGIN_TYPE_FACEBOOK);
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id, email");
                    request.setParameters(parameters);
                    request.executeAsync();
                } else {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            mProfileTracker.stopTracking();
                            if (profile2 != null) {
                                final String name = profile2.getName();
                                final Uri uri = profile2.getProfilePictureUri(200, 200);

                                // void actionOnSucess(String email, Uri photoUrl, String name) {
                                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Bundle bFacebookData = getFacebookData(object);
                                        String email = bFacebookData.getString("email", null);
                                        if (email == null || email.isEmpty()) {
                                            LoginManager.getInstance().logOut();
                                            showEmailAlert();
                                        } else {
                                            actionOnSucess(email, uri, name, Constants.LOGIN_TYPE_FACEBOOK);
                                        }
                                    }
                                });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id, email");
                                request.setParameters(parameters);
                                request.executeAsync();
                            }

                        }
                    };
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

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


    }

    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Sign In");
        progressDialog.setMessage("Performing authentication... ");
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

    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    void showEmailAlert() {
        Alerter.create(Login.this)
                .setTitle("Give email permission")
                .setText("Tisy uses email address to verify and authorize it's users. Kindly grant email permission.")
                .setBackgroundColor(R.color.facebook_login_email)
                .show();
    }

    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();
        try {

            String id = object.getString("id");
            bundle.putString("fb_id", id);
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));

        } catch (JSONException e) {
            Alerter.create(Login.this)
                    .setText("Oops! There seemed to be some problem.")
                    .setBackgroundColor(R.color.facebook_login_email)
                    .show();
        }
        return bundle;
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
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            actionOnSucess(acct.getEmail(), acct.getPhotoUrl(), acct.getDisplayName(), Constants.LOGIN_TYPE_GOOGLE);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    void actionOnSucess(String email, Uri photoUrl, String name, String type) {
        progressDialog.show();
        String[] dotSplit = email.split("\\.");

        String tempUsername = dotSplit[0] + "-" + dotSplit[1];
        String[] splitAt = tempUsername.split("@");
        username = splitAt[0];
        serviceName = splitAt[1];

        this.type = type;
        saveLoginType(type);

        this.email = email;
        if (name != null) {
            this.name = name;
        }
        if (photoUrl == null) {
            noPhoto = true;
        } else {
            noPhoto = false;
            userPhotoUrl = photoUrl;
        }

        userAction();
    }

    void saveLoginType(String type) {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesName.LOGIN_STATUS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("login_type", type);
        editor.apply();
    }


    /*
     *checks if user exists or not
     * if exists the check for details and logs in
     * else, creates a new user
     */
    public void userAction() {
        // set the Firebase database reference
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);
        firebase.keepSynced(true);
        // listener for firebase
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // checks if the firebase reference (FIREBASE_USER_DETAILS) has the entered username in database
                if (dataSnapshot.hasChild(username)) {
                    String tempEmailID = dataSnapshot.child(username).child("email").getValue().toString();
                    if (Objects.equals(tempEmailID, email)) {
                        updateUserProfilePhotoInfo();
                    } else {
                        save();
                    }
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

    void updateUserProfilePhotoInfo() {
        Map<String, Object> details = new HashMap<>();
        if (noPhoto) {
            details.put("userPhotoUri", noPhoto);
        } else {
            details.put("userPhotoUri", userPhotoUrl.toString());
        }
        // creating a new user
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + username);

        // adding information to userDetails
        firebase.updateChildren(details, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                storeSharedPreference();
            }
        });

    }

    // creates a new user in firebase
    public void save() {
        username = username + "-" + serviceName;


        // has the password and the count (No. of events created)
        Map<String, Object> details = new HashMap<>();
        details.put("eventCount", 0);
        details.put("name", name);
        if (noPhoto) {
            details.put("userPhotoUri", noPhoto);
        } else {
            details.put("userPhotoUri", userPhotoUrl.toString());
        }
        details.put("lastSeen", "unknown");
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

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        next();
    }

    // move to the next activity
    void next() {

        intent = new Intent(Login.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }


    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}
