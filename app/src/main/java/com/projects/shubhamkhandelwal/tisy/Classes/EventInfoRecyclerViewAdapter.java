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
    Dialog dialog;
    private LayoutInflater inflator;


    public EventInfoRecyclerViewAdapter(Context context, Dialog dialog, List<String> memberList, List<String> memberCoordinate, List<String> memberProfileImageUrl, List<String> memberProfileName) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.dialog = dialog;
        this.memberList = memberList;
        this.memberCoordinate = memberCoordinate;
        this.memberProfileImageUrl = memberProfileImageUrl;
        this.memberProfileName = memberProfileName;
    }

    @Override
    public EventInfoRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_event_info_members_row_layout, parent, false);
        EventInfoRecyclerViewHolder viewHolder = new EventInfoRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventInfoRecyclerViewHolder holder, int position) {

        holder.memberTextView.setText(memberList.get(position));
        holder.coordinateSnapShotTextView.setText(memberCoordinate.get(position));
        holder.nameTextView.setText(memberProfileName.get(position));
        Picasso.with(context).load(Uri.parse(memberProfileImageUrl.get(position))).error(R.drawable.default_profile_image_icon).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }
void showTracks(int position){
    dialog.dismiss();
    Intent intent = new Intent(context, TrackActivity.class);
    intent.putExtra("username", memberList.get(position));
    context.startActivity(intent);
}
    class EventInfoRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView memberTextView;
        TextView coordinateSnapShotTextView;
        TextView nameTextView;


        CircleImageView profileImage;

        public EventInfoRecyclerViewHolder(View itemView) {
            super(itemView);
            memberTextView = (TextView) itemView.findViewById(R.id.message_text_view_recycler_view);
            coordinateSnapShotTextView = (TextView) itemView.findViewById(R.id.coordinate_snapshot_text_view_recycler_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view_recycler_view);
            profileImage = (CircleImageView) itemView.findViewById(R.id.profileImageCircleImageView);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (memberCoordinate.get(getPosition()).equals("0.0,0.0")) {
                    }else{
                        showTracks(getPosition());
                    }
                }
            });
        }
    }
}
