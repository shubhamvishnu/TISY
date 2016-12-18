package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.projects.shubhamkhandelwal.tisy.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Shubham Khandelwal on 12/18/2016.
 */
public class EventMembersRecyclerViewAdapater extends RecyclerView.Adapter<EventMembersRecyclerViewAdapater.EventMembersRecyclerViewHolder> {
    Context context;
    String eventID;
    List<String> memberList;
    List<String> memberProfileImageUrl;
    List<String> memberProfileName;
    Firebase firebase;
    private LayoutInflater inflator;
    public EventMembersRecyclerViewAdapater(Context context, List<String> memberList) {
        this.context = context;
        inflator = LayoutInflater.from(context);

        this.memberList = memberList;
        this.memberProfileImageUrl =new ArrayList<>();
        this.memberProfileName = new ArrayList<>();
        init();
    }
    void init(){
        for(int i = 0; i < memberList.size(); i++){
            firebase = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + memberList.get(i));
            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int position = memberProfileName.size();
                    memberProfileImageUrl.add(dataSnapshot.child("userPhotoUri").getValue().toString());
                    memberProfileName.add(dataSnapshot.child("name").getValue().toString());
                    notifyItemInserted(position);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public EventMembersRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_event_members_row_layout, parent, false);
        EventMembersRecyclerViewHolder viewHolder = new EventMembersRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventMembersRecyclerViewHolder holder, int position) {
        holder.memberTextView.setText(memberList.get(position));
        holder.nameTextView.setText(memberProfileName.get(position));
        Picasso.with(context).load(Uri.parse(memberProfileImageUrl.get(position))).error(R.drawable.default_profile_image_icon).into(holder.profileImage);

    }

    @Override
    public int getItemCount() {
        return memberProfileName.size();
    }
void removeMember(final int position){
    Firebase removeMemberFromEvent = new Firebase(FirebaseReferences.FIREBASE_ALL_EVENT_DETAILS + Constants.currentEventId + "/members/" + memberList.get(position) );
    removeMemberFromEvent.removeValue(new Firebase.CompletionListener() {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            Firebase removeCurrentUser = new Firebase(FirebaseReferences.FIREBASE_USER_DETAILS + memberList.get(position)+ "/activeEvent/" + Constants.currentEventId);
            removeCurrentUser.removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    memberList.remove(position);
                    memberProfileImageUrl.remove(position);
                    memberProfileName.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    });

}
    class EventMembersRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView memberTextView;
        TextView nameTextView;
        CircleImageView profileImage;
        ImageButton memberRemoveImageButton;

        public EventMembersRecyclerViewHolder(View itemView) {
            super(itemView);
            memberTextView = (TextView) itemView.findViewById(R.id.message_text_view_members_recycler_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view_members_recycler_view);
            profileImage = (CircleImageView) itemView.findViewById(R.id.membersProfileImageCircleImageView);
            memberRemoveImageButton = (ImageButton) itemView.findViewById(R.id.member_remove_image_button);
            memberRemoveImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMember(getPosition());
                }
            });
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
