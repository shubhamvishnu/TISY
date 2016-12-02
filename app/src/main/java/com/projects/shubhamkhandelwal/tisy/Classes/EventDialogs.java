package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.projects.shubhamkhandelwal.tisy.R;

public class EventDialogs{
    public void showDialog(Context context, int type){
        final Dialog dialog = new Dialog(context, R.style.event_dialogs);
        if(type == Constants.TYPE_ALL_EVENTS) {
            dialog.setContentView(R.layout.activity_all_events_actvity);
            showAllEventsDialog(context, dialog);
        }else if(type == Constants.TYPE_ALL_REQUESTS){
            dialog.setContentView(R.layout.activity_all_requests);
            showAllRequestsDialog(context, dialog);
        }else if(type == Constants.TYPE_RECEIVED_REQUESTS){
            dialog.setContentView(R.layout.dialog_received_requests_layout);
            showReceviedRequests(context, dialog);
        }else if(type == Constants.TYPE_REQUESTS){
            dialog.setContentView(R.layout.dialog_requests_layout);
            showRequests(context, dialog);
        }

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);


    }
    void showRequests(final Context context, final Dialog dialog){
        Button joinRequestButton = (Button) dialog.findViewById(R.id.dialog_join_request_button);


        // received requests

        RecyclerView receivedRequestRecyclerView;
        ReceivedRequestsRecyclerViewAdapter receivedRequestsRecyclerViewAdapter;

        receivedRequestRecyclerView= (RecyclerView) dialog.findViewById(R.id.dialog_received_requests_recycler_view);
        receivedRequestRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        receivedRequestRecyclerView.setLayoutManager(linearLayoutManager);

        receivedRequestsRecyclerViewAdapter = new ReceivedRequestsRecyclerViewAdapter(context);
        receivedRequestRecyclerView.setAdapter(receivedRequestsRecyclerViewAdapter);

        // sent requests

        RecyclerView joinEventRequestsRecyclerView;
        JoinEventRequestsRecyclerViewAdapter joinEventRequestsRecyclerViewAdapter;

        joinEventRequestsRecyclerView= (RecyclerView) dialog.findViewById(R.id.dialog_sent_requests_recycler_view);
        joinEventRequestsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManagerSentRequests = new LinearLayoutManager(dialog.getContext());
        joinEventRequestsRecyclerView.setLayoutManager(linearLayoutManagerSentRequests);

        joinEventRequestsRecyclerViewAdapter = new JoinEventRequestsRecyclerViewAdapter(context);
        joinEventRequestsRecyclerView.setAdapter(joinEventRequestsRecyclerViewAdapter);


        joinRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                joinRequestDialog(context);
            }
        });
    }
    void joinRequestDialog(Context context){

        final EditText searchEditText;
        final ImageButton searchButton;
        final Dialog searchOptionDialog = new Dialog(context, R.style.event_info_dialog_style);
        searchOptionDialog.setContentView(R.layout.dialog_join_request_layout);
        final RecyclerView joinRequestSearchResultRecyclerView = (RecyclerView) searchOptionDialog.findViewById(R.id.join_request_dialog_recycler_view);
        searchEditText = (EditText) searchOptionDialog.findViewById(R.id.join_request_dialog_search_edit_text);
        searchButton = (ImageButton) searchOptionDialog.findViewById(R.id.join_request_dialog_search_image_button);

        joinRequestSearchResultRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(searchOptionDialog.getContext());
        joinRequestSearchResultRecyclerView.setLayoutManager(linearLayoutManager);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameSearch = searchEditText.getText().toString();
                if (!nameSearch.isEmpty()) {
                    JoinRequestSearchResultRecyclerViewAdapter joinRequestSearchResultRecyclerViewAdapter = new JoinRequestSearchResultRecyclerViewAdapter(searchOptionDialog.getContext(), nameSearch);
                    joinRequestSearchResultRecyclerView.setAdapter(joinRequestSearchResultRecyclerViewAdapter);
                }
            }
        });


        Window window = searchOptionDialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        searchOptionDialog.setCanceledOnTouchOutside(true);
        searchOptionDialog.show();


    }
    void showReceviedRequests(Context context, final Dialog dialog){
        ImageButton exitButton;
        RecyclerView receivedRequestRecyclerView;
        ReceivedRequestsRecyclerViewAdapter receivedRequestsRecyclerViewAdapter;
        exitButton = (ImageButton) dialog.findViewById(R.id.received_requests_back_arrow_image_button);
        receivedRequestRecyclerView= (RecyclerView) dialog.findViewById(R.id.dialog_received_request_recycler_view);
        receivedRequestRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        receivedRequestRecyclerView.setLayoutManager(linearLayoutManager);

        receivedRequestsRecyclerViewAdapter = new ReceivedRequestsRecyclerViewAdapter(context);
        receivedRequestRecyclerView.setAdapter(receivedRequestsRecyclerViewAdapter);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    void showAllRequestsDialog(Context context, final Dialog dialog) {
        ImageButton exitImageButton;
        RecyclerView joinEventRequestsRecyclerView;
        JoinEventRequestsRecyclerViewAdapter joinEventRequestsRecyclerViewAdapter;
        exitImageButton = (ImageButton) dialog.findViewById(R.id.all_requests_back_arrow_image_button);
        joinEventRequestsRecyclerView= (RecyclerView) dialog.findViewById(R.id.user_requests_recycler_view);
        joinEventRequestsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        joinEventRequestsRecyclerView.setLayoutManager(linearLayoutManager);

        joinEventRequestsRecyclerViewAdapter = new JoinEventRequestsRecyclerViewAdapter(context);
        joinEventRequestsRecyclerView.setAdapter(joinEventRequestsRecyclerViewAdapter);
        exitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }
    void showAllEventsDialog(Context context, final Dialog dialog){
        ImageButton exitImageButton;
        RecyclerView activeEventsRecyclerView;
        ActiveEventsRecyclerViewAdapter activeEventsRecyclerViewAdapter;
        exitImageButton = (ImageButton) dialog.findViewById(R.id.all_events_back_arrow_image_button);
        activeEventsRecyclerView= (RecyclerView) dialog.findViewById(R.id.active_events_recycler_view);
        activeEventsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        activeEventsRecyclerView.setLayoutManager(linearLayoutManager);

        activeEventsRecyclerViewAdapter = new ActiveEventsRecyclerViewAdapter(context);
        activeEventsRecyclerView.setAdapter(activeEventsRecyclerViewAdapter);

        exitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}