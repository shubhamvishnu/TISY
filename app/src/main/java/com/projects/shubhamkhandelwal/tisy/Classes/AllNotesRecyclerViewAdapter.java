package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.projects.shubhamkhandelwal.tisy.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Shubham Khandelwal on 3/25/2017.
 */
public class AllNotesRecyclerViewAdapter extends RecyclerView.Adapter<AllNotesRecyclerViewAdapter.AllNotesRecyclerViewHolder> {

    Context context;
    private LayoutInflater inflator;
    Dialog dialog;
    Map<Integer, Note> tagNoteMap;

    public AllNotesRecyclerViewAdapter(Context context, Map<Integer, Note> tagNoteMap) {
        inflator = LayoutInflater.from(context);
        this.context = context;
        this.tagNoteMap = tagNoteMap;
    }


    @Override
    public AllNotesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_all_notes_row_layout, parent, false);
        AllNotesRecyclerViewHolder viewHolder = new AllNotesRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AllNotesRecyclerViewHolder holder, int position) {
        holder.titleTextView.setText(tagNoteMap.get(position).getTitle());
        holder.descTextView.setText(tagNoteMap.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return tagNoteMap.size();
    }


    public class AllNotesRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descTextView;
        ImageButton actionImageButton;
        public AllNotesRecyclerViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.all_notes_title_text_view);
            descTextView = (TextView) itemView.findViewById(R.id.all_notes_desc_text_view);

        }
    }
}
