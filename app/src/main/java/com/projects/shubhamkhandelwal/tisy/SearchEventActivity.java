package com.projects.shubhamkhandelwal.tisy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import com.projects.shubhamkhandelwal.tisy.Classes.JoinRequestSearchResultRecyclerViewAdapter;

public class SearchEventActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);
        initSearchOption();
    }
    void initSearchOption() {

        final EditText searchEditText;
        final ImageButton searchButton, backImageButton;
        //searchOptionDialog.setContentView(R.layout.dialog_join_request_layout);
        final RecyclerView joinRequestSearchResultRecyclerView = (RecyclerView) findViewById(R.id.join_request_dialog_recycler_view);
        searchEditText = (EditText) findViewById(R.id.join_request_dialog_search_edit_text);
        searchButton = (ImageButton) findViewById(R.id.join_request_dialog_search_image_button);
        backImageButton = (ImageButton) findViewById(R.id.back_search_image_button);

        joinRequestSearchResultRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchEventActivity.this);
        joinRequestSearchResultRecyclerView.setLayoutManager(linearLayoutManager);

        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    String nameSearch = searchEditText.getText().toString();
                    if (!nameSearch.trim().isEmpty()) {
                        JoinRequestSearchResultRecyclerViewAdapter joinRequestSearchResultRecyclerViewAdapter = new JoinRequestSearchResultRecyclerViewAdapter(SearchEventActivity.this, nameSearch);
                        joinRequestSearchResultRecyclerView.setAdapter(joinRequestSearchResultRecyclerViewAdapter);
                    }
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameSearch = searchEditText.getText().toString();
                if (!nameSearch.isEmpty()) {
                    JoinRequestSearchResultRecyclerViewAdapter joinRequestSearchResultRecyclerViewAdapter = new JoinRequestSearchResultRecyclerViewAdapter(SearchEventActivity.this, nameSearch);
                    joinRequestSearchResultRecyclerView.setAdapter(joinRequestSearchResultRecyclerViewAdapter);
                }
            }
        });

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchEventActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchEventActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
