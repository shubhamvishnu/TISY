package com.projects.shubhamkhandelwal.tisy;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.projects.shubhamkhandelwal.tisy.Classes.ChatNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.LocationListenerService;
import com.projects.shubhamkhandelwal.tisy.Classes.RequestNotificationService;
import com.projects.shubhamkhandelwal.tisy.Classes.SQLiteDatabaseConnection;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.projects.shubhamkhandelwal.tisy.Classes.TimeStamp;
import com.tapadoo.alerter.Alerter;

import java.util.HashMap;
import java.util.Map;


public class CreateEvent extends Activity {

    public static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    public static final int REQUEST_PERMISSION_SETTINGS = 1;
    public static String CREATE_EVENT_TAG = "CreateEvent";
    // firebase reference object
    Firebase firebase;
    // temporary eventId
    String eventId;
    EventInfo eventInfo;
    //Button sLocation;
    ImageButton dLocation;
    Button createEventButton;
    ImageButton dIconButton;
    Button editDestinationIconButton;
    //EditText sLocationDescEditText;
    EditText dLocationDescEditText;
    EditText descriptionEditText;
    EditText titleEditText;

    TextView userIdTextView;
    //ImageButton sLocationEditImageButton;
    ImageButton dLocationEditImageButton;
    ImageButton dLocationIconImageButton;
    Intent intent;
    CoordinatorLayout coordinatorLayout;
    Map<String, Object> members;

    ProgressDialog progressDialog;

    // number of events created by this user
    int eventCount;
    // intialization for place picker dialog
    int PLACE_PICKER_REQUEST = 1;
    /*
    * to check if the choice is for start location or destination location.
    * 1 is for start; 2 is for destination.
    */
    int locationPreference = 0;
    PlacePicker.IntentBuilder builder;
    int iconResourceId;
   // LinearLayout sLocationLinearLayout;
    LinearLayout dLocationLinearLayout;
    LinearLayout dLocationIconLinearLayout;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2840079713824644~7949777217");
        // Firebase context
        Firebase.setAndroidContext(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutCreateEvent);

        // Object to store the event informations
        eventInfo = new EventInfo();

        // intializing the members List object
        members = new HashMap<>();

        // intializing the view elements

        dLocation = (ImageButton) findViewById(R.id.dlocation);
        createEventButton = (Button) findViewById(R.id.createEventButton);
        dIconButton = (ImageButton) findViewById(R.id.dIconButton);
        editDestinationIconButton = (Button) findViewById(R.id.edit_destination_icon_button);


        dLocationDescEditText = (EditText) findViewById(R.id.dLocationDescEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        titleEditText = (EditText) findViewById(R.id.title_edit_text);

        userIdTextView = (TextView) findViewById(R.id.userId);

        dLocationEditImageButton = (ImageButton) findViewById(R.id.editDLocationImageButton);
        dLocationIconImageButton = (ImageButton) findViewById(R.id.dLocationIconImageButton);


        dLocationLinearLayout = (LinearLayout) findViewById(R.id.dLocationLinearLayout);
        dLocationIconLinearLayout = (LinearLayout) findViewById(R.id.dLocationIconLinearLayout);



        dLocationLinearLayout.setVisibility(View.INVISIBLE);
        dLocationIconLinearLayout.setVisibility(View.INVISIBLE);

        // inaitalizing eventId
        eventId = new String();
        iconResourceId = -1;


        // generate the eventId; calls the generate() function
        generateEventId();
        initProgressDialog();
        initAdd();

        initServices();


        dLocationEditImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationPreference = 2;
                placePickerDialog();
            }
        });



        dIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                showDestinationIconDialog();


            }
        });
        editDestinationIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDestinationIconDialog();
            }
        });
        // TODO: change the locationPreference value intialization using switch case (optional)

        dLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                locationPreference = 2;
                placePickerDialog();
            }
        });

        /*
         *TODO: add members dynamically; by the host (optional)
         *TODO: or let users join the created event
         *TODO: or let users join the created event
         */
        members.put(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null), "0.0,0.0");



        // creating event in firebase
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String destLocation = eventInfo.getdLocation();
                String destLocationDesc = eventInfo.getdLocationDesc();

                String eventDescription = descriptionEditText.getText().toString();
                String eventTitle = titleEditText.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    if (!eventId.isEmpty()) {
                        if (dLocationDescEditText.getText().toString().isEmpty() || destLocation == null || destLocationDesc == null || destLocation.isEmpty() || destLocationDesc.isEmpty() || iconResourceId == -1 || eventDescription == null || eventDescription.isEmpty() || eventTitle.isEmpty() || eventTitle == null) {
                            Alerter.create(CreateEvent.this)
                                    .setText("Please enter all the details...")
                                    .setBackgroundColor(R.color.colorPrimaryDark)
                                    .show();
                        } else {
                            if (checkInternetConnection()) {
                                progressDialog.show();

                                if (!checkInternetConnection()) {
                                    Alerter.create(CreateEvent.this)
                                            .setText("Oops! no internet connection...")
                                            .setBackgroundColor(R.color.colorAccent)
                                            .setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    openInternetSettings();
                                                }
                                            })
                                            .show();
                                } else {

                                    // creating reference with the new eventID
                                    firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + eventId);
                                    Map<String, Object> newEvent = new HashMap<String, Object>();
                                    newEvent.put("admin", getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
                                    newEvent.put("info", eventInfo);
                                    newEvent.put("members", members);
                                    newEvent.put("dIcon", iconResourceId);
                                    newEvent.put("desc", eventDescription);
                                    newEvent.put("title", eventTitle);
                                    newEvent.put("time", TimeStamp.getTime());
                                    firebase.setValue(newEvent, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            updateEvent();
                                        }
                                    });
                                }

                            }
                        }

                    } else {
                        Alerter.create(CreateEvent.this)
                                .setText("Oops! no internet connection...")
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        openInternetSettings();
                                    }
                                })
                                .setBackgroundColor(R.color.colorAccent)
                                .show();
                    }
                }
            }
        });
    }
    void openInternetSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
        startActivity(intent);
    }

    void initServices(){
        if(!Constants.LOCATION_NOTIFICATION_SERVICE_STATUS){
            startService(new Intent(getBaseContext(), LocationListenerService.class));
        }
        if(!Constants.CHAT_NOTIFICATION_SERVICE_STATUS) {
            startService(new Intent(getBaseContext(), ChatNotificationService.class));
        }
        if(!Constants.REQUEST_NOTIFICATION_SERVICE_STATUS){
            startService(new Intent(getBaseContext(), RequestNotificationService.class));
        }
    }
void initAdd(){
    mInterstitialAd = new InterstitialAd(this);
    mInterstitialAd.setAdUnitId("ca-app-pub-2840079713824644/4000158413");

    mInterstitialAd.setAdListener(new AdListener() {
        @Override
        public void onAdClosed() {
            requestNewInterstitial();
            checkForCount();
        }
    });
    requestNewInterstitial();
}
    void checkForCount(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            next();
        }
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("making changes...");
        progressDialog.setMessage("Working on it!");
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

    // get the eventCount of the user and generate an eventID by calling generate()
    void generateEventId() {
        // intializing eventCount to 0
        eventCount = 0;
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventCount = Integer.parseInt(dataSnapshot.child(getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null)).child("eventCount").getValue().toString());
                generate(eventCount);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void showDestinationIconDialog() {
        ImageButton destinationWalking, destinationSwimming, destinationSpa, destinationGym, destinationFootBall, destinationBicycle, destinationRunning, destinationDance, destinationProtest;
        ImageButton destinationDrinks, destinationCasino, destinationZoo, destinationBowlingAlley, destinationAmusementPark, destinationAquarium, destinationMovie, destinationNightClub, destinationGaming;
        ImageButton destinationCafe, destinationRestaurant, destinationDinning, destinationPizza;
        ImageButton destinationHotel, destinationUniversity, destinationLibrary, destinationMuseum, destinationStadium, destinationSchool, destinationBeautySalon, destinationHome, destinationPark, destinationConference;
        ImageButton destinationPharmacy, destinationHospital, destinationWorship, destinationYoga;
        ImageButton destinationMall, destinationBookStore, destinationConvenienceStore, destinationLiquorStore, destinationLaundry, destinationGrocery, destinationPrintShop;
        ImageButton destinationParking, destinationAirport, destinationTrainStation, destinationBusStation, destinationSubwayStation, destinationTram;


        Button closeDestinationIconDialogButton;

        final Dialog dialog = new Dialog(this, R.style.event_info_dialog_style);
        dialog.setContentView(R.layout.dialog_choose_destination_icon);

        destinationWalking = (ImageButton) dialog.findViewById(R.id.destination_walking);
        destinationSwimming = (ImageButton) dialog.findViewById(R.id.destination_swimming);
        destinationSpa = (ImageButton) dialog.findViewById(R.id.destination_spa);
        destinationGym = (ImageButton) dialog.findViewById(R.id.destination_gym);
        destinationDrinks = (ImageButton) dialog.findViewById(R.id.destination_drinks);
        destinationCasino = (ImageButton) dialog.findViewById(R.id.destination_casino);
        destinationZoo = (ImageButton) dialog.findViewById(R.id.destination_zoo);
        destinationBowlingAlley = (ImageButton) dialog.findViewById(R.id.destination_bowling_alley);
        destinationAmusementPark = (ImageButton) dialog.findViewById(R.id.destination_amusement_park);
        destinationAquarium = (ImageButton) dialog.findViewById(R.id.destination_aquarium);
        destinationNightClub = (ImageButton) dialog.findViewById(R.id.destination_night_club);
        destinationFootBall = (ImageButton) dialog.findViewById(R.id.destination_football);
        destinationBicycle = (ImageButton) dialog.findViewById(R.id.destination_bicycler);
        destinationRunning = (ImageButton) dialog.findViewById(R.id.destination_running);
        destinationGaming = (ImageButton) dialog.findViewById(R.id.destination_gaming);
        destinationCafe = (ImageButton) dialog.findViewById(R.id.destination_cafe);
        destinationRestaurant = (ImageButton) dialog.findViewById(R.id.destination_restaurant);
        destinationDinning = (ImageButton) dialog.findViewById(R.id.destination_dinning);
        destinationPizza = (ImageButton) dialog.findViewById(R.id.destination_pizza);
        destinationPark = (ImageButton) dialog.findViewById(R.id.destination_park);
        destinationPharmacy = (ImageButton) dialog.findViewById(R.id.destination_pharmacy);
        destinationHospital = (ImageButton) dialog.findViewById(R.id.destination_hospital);
        destinationHotel = (ImageButton) dialog.findViewById(R.id.destination_hotel);
        destinationUniversity = (ImageButton) dialog.findViewById(R.id.destination_university);
        destinationLibrary = (ImageButton) dialog.findViewById(R.id.destination_library);
        destinationMuseum = (ImageButton) dialog.findViewById(R.id.destination_museum);
        destinationStadium = (ImageButton) dialog.findViewById(R.id.destination_stadium);
        destinationSchool = (ImageButton) dialog.findViewById(R.id.destination_school);
        destinationBeautySalon = (ImageButton) dialog.findViewById(R.id.destination_beauty_salon);
        destinationHome = (ImageButton) dialog.findViewById(R.id.destination_home);
        destinationWorship = (ImageButton) dialog.findViewById(R.id.destination_worship);
        destinationMall = (ImageButton) dialog.findViewById(R.id.destination_mall);
        destinationBookStore = (ImageButton) dialog.findViewById(R.id.destination_book_store);
        destinationConvenienceStore = (ImageButton) dialog.findViewById(R.id.destination_convenience_store);
        destinationLiquorStore = (ImageButton) dialog.findViewById(R.id.destination_liquor_store);
        destinationLaundry = (ImageButton) dialog.findViewById(R.id.destination_laundry);
        destinationGrocery = (ImageButton) dialog.findViewById(R.id.destination_grocery_store);
        destinationPrintShop = (ImageButton) dialog.findViewById(R.id.destination_print_shop);
        destinationParking = (ImageButton) dialog.findViewById(R.id.destination_parking);
        destinationAirport = (ImageButton) dialog.findViewById(R.id.destination_airport);
        destinationTrainStation = (ImageButton) dialog.findViewById(R.id.destination_train_station);
        destinationBusStation = (ImageButton) dialog.findViewById(R.id.destination_bus_station);
        destinationSubwayStation = (ImageButton) dialog.findViewById(R.id.destination_subway_station);
        destinationTram = (ImageButton) dialog.findViewById(R.id.destination_tram);

        destinationDance=(ImageButton) dialog.findViewById(R.id.destination_dance);
        destinationProtest=(ImageButton) dialog.findViewById(R.id.destination_protest);
        destinationMovie=(ImageButton) dialog.findViewById(R.id.destination_movie);
        destinationConference=(ImageButton) dialog.findViewById(R.id.destination_conference);
        destinationYoga=(ImageButton) dialog.findViewById(R.id.destination_yoga);

        destinationDance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationProtest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationConference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationYoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        closeDestinationIconDialogButton = (Button) dialog.findViewById(R.id.close_destination_icon_dialog_button);
        closeDestinationIconDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        destinationParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationAirport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);

            }
        });
        destinationTrainStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);

            }
        });
        destinationBusStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);

            }
        });
        destinationSubwayStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);

            }
        });
        destinationTram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);

            }
        });
        destinationMall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);


            }
        });

        destinationBookStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationConvenienceStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationLiquorStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationLaundry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationPrintShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationGrocery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });


        destinationPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });


        destinationHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationUniversity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationMuseum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationStadium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationBeautySalon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationDinning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationPizza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        destinationGaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationFootBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationBicycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                destinationIconClickListener(view);
            }
        });
        destinationWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                destinationIconClickListener(view);
            }
        });
        destinationSwimming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationSpa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationGym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationDrinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationCasino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationZoo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationBowlingAlley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationAmusementPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationAquarium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationNightClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });
        destinationWorship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                destinationIconClickListener(view);
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.setCanceledOnTouchOutside(true);

        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        dialog.show();


    }

    public void loadDestinationIcon() {
        if (iconResourceId != -1) {
            switch (iconResourceId) {
                case 1: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_walking);
                    break;
                }
                case 2: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_swimming);
                    break;
                }
                case 3: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_spa);
                    break;
                }
                case 4: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_gym);
                    break;
                }
                case 5: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_drinks);
                    break;
                }
                case 6: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_casino);
                    break;
                }
                case 7: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination__zoo);
                    break;
                }
                case 8: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_amusement_park);
                    break;
                }
                case 9: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_bowling_alley);
                    break;
                }
                case 10: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_aquarium);
                    break;
                }
                case 11: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_night_club);
                    break;
                }
                case 12: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_running);
                    break;
                }
                case 13: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_football);
                    break;
                }
                case 14: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_gaming);
                    break;
                }
                case 15: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_bicycle);
                    break;
                }
                case 16: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_cafe);
                    break;
                }
                case 17: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_restaurant);
                    break;
                }
                case 18: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_dinning);
                    break;
                }
                case 19: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_pizza);
                    break;
                }
                case 20: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_hotel);
                    break;
                }
                case 21: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_university);
                    break;
                }
                case 23: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_library);
                    break;
                }
                case 24: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_museum);
                    break;
                }
                case 25: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_beauty_salon);
                    break;
                }
                case 26: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_school);
                    break;
                }
                case 27: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_home);
                    break;
                }
                case 28: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_stadium);
                    break;
                }
                case 29: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_park);
                    break;
                }
                case 30: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_pharmacy);
                    break;
                }
                case 31: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_hospital);
                    break;
                }
                case 32: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_worship);
                    break;
                }
                case 33: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_mall);
                    break;
                }
                case 34: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_book_store);
                    break;
                }
                case 35: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_convenience_store);
                    break;
                }
                case 36: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_liquor_store);
                    break;
                }
                case 37: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_laundry);
                    break;
                }
                case 38: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_print_shop);
                    break;
                }
                case 39: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_grocery_store);
                    break;
                }
                case 40: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_parking);
                    break;
                }
                case 41: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_airport);
                    break;
                }
                case 42: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_train_station);
                    break;
                }
                case 43: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_bus_station);
                    break;
                }
                case 44: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_subway_station);
                    break;
                }
                case 45: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_icon_tram);
                    break;
                }
                case 46: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_dance);
                    break;
                }
                case 47: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_protest);
                    break;
                }
                case 48: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_conference);
                    break;
                }
                case 49: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_yoga);
                    break;
                }
                case 50: {
                    dLocationIconImageButton.setImageResource(R.drawable.destination_movie);
                    break;
                }
            }
        }
    }

    public void destinationIconClickListener(View v) {
        InitIcon destinationIconInit = new InitIcon();
        iconResourceId = destinationIconInit.initDestinationIconResourceID(v.getId());
        showDLocationView();
    }

    void showDLocationView() {
        if (iconResourceId != -1) {
            loadDestinationIcon();
            dIconButton.setVisibility(View.INVISIBLE);
            dLocationIconLinearLayout.setVisibility(View.VISIBLE);
            dLocationIconImageButton.setVisibility(View.VISIBLE);
            dLocationIconImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDestinationIconDialog();
                }
            });
        }
    }


    // generates a new temporary event ID (since event is not created and stored in Firebase database)
    void generate(int count) {
        count = ++count;
        eventId = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null) + "-" + count;
        userIdTextView.setText(eventId);
    }

    // callback for place picker dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latLng = place.getLatLng();
                String locationDesc = place.getName().toString();
                switch (locationPreference) {
                    case 2:
                        eventInfo.setdLocation(String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
                        eventInfo.setdLocationDesc(locationDesc);
                        if (!(locationDesc.isEmpty())) {
                            dLocation.setVisibility(View.INVISIBLE);
                            dLocationLinearLayout.setVisibility(View.VISIBLE);


                        }
                        break;
                }
            }
        }
    }

    // to call the placepicker dialog
    void placePickerDialog() {
        builder = new PlacePicker.IntentBuilder();
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // toast the reason why we need the permission
                    Alerter.create(CreateEvent.this)
                            .setText("TISY uses GPS to locate and track users. It required permission to use your GPS.")
                            .setBackgroundColor(R.color.colorAccent)
                            .show();
                }
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                showPermissionAlert();

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
    void showPermissionAlert(){
        Alerter.create(this)
                .setTitle("Enable location permission")
                .setText("TISY uses GPS to locate and track users. It required permission to use your GPS.")
                .setBackgroundColor(R.color.colorPrimaryDark)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings();
                    }
                })
                .show();
    }


    void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
    }

    // updates the user's event info in Firebase's userDetails database
    void updateEvent() {
        firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
        Map<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("eventCount", ++eventCount);
        firebase.updateChildren(updateInfo, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Map<String, Object> activeEventsMap = new HashMap<>();
                activeEventsMap.put(eventId, "created");
                firebase.child("activeEvent").updateChildren(activeEventsMap, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Constants.currentEventId = eventId;

                        // to set if the current user is the event admin
                        Constants.eventAdmin = true;
                        Constants.dIconResourceId = iconResourceId;
                        // TODO: include a completion listener and update the eventCount for the user

                        storeMapConfig();
                    }
                });
                // public static String currentEventId to reference the eventID of the currenly active event

            }
        });

    }

    void storeMapConfig() {
        SharedPreferences mapStylePreference = getSharedPreferences(SharedPreferencesName.MAP_CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor mapStyleEditor = mapStylePreference.edit();
        int style = mapStylePreference.getInt("style", 0);
        if (!(style == 0)) {
            mapStyleEditor.putInt("style", Constants.TYPE_MAP_STYLE_AUBERGINE);
        }
        int type = mapStylePreference.getInt("type", 0);
        if (!(type == 0)) {
            mapStyleEditor.putInt("type", Constants.TYPE_MAP_SATELLITE);
        }
        mapStyleEditor.apply();

        storeEventInDatabase();

    }
    void storeEventInDatabase(){
        SQLiteDatabaseConnection sqLiteDatabaseConnection = new SQLiteDatabaseConnection(this);
        long count = sqLiteDatabaseConnection.insertRow(eventId, 0);
        if(count < 0){

        }else{
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        checkForCount();
    }

    void next() {

        intent = new Intent(CreateEvent.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

        intent = new Intent(CreateEvent.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
