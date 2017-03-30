package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import com.projects.shubhamkhandelwal.tisy.Classes.EventInfoRecyclerViewAdapter;
import com.projects.shubhamkhandelwal.tisy.Classes.EventMembersRecyclerViewAdapater;
import com.projects.shubhamkhandelwal.tisy.Classes.FirebaseReferences;
import com.projects.shubhamkhandelwal.tisy.Classes.InitIcon;
import com.projects.shubhamkhandelwal.tisy.Classes.SharedPreferencesName;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EventInfoActivity extends FragmentActivity {

    public static final int PLACE_PICKER_REQUEST = 1; // used for the place picker intent

    LinearLayout destinationLocationIconLinearLayout;
    TextView eventIdDialogTextView;

    TextView destLocationDialogTextView;
    TextView eventDescriptionTextView;
    TextView timeStampTextView;
    TextView titleTextView;
    RecyclerView eventInfoMembersRecyclerView;
    ImageButton editMembersImageButton;
    Button editDestinationLocationButton;
    Button editDescriptionButton;
    ImageButton dLocationIconImageButton;
    Button dIconEditButton;
    String username;
    Firebase firebase;
    PlacePicker.IntentBuilder builder;
    List<String> membersList; // members (username) in the event
    List<String> memberCoordinate; // coordinates (LatLng) of the members in the event
    List<String> memberProfileImageUrls; // profile Image URL of every member in the event
    List<String> lastSeenInfo;
    List<String> memberProfileName; // member name for users in the event
    String timeStamp; // date and time of when the event was created
    String eventTitle; // title of the event

    String destLocationTextView; // destination location description of the event
    String eventDescription; // event description
    int memberUriCount; // number of URL's fetched of the members

    List<String> eventMemberList;

    boolean editDestinationLocation;
    int iconResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        iconResourceId = -1;
        username = getSharedPreferences(SharedPreferencesName.USER_DETAILS, MODE_PRIVATE).getString("username", null);

        initInfo();
    }

    void initInfo() {
        editDestinationLocation = false;
        membersList = new ArrayList<>();
        memberCoordinate = new ArrayList<>();
        timeStamp = new String();
        eventTitle = new String();
        iconResourceId = -1;
        firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.child("info").getChildren()) {
                    if (snapshot.getKey().equals("dLocationDesc")) {
                        destLocationTextView = snapshot.getValue().toString();
                    }
                }
                eventDescription = dataSnapshot.child("desc").getValue().toString();
                timeStamp = dataSnapshot.child("time").getValue().toString();
                eventTitle = dataSnapshot.child("title").getValue().toString();
                iconResourceId = Integer.parseInt(dataSnapshot.child("dIcon").getValue().toString());

                for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                    membersList.add(snapshot.getKey());
                    memberCoordinate.add(snapshot.getValue().toString());
                }
                //showEventInfoDialog();
                loadProfileInfo();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    void loadProfileInfo() {
        lastSeenInfo = new ArrayList<>();
        memberProfileImageUrls = new ArrayList<>();
        memberProfileName = new ArrayList<>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching event details for you!");
        progressDialog.setCancelable(false);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                initEventInfo();
            }
        });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        progressDialog.show();
        memberUriCount = 0;
        for (String name : membersList) {
            firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + name);
            firebase.keepSynced(true);
            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ++memberUriCount;
                    memberProfileImageUrls.add(dataSnapshot.child("userPhotoUri").getValue().toString());
                    memberProfileName.add(dataSnapshot.child("name").getValue().toString());
                    lastSeenInfo.add(dataSnapshot.child("lastSeen").getValue().toString());
                    if (membersList.size() == memberUriCount) {
                        progressDialog.dismiss();

                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }


    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                showDestinationLocationChangeDialog(place.getLatLng());

            }
        }
    }

    void showDestinationLocationChangeDialog(final LatLng latLng) {
        final Dialog destinationLocationChangeDialog = new Dialog(this, R.style.event_info_dialog_style);
        destinationLocationChangeDialog.setContentView(R.layout.dialog_edit_destination_location_layout);
        final EditText editDestinationEditText = (EditText) destinationLocationChangeDialog.findViewById(R.id.edit_destination_edit_text);
        ImageButton cancelEditDestinationImageButton = (ImageButton) destinationLocationChangeDialog.findViewById(R.id.cancel_edit_destination_image_button);
        ImageButton confirmEditDestinationImageButton = (ImageButton) destinationLocationChangeDialog.findViewById(R.id.confirm_edit_destination_button);

        cancelEditDestinationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationLocationChangeDialog.dismiss();
                initEventInfo();
            }
        });
        confirmEditDestinationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInternetConnection()) {
                    final String destinationDesc = editDestinationEditText.getText().toString();
                    if (!(destinationDesc == null || destinationDesc.isEmpty())) {
                        Firebase editDestinationFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/info");
                        editDestinationFirebase.keepSynced(true);
                        Map<String, Object> editLocationMap = new HashMap<String, Object>();
                        editLocationMap.put("dLocation", String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));
                        editLocationMap.put("dLocationDesc", destinationDesc);
                        editDestinationFirebase.updateChildren(editLocationMap, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                destinationLocationChangeDialog.dismiss();
                                initInfo();
                            }
                        });
                    } else {
                        Toast.makeText(EventInfoActivity.this, "enter a destination title", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNoInternetConnectionAlert();
                }
            }
        });

        Window window = destinationLocationChangeDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        destinationLocationChangeDialog.setCanceledOnTouchOutside(true);
        destinationLocationChangeDialog.show();

    }

    void showNoInternetConnectionAlert() {
        Alerter.create(this)
                .setText("Oops! No internet connection")
                .setBackgroundColor(R.color.colorAccent)
                .show();
    }

    boolean checkInternetConnection() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    void initEventInfo() {


        destinationLocationIconLinearLayout = (LinearLayout) findViewById(R.id.dLocation_event_info_icon_linear_layout);
        eventIdDialogTextView = (TextView) findViewById(R.id.event_id_info_text_view);

        destLocationDialogTextView = (TextView) findViewById(R.id.dest_location_desc_text_view);
        eventDescriptionTextView = (TextView) findViewById(R.id.event_desc_text_view);
        timeStampTextView = (TextView) findViewById(R.id.time_stamp_text_view);
        titleTextView = (TextView) findViewById(R.id.event_title_text_view);

        editDestinationLocationButton = (Button) findViewById(R.id.edit_destination_location_button);
        editDescriptionButton = (Button) findViewById(R.id.edit_description_button);
        dLocationIconImageButton = (ImageButton) findViewById(R.id.dLocation_event_info_icon_image_button);
        dIconEditButton = (Button) findViewById(R.id.edit_destination_event_info_icon_button);


        editMembersImageButton = (ImageButton) findViewById(R.id.editMembersImageButton);
        if (!Constants.eventAdmin) {
            dIconEditButton.setVisibility(View.GONE);
            editDescriptionButton.setVisibility(View.GONE);
            editMembersImageButton.setVisibility(View.GONE);
            editDestinationLocationButton.setVisibility(View.GONE);
        }

        editDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDescriptionDialog();
            }
        });
        dIconEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInternetConnection()) {
                    showDestinationIconDialog();
                } else {
                    showNoInternetConnectionAlert();
                }
            }
        });

        if (iconResourceId != -1) {
            loadDestinationIcon();
        }

        eventInfoMembersRecyclerView = (RecyclerView) findViewById(R.id.members_recycler_view);
        eventInfoMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        titleTextView.setText(eventTitle);
        eventIdDialogTextView.setText(Constants.currentEventId);


        destLocationDialogTextView.setText(destLocationTextView);
        eventDescriptionTextView.setText(eventDescription);
        timeStampTextView.setText(timeStamp);

        eventInfoMembersRecyclerView.setHasFixedSize(true);
        EventInfoRecyclerViewAdapter adapter = new EventInfoRecyclerViewAdapter(this, membersList, memberCoordinate, memberProfileImageUrls, memberProfileName, lastSeenInfo);
        eventInfoMembersRecyclerView.setAdapter(adapter);

        editDestinationLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager manager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    placePickerDialog();
                } else {
                    showGPSAlert();
                }

            }
        });

        editMembersImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //    new EventDialogs().showDialog(MapsActivity.this, Constants.TYPE_DELETE_MEMBERS);
                showMembersDialog();
            }
        });
    }

    void showDestinationIconDialog() {
        ImageButton destinationWalking, destinationSwimming, destinationSpa, destinationGym, destinationFootBall, destinationBicycle, destinationRunning, destinationDance, destinationProtest;
        ImageButton destinationDrinks, destinationCasino, destinationZoo, destinationBowlingAlley, destinationAmusementPark, destinationAquarium, destinationNightClub, destinationGaming, destinationMovie;
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
        dialog.show();


    }

    public void destinationIconClickListener(View v) {
        InitIcon destinationIconInit = new InitIcon();
        iconResourceId = destinationIconInit.initDestinationIconResourceID(v.getId());


        if (iconResourceId != -1) {
            Firebase updateDestinationIconFirebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
            updateDestinationIconFirebase.keepSynced(true);
            Map<String, Object> dIconMap = new HashMap<>();
            dIconMap.put("dIcon", iconResourceId);
            updateDestinationIconFirebase.updateChildren(dIconMap, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    Constants.dIconResourceId = iconResourceId;
                    loadDestinationIcon();
                }
            });


        }
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

    void showEditDescriptionDialog() {
        final Dialog descriptionChangeDialog = new Dialog(this, R.style.event_info_dialog_style);
        descriptionChangeDialog.setContentView(R.layout.dialog_edit_description_layout);
        final EditText editDestinationEditText = (EditText) descriptionChangeDialog.findViewById(R.id.edit_description_edit_text);
        ImageButton cancelEditDescriptionImageButton = (ImageButton) descriptionChangeDialog.findViewById(R.id.cancel_edit_description_image_button);
        ImageButton confirmEditDescriptionImageButton = (ImageButton) descriptionChangeDialog.findViewById(R.id.confirm_edit_description_button);

        cancelEditDescriptionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descriptionChangeDialog.dismiss();
            }
        });
        confirmEditDescriptionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInternetConnection()) {
                    final String description = editDestinationEditText.getText().toString();
                    if (description == null || description.isEmpty()) {
                        Toast.makeText(EventInfoActivity.this, "Write a description", Toast.LENGTH_SHORT).show();
                    } else {
                        Firebase changeDescription = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId);
                        changeDescription.keepSynced(true);
                        Map<String, Object> editDescriptionMap = new HashMap<String, Object>();
                        editDescriptionMap.put("desc", description);
                        changeDescription.updateChildren(editDescriptionMap, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (descriptionChangeDialog.isShowing()) {
                                    descriptionChangeDialog.dismiss();
                                }
                                Toast.makeText(EventInfoActivity.this, "Event description changed", Toast.LENGTH_SHORT).show();
                                initInfo();
                            }
                        });
                    }
                } else {
                    descriptionChangeDialog.dismiss();
                    showNoInternetConnectionAlert();
                }
            }
        });

        Window window = descriptionChangeDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        descriptionChangeDialog.setCanceledOnTouchOutside(true);
        descriptionChangeDialog.show();
    }

    void showGPSAlert() {
        Alerter.create(this)
                .setTitle("Turn on GPS")
                .setText("TISY uses GPS to locate and track users.")
                .setBackgroundColor(R.color.colorAccent)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGPSSettings();
                    }
                })
                .show();
    }

    void openGPSSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    void showMembersDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("fetching event members!");
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
        progressDialog.show();
        eventMemberList = new ArrayList<>();
        Firebase firebase = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members");
        firebase.keepSynced(true);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!Objects.equals(snapshot.getKey(), username)) {
                            eventMemberList.add(snapshot.getKey());
                        }
                    }
                    progressDialog.dismiss();
                    initDeleteEventMemberRecyclerView(eventMemberList);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    void initDeleteEventMemberRecyclerView(List<String> members) {
        final Dialog dialog = new Dialog(this, R.style.event_dialogs);
        dialog.setContentView(R.layout.dialog_delete_event_members_layout);
        RecyclerView deleteEventMemberRecyclerView;
        EventMembersRecyclerViewAdapater eventMembersRecyclerViewAdapater;

        deleteEventMemberRecyclerView = (RecyclerView) dialog.findViewById(R.id.delete_event_members_recycler_view);
        deleteEventMemberRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        deleteEventMemberRecyclerView.setLayoutManager(linearLayoutManager);

        eventMembersRecyclerViewAdapater = new EventMembersRecyclerViewAdapater(this, members);
        deleteEventMemberRecyclerView.setAdapter(eventMembersRecyclerViewAdapater);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                initInfo();
            }
        });
        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EventInfoActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
