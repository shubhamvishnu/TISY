package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

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
        }

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);


    }
    void showAllRequestsDialog(Context context, Dialog dialog){
        RecyclerView joinEventRequestsRecyclerView;
        JoinEventRequestsRecyclerViewAdapter joinEventRequestsRecyclerViewAdapter;

        joinEventRequestsRecyclerView= (RecyclerView) dialog.findViewById(R.id.user_requests_recycler_view);
        joinEventRequestsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialog.getContext());
        joinEventRequestsRecyclerView.setLayoutManager(linearLayoutManager);

        joinEventRequestsRecyclerViewAdapter = new JoinEventRequestsRecyclerViewAdapter(context);
        joinEventRequestsRecyclerView.setAdapter(joinEventRequestsRecyclerViewAdapter);

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