package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.shubhamkhandelwal.tisy.R;

import java.util.List;

/**
 * Created by Shubham Khandelwal on 1/1/2017.
 */
public class ActiveMembersRecyclerViewAdapter extends RecyclerView.Adapter<ActiveMembersRecyclerViewAdapter.ActiveMemberRecyclerViewHolder> {
    List<String> memberList;
    Context context;
    private LayoutInflater inflator;

    public ActiveMembersRecyclerViewAdapter(Context context, List<String> memberList) {
        inflator = LayoutInflater.from(context);
        this.context = context;
        this.memberList = memberList;
    }

    @Override
    public ActiveMemberRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_active_event_members_row_layout, parent, false);
        ActiveMemberRecyclerViewHolder viewHolder = new ActiveMemberRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActiveMemberRecyclerViewHolder holder, int position) {
        holder.memberTextView.setText(memberList.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ActiveMemberRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView memberTextView;
        public ActiveMemberRecyclerViewHolder(View itemView) {
            super(itemView);
            memberTextView = (TextView) itemView.findViewById(R.id.active_event_members_text_view);
        }
    }
}
