package com.projects.shubhamkhandelwal.tisy.Classes;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.projects.shubhamkhandelwal.tisy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Shubham Khandelwal on 3/25/2017.
 */
public class AllNotesRecyclerViewAdapter extends RecyclerView.Adapter<AllNotesRecyclerViewAdapter.AllNotesRecyclerViewHolder> {

    Context context;
    private LayoutInflater inflator;
    List<Note> noteList;

    public AllNotesRecyclerViewAdapter(Context context, Map<Integer, Note> tagNoteMap) {
        inflator = LayoutInflater.from(context);
        this.context = context;
        noteList = new ArrayList<>();

        init(tagNoteMap);
    }
    void init(Map<Integer, Note> tagNoteMap){
        for(Map.Entry<Integer, Note> entry: tagNoteMap.entrySet()){
            Note note = new Note();
            note.setTitle(entry.getValue().getTitle());
            note.setDesc(entry.getValue().getDesc());
            note.setLatlng(entry.getValue().getLatlng());
            note.setKey(entry.getValue().getKey());

            int position = noteList.size();
            noteList.add(note);
            notifyItemInserted(position);
        }
    }

    @Override
    public AllNotesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.recycler_view_all_notes_row_layout, parent, false);
        AllNotesRecyclerViewHolder viewHolder = new AllNotesRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AllNotesRecyclerViewHolder holder, int position) {
        holder.titleTextView.setText(noteList.get(position).getTitle());
        holder.descTextView.setText(noteList.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    public class AllNotesRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descTextView;

        public AllNotesRecyclerViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.all_notes_title_text_view);
            descTextView = (TextView) itemView.findViewById(R.id.all_notes_desc_text_view);

        }
    }
}
