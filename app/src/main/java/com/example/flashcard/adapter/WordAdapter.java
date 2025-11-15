package com.example.flashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private final List<Word> wordList;
    private final OnSpeakerClickListener speakerClickListener;
    private final OnItemLongClickListener longClickListener;
    private final OnItemClickListener itemClickListener;

    public interface OnSpeakerClickListener {
        void onSpeakerClick(String word);
    }
    
    public interface OnItemLongClickListener {
        void onItemLongClick(Word word, int position);
    }
    
    public interface OnItemClickListener {
        void onItemClick(Word word);
    }

    public WordAdapter(List<Word> wordList, OnSpeakerClickListener listener) {
        this.wordList = wordList;
        this.speakerClickListener = listener;
        this.longClickListener = null;
        this.itemClickListener = null;
    }
    
    public WordAdapter(List<Word> wordList, OnSpeakerClickListener listener, OnItemLongClickListener longClickListener) {
        this.wordList = wordList;
        this.speakerClickListener = listener;
        this.longClickListener = longClickListener;
        this.itemClickListener = null;
    }
    
    public WordAdapter(List<Word> wordList, OnSpeakerClickListener listener, OnItemLongClickListener longClickListener, OnItemClickListener itemClickListener) {
        this.wordList = wordList;
        this.speakerClickListener = listener;
        this.longClickListener = longClickListener;
        this.itemClickListener = itemClickListener;
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
        
        holder.tvVietnamese.setText(word.getVietnamese());
        
        holder.tvEnglish.setText(word.getEnglish());
        
        if (word.getPronunciation() != null && !word.getPronunciation().isEmpty()) {
            holder.tvPronunciation.setText("[" + word.getPronunciation() + "]");
            holder.tvPronunciation.setVisibility(View.VISIBLE);
        } else {
            holder.tvPronunciation.setVisibility(View.GONE);
        }
        
        // Hide example and memory tip - they will be shown in popup dialog
        holder.tvExample.setVisibility(View.GONE);
        holder.tvMemoryTip.setVisibility(View.GONE);
        
        if (holder.speakerButtonContainer != null) {
            holder.speakerButtonContainer.setOnClickListener(v -> {
                if (speakerClickListener != null) {
                    speakerClickListener.onSpeakerClick(word.getEnglish());
                }
            });
        } else {
            holder.ivSpeaker.setOnClickListener(v -> {
                if (speakerClickListener != null) {
                    speakerClickListener.onSpeakerClick(word.getEnglish());
                }
            });
        }
        
        // Handle item click to show detail dialog
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(word);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(word, position);
                return true;
            }
            return false;
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
        TextView tvExample;
        TextView tvMemoryTip;
        ImageView ivSpeaker;
        CardView speakerButtonContainer;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVietnamese = itemView.findViewById(R.id.tvVietnamese);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvPronunciation = itemView.findViewById(R.id.tvPronunciation);
            tvExample = itemView.findViewById(R.id.tvExample);
            tvMemoryTip = itemView.findViewById(R.id.tvMemoryTip);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
            speakerButtonContainer = itemView.findViewById(R.id.speakerButtonContainer);
        }
    }
}

