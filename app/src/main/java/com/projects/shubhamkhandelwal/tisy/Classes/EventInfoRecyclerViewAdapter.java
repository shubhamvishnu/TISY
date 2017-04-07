package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.shubhamkhandelwal.tisy.R;
import com.projects.shubhamkhandelwal.tisy.TrackActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shubham Khandelwal on 9/7/2016.
 */
public class EventInfoRecyclerViewAdapter extends RecyclerView.Adapter<EventInfoRecyclerViewAdapter.EventInfoRecyclerViewHolder> {
    Context context;
    List<String> memberList;
    List<String> memberCoordinate;
    List<String> memberProfileImageUrl;
    List<String> memberProfileName;
    List<String> lastSeen;



    private LayoutInflater inflator;


    public EventInfoRecyclerViewAdapter(Context context, List<String> memberList, List<String> memberCoordinate, List<String> memberProfileImageUrl, List<String> memberProfileName, List<String> lastSeen) {
        this.context = context;
        inflator = LayoutInflater.from(context);

        this.memberList = memberList;
        this.memberCoordinate = memberCoordinate;
        this.memberProfileImageUrl = memberProfileImageUrl;
        this.memberProfileName = memberProfileName;
        this.lastSeen = lastSeen;
    }

    @Override
    public EventInfoRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_event_info_members_row_layout, parent, false);
        EventInfoRecyclerViewHolder viewHolder = new EventInfoRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final EventInfoRecyclerViewHolder holder,final int position) {
        holder.memberTextView.setText(memberList.get(position));
        holder.coordinateSnapShotTextView.setText(memberCoordinate.get(position));
        holder.nameTextView.setText(memberProfileName.get(position));
        Picasso.with(context).load(Uri.parse(memberProfileImageUrl.get(position))).networkPolicy(NetworkPolicy.OFFLINE).into(holder.profileImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(context).load(Uri.parse(memberProfileImageUrl.get(position))).error(R.drawable.default_profile_image_icon).into(holder.profileImage);
            }
        });


        holder.lastSeenTextView.setText(lastSeen.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }


    class EventInfoRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView memberTextView;
        TextView coordinateSnapShotTextView;
        TextView nameTextView;
        CircleImageView profileImage;
        TextView lastSeenTextView;

        public EventInfoRecyclerViewHolder(View itemView) {
            super(itemView);
            memberTextView = (TextView) itemView.findViewById(R.id.message_text_view_recycler_view);
            coordinateSnapShotTextView = (TextView) itemView.findViewById(R.id.coordinate_snapshot_text_view_recycler_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view_recycler_view);
            profileImage = (CircleImageView) itemView.findViewById(R.id.profileImageCircleImageView);
            lastSeenTextView = (TextView) itemView.findViewById(R.id.last_seen_text_view_recycler_view);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
