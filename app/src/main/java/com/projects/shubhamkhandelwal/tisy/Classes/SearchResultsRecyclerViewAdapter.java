package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 10/21/2016.
 */
public class SearchResultsRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultsRecyclerViewAdapter.SearchResultsRecyclerViewHolder> {
    Context context;
    LayoutInflater inflator;
    String name;
    List<String> nameList;
    List<String> eventIdList;

    public SearchResultsRecyclerViewAdapter(Context context, String name) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.name = name;
        eventIdList = new ArrayList<>();
        nameList = new ArrayList<>();
        populateViewWithResults();
    }

    void populateViewWithResults() {
        nameList = new ArrayList<>();
        eventIdList = new ArrayList<>();
        Firebase searchResultFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS);
        searchResultFirebase.keepSynced(true);
        searchResultFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(snapshot.getKey().contains(name) || snapshot.child("name").getValue().toString().contains(name)){
                            int position = eventIdList.size();
                            eventIdList.add(snapshot.getKey());
                            nameList.add(snapshot.child("name").getValue().toString());
                            notifyItemInserted(position);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    @Override
    public SearchResultsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_search_option_check_row_layout, parent, false);
        SearchResultsRecyclerViewHolder viewHolder = new SearchResultsRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SearchResultsRecyclerViewHolder holder, int position) {
            holder.nameTextView.setText(nameList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventIdList.size();
    }

    class SearchResultsRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView addMemberImageView;

        public SearchResultsRecyclerViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.search_option_choice_name_text_view);
            addMemberImageView = (ImageView) itemView.findViewById(R.id.search_option_add_member_image_view);

        }
    }
}
