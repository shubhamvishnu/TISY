package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.client.Firebase;
import com.projects.shubhamkhandelwal.tisy.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 10/20/2016.
 */
public class UserStatusOptionRecyclerViewAdapter extends RecyclerView.Adapter<UserStatusOptionRecyclerViewAdapter.UserStatusOptionRecyclerViewHolder> {
    Context context;
    LayoutInflater inflator;
    List<String> status;

    public UserStatusOptionRecyclerViewAdapter(Context context, List<String> status) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.status = status;

    }

    @Override
    public UserStatusOptionRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_user_status_option_row_layout, parent, false);
        UserStatusOptionRecyclerViewHolder viewHolder = new UserStatusOptionRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserStatusOptionRecyclerViewHolder holder, int position) {
        holder.userStatusRecyclerViewButton.setText(status.get(position));
    }

    @Override
    public int getItemCount() {
        return status.size();
    }
    void updateUserStatus(String status){
        Firebase updateStatusFirebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + context.getSharedPreferences(SharedPreferencesName.USER_DETAILS, Context.MODE_PRIVATE).getString("username", null));
        HashMap<String, Object> statusUpdateMap = new HashMap<>();
        statusUpdateMap.put("status", status);
        updateStatusFirebase.updateChildren(statusUpdateMap);

    }

    class UserStatusOptionRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button userStatusRecyclerViewButton;

        public UserStatusOptionRecyclerViewHolder(View itemView) {
            super(itemView);
            userStatusRecyclerViewButton = (Button) itemView.findViewById(R.id.user_status_option_recycler_view_button);
            userStatusRecyclerViewButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.user_status_option_recycler_view_button: {
                    updateUserStatus(status.get(getPosition()));
                    break;

                }
            }
        }
    }
}
