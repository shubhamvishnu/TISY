package com.projects.shubhamkhandelwal.tisy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewForLocationActivity extends FragmentActivity
        implements OnStreetViewPanoramaReadyCallback {
    LatLng latLng = null;
    String locationDesc;
    // intialization for place picker dialog
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder;
    LinearLayout streetViewUnavailableLinearLayout;
    LinearLayout streetViewAvailableLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view_for_location);
        streetViewUnavailableLinearLayout = (LinearLayout) findViewById(R.id.streetview_unavailable_linear_layout);
        streetViewAvailableLinearLayout = (LinearLayout) findViewById(R.id.streetview_available_linear_layout);
        placePickerDialog();


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

    // callback for place picker dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                locationDesc = new String();
                Place place = PlacePicker.getPlace(data, this);
                latLng = place.getLatLng();
                locationDesc = place.getName().toString();
                initStreetView();
            }
        }
    }

    void initStreetView() {

        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streeview_activity_streetviewpanorama);
        streetViewPanoramaFragment.getView().setBackgroundColor(Color.WHITE);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(latLng);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StreetViewForLocationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
