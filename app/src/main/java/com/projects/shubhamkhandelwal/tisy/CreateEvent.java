package com.projects.shubhamkhandelwal.tisy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatDrawableManager;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.projects.shubhamkhandelwal.tisy.Classes.Constants;
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfo;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InternetConnectionService;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;

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
    Button sLocation;
    Button dLocation;
    Button createEventButton;
    Button dIconButton;
    EditText sLocationDescEditText;
    EditText dLocationDescEditText;
    EditText descriptionEditText;
    TextView userIdTextView;
    ImageButton sLocationEditImageButton;
    ImageButton dLocationEditImageButton;
    ImageButton dLocationIconImageButton;
    Intent intent;
    CoordinatorLayout coordinatorLayout;
    Map<String, Object> members;

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
    LinearLayout sLocationLinearLayout;
    LinearLayout dLocationLinearLayout;
    LinearLayout dLocationIconLinearLayout;
    Bitmap destinationIconBitmap;

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(42,
                42, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, 42, 42);
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Firebase context
        Firebase.setAndroidContext(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutCreateEvent);

        // Object to store the event informations
        eventInfo = new EventInfo();

        // intializing the members List object
        members = new HashMap<>();

        // intializing the view elements
        sLocation = (Button) findViewById(R.id.slocation);
        dLocation = (Button) findViewById(R.id.dlocation);
        createEventButton = (Button) findViewById(R.id.createEventButton);
        dIconButton = (Button) findViewById(R.id.dIconButton);

        sLocationDescEditText = (EditText) findViewById(R.id.sLocationDescEditText);
        dLocationDescEditText = (EditText) findViewById(R.id.dLocationDescEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        userIdTextView = (TextView) findViewById(R.id.userId);
        sLocationEditImageButton = (ImageButton) findViewById(R.id.editSLocationImageButton);
        dLocationEditImageButton = (ImageButton) findViewById(R.id.editDLocationImageButton);
        dLocationIconImageButton = (ImageButton) findViewById(R.id.dLocationIconImageButton);
        sLocationLinearLayout = (LinearLayout) findViewById(R.id.sLocationLinearLayout);
        dLocationLinearLayout = (LinearLayout) findViewById(R.id.dLocationLinearLayout);
        dLocationIconLinearLayout = (LinearLayout) findViewById(R.id.dLocationIconLinearLayout);


        sLocationLinearLayout.setVisibility(View.INVISIBLE);
        dLocationLinearLayout.setVisibility(View.INVISIBLE);
        dLocationIconLinearLayout.setVisibility(View.INVISIBLE);

        // inaitalizing eventId
        eventId = new String();
        iconResourceId = -1;
        startService(new Intent(getBaseContext(), InternetConnectionService.class));


        // generate the eventId; calls the generate() function
        generateEventId();

        sLocationEditImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationPreference = 1;
                placePickerDialog();
            }
        });

        dLocationEditImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationPreference = 2;
                placePickerDialog();
            }
        });
        sLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sLocation.setBackgroundColor(Color.parseColor("#26FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        sLocation.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                }
                return false;
            }
        });
        dLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dLocation.setBackgroundColor(Color.parseColor("#26FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        dLocation.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                }
                return false;
            }
        });
        dIconButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dIconButton.setBackgroundColor(Color.parseColor("#26FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        dIconButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                }
                return false;
            }
        });
        dIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDestinationIconDialog();


            }
        });
        // TODO: change the locationPreference value intialization using switch case (optional)
        sLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationPreference = 1;
                placePickerDialog();
            }
        });

        dLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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


        createEventButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        createEventButton.setBackgroundColor(Color.parseColor("#26FFFFFF"));
                        break;
                    case MotionEvent.ACTION_UP:
                        createEventButton.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                        break;
                }
                return false;
            }
        });
        // creating event in firebase
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String destLocation = eventInfo.getdLocation();
                String destLocationDesc = eventInfo.getdLocationDesc();
                String startLocation = eventInfo.getsLocation();
                String startLocationDesc = eventInfo.getsLocationDesc();
                String eventDescription = descriptionEditText.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    if (!eventId.isEmpty()) {
                        if (destLocation == null || startLocation == null || startLocationDesc == null || destLocationDesc == null || destLocation.isEmpty() || startLocation.isEmpty() || startLocationDesc.isEmpty() || destLocationDesc.isEmpty() || iconResourceId == -1 || eventDescription == null || eventDescription.isEmpty()) {
                            Toast.makeText(CreateEvent.this, "please mention all the event details", Toast.LENGTH_SHORT).show();
                        } else {

                            if (sLocationDescEditText.getText().toString().isEmpty() || dLocationDescEditText.getText().toString().isEmpty()) {
                                sLocationDescEditText.setText(eventInfo.getsLocationDesc());
                                dLocationDescEditText.setText(eventInfo.getdLocationDesc());
                            } else {
                                eventInfo.setsLocationDesc(sLocationDescEditText.getText().toString());
                                eventInfo.setdLocationDesc(dLocationDescEditText.getText().toString());
                            }
                            // TODO: check for eventID being empty

                            if (!checkInternetConnection()) {
                                Toast.makeText(CreateEvent.this, "no internet connection", Toast.LENGTH_SHORT).show();
                            } else {

                                // creating reference with the new eventID
                                firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + eventId);
                                Map<String, Object> newEvent = new HashMap<String, Object>();
                                newEvent.put("admin", getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null));
                                newEvent.put("info", eventInfo);
                                newEvent.put("members", members);
                                newEvent.put("dIcon", iconResourceId);
                                newEvent.put("desc", eventDescription);

                                firebase.setValue(newEvent, new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        updateEvent();
                                    }
                                });
                            }


                        }

                    } else {
                        Toast.makeText(CreateEvent.this, "some problem has occured. please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
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
        ImageButton destinationWalking, destinationSwimming, destinationSpa, destinationGym, destinationFootBall, destinationBicycle, destinationRunning;
        ImageButton destinationDrinks, destinationCasino, destinationZoo, destinationBowlingAlley, destinationAmusementPark, destinationAquarium, destinationNightClub, destinationGaming;
        ImageButton destinationCafe, destinationRestaurant, destinationDinning, destinationPizza;
        ImageButton destinationHotel, destinationUniversity, destinationLibrary, destinationMuseum, destinationStadium, destinationSchool, destinationBeautySalon, destinationHome, destinationPark;
        ImageButton destinationPharmacy, destinationHospital, destinationWorship;
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
        dialog.show();


    }

    public void loadDestinationIcon() {
        if (iconResourceId != -1) {
            switch (Constants.dIconResourceId) {
                case 1: {

                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_walking);
                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 2: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_swimming);
                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 3: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_spa);
                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 4: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_gym);
                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 5: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_drinks);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 6: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_casino);




                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 7: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination__zoo);

                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 8: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_amusement_park);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 9: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bowling_alley);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 10: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_aquarium);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 11: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_night_club);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 12: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_running);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;

                }
                case 13: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_football);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 14: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_gaming);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 15: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_bicycle);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 16: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_cafe);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 17: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_restaurant);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 18: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_dinning);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 19: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pizza);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 20: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hotel);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 21: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_university);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 23: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_library);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 24: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_museum);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 25: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_beauty_salon);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 26: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_school);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 27: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_home);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 28: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_stadium);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 29: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_park);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 30: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_pharmacy);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;

                }

                case 31: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_hospital);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;

                }
                case 32: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_worship);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 33: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_mall);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 34: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_book_store);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 35: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_convenience_store);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 36: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_liquor_store);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 37: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_laundry);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 38: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_print_shop);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }
                case 39: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_grocery_store);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 40: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_parking);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 41: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_airport);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 42: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_train_station);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 43: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_bus_station);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 44: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_subway_station);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }

                case 45: {
                    destinationIconBitmap = getBitmapFromVectorDrawable(this, R.drawable.destination_icon_tram);


                    dLocationIconImageButton.setImageBitmap(destinationIconBitmap);
                    break;
                }


            }
        }

    }

    public void destinationIconClickListener(View v) {

        switch (v.getId()) {
            case R.id.destination_walking: {
                iconResourceId = 1;
                break;
            }
            case R.id.destination_swimming: {
                iconResourceId = 2;
                break;
            }
            case R.id.destination_spa: {
                iconResourceId = 3;

                break;
            }
            case R.id.destination_gym: {
                iconResourceId = 4;

                break;
            }
            case R.id.destination_drinks: {
                iconResourceId = 5;

                break;
            }
            case R.id.destination_casino: {
                iconResourceId = 6;

                break;
            }
            case R.id.destination_zoo: {
                iconResourceId = 7;

                break;
            }
            case R.id.destination_amusement_park: {
                iconResourceId = 8;
                break;
            }
            case R.id.destination_bowling_alley: {
                iconResourceId = 9;
                break;
            }
            case R.id.destination_aquarium: {
                iconResourceId = 10;
                break;
            }
            case R.id.destination_night_club: {
                iconResourceId = 11;
                break;
            }
            case R.id.destination_running: {
                iconResourceId = 12;
                break;

            }
            case R.id.destination_football: {
                iconResourceId = 13;
                break;
            }
            case R.id.destination_gaming: {
                iconResourceId = 14;
                break;
            }
            case R.id.destination_bicycler: {
                iconResourceId = 15;
                break;
            }
            case R.id.destination_cafe: {
                iconResourceId = 16;
                break;
            }
            case R.id.destination_restaurant: {
                iconResourceId = 17;
                break;
            }
            case R.id.destination_dinning: {
                iconResourceId = 18;
                break;
            }
            case R.id.destination_pizza: {
                iconResourceId = 19;
                break;
            }

            case R.id.destination_hotel: {
                iconResourceId = 20;
                break;
            }
            case R.id.destination_university: {
                iconResourceId = 21;
                break;
            }
            case R.id.destination_library: {
                iconResourceId = 23;
                break;
            }
            case R.id.destination_museum: {
                iconResourceId = 24;
                break;
            }
            case R.id.destination_beauty_salon: {
                iconResourceId = 25;
                break;
            }
            case R.id.destination_school: {
                iconResourceId = 26;
                break;
            }
            case R.id.destination_home: {
                iconResourceId = 27;
                break;
            }
            case R.id.destination_stadium: {
                iconResourceId = 28;
                break;
            }
            case R.id.destination_park: {
                iconResourceId = 29;
                break;
            }
            case R.id.destination_pharmacy: {
                iconResourceId = 30;
                break;

            }

            case R.id.destination_hospital: {
                iconResourceId = 31;
                break;

            }
            case R.id.destination_worship: {
                iconResourceId = 32;
                break;
            }
            case R.id.destination_mall: {
                iconResourceId = 33;
                break;
            }

            case R.id.destination_book_store: {
                iconResourceId = 34;
                break;
            }
            case R.id.destination_convenience_store: {
                iconResourceId = 35;
                break;
            }
            case R.id.destination_liquor_store: {
                iconResourceId = 36;
                break;
            }
            case R.id.destination_laundry: {
                iconResourceId = 37;
                break;
            }
            case R.id.destination_print_shop: {
                iconResourceId = 38;
                break;
            }
            case R.id.destination_grocery_store: {
                iconResourceId = 39;
                break;
            }

            case R.id.destination_parking: {
                iconResourceId = 40;
                break;
            }

            case R.id.destination_airport: {
                iconResourceId = 41;
                break;
            }

            case R.id.destination_train_station: {
                iconResourceId = 42;
                break;
            }

            case R.id.destination_bus_station: {
                iconResourceId = 43;
                break;
            }

            case R.id.destination_subway_station: {
                iconResourceId = 44;
                break;
            }

            case R.id.destination_tram: {
                iconResourceId = 45;
                break;
            }

        }
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

    /*void storeDestinationIconFirebase(Drawable drawable) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        //  StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://fir-trio.appspot.com");
        StorageReference imageStorageReference = firebaseStorage.getReferenceFromUrl("gs://fir-trio.appspot.com/" + eventId + "/dIcon");
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageStorageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(CreateEvent.this, "destination icon uploaded successfully.", Toast.LENGTH_SHORT).show();
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
               // iconResourceId = downloadUrl.toString();
            }
        });


    }*/

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
                    case 1:
                        eventInfo.setsLocation(String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
                        eventInfo.setsLocationDesc(locationDesc);
                        if (!(locationDesc.isEmpty())) {

                            sLocation.setVisibility(View.INVISIBLE);
                            // show the start location description text
                            sLocationLinearLayout.setVisibility(View.VISIBLE);
                            sLocationDescEditText.setText(eventInfo.getsLocationDesc());

                        }
                        break;
                    case 2:
                        eventInfo.setdLocation(String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
                        eventInfo.setdLocationDesc(locationDesc);
                        if (!(locationDesc.isEmpty())) {

                            dLocation.setVisibility(View.INVISIBLE);
                            dLocationLinearLayout.setVisibility(View.VISIBLE);
                            // show the destination location description text
                            dLocationDescEditText.setText(eventInfo.getdLocationDesc());


                        }
                        break;
                }
            }
        }
    }

    // to call the placepicker dialog
    void placePickerDialog() {
        builder = new PlacePicker.IntentBuilder();
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
                    // TODO: toast the reason why we need the permission
                    Toast.makeText(CreateEvent.this, "location access permission is needed to give you the best experience", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // TODO: show snackbar android
                //TODO: start from here tom
                showSnackBar();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    void showSnackBar() {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "enable location permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings();
                    }
                });

        snackbar.setActionTextColor(Color.parseColor("#F7BF8E"));
        snackbar.show();
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

                        next();
                    }
                });
                // public static String currentEventId to reference the eventID of the currenly active event

            }
        });

    }

    void next() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        intent = new Intent(CreateEvent.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
        intent = new Intent(CreateEvent.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), InternetConnectionService.class));
    }
}
