package com.example.flashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private final List<Word> wordList;
    private final OnSpeakerClickListener speakerClickListener;

    public interface OnSpeakerClickListener {
        void onSpeakerClick(String word);
    }

    public WordAdapter(List<Word> wordList, OnSpeakerClickListener listener) {
        this.wordList = wordList;
        this.speakerClickListener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        
        // Hiển thị từ tiếng Việt
        holder.tvVietnamese.setText(word.getVietnamese());
        
        // Hiển thị từ tiếng Anh
        holder.tvEnglish.setText(word.getEnglish());
        
        // Hiển thị phiên âm
        if (word.getPronunciation() != null && !word.getPronunciation().isEmpty()) {
            holder.tvPronunciation.setText("[" + word.getPronunciation() + "]");
            holder.tvPronunciation.setVisibility(View.VISIBLE);
        } else {
            holder.tvPronunciation.setVisibility(View.GONE);
        }
        
        // Xử lý click vào speaker icon
        holder.ivSpeaker.setOnClickListener(v -> {
            if (speakerClickListener != null) {
                speakerClickListener.onSpeakerClick(word.getEnglish());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList != null ? wordList.size() : 0;
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvVietnamese;
        TextView tvEnglish;
        TextView tvPronunciation;
        ImageView ivSpeaker;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVietnamese = itemView.findViewById(R.id.tvVietnamese);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvPronunciation = itemView.findViewById(R.id.tvPronunciation);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
        }
    }
}

