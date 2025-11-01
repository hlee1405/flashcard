// File: app/src/main/java/com/example/yourpackage/adapter/VocabularySetAdapter.java
package com.example.flashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.VocabularySet;

import java.util.List;

public class VocabularySetAdapter extends RecyclerView.Adapter<VocabularySetAdapter.SetViewHolder> {

    private final List<VocabularySet> vocabularySets;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(VocabularySet set);
    }

    public VocabularySetAdapter(List<VocabularySet> vocabularySets, OnItemClickListener listener) {
        this.vocabularySets = vocabularySets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        VocabularySet set = vocabularySets.get(position);
        holder.tvSetTitle.setText(set.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(set));
    }

    @Override
    public int getItemCount() {
        return vocabularySets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvSetTitle;
        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetTitle = itemView.findViewById(R.id.tvSetTitle);
        }
    }
}
