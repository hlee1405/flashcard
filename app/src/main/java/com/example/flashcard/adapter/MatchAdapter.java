package com.example.flashcard.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.MatchCard;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private final List<MatchCard> cardList;
    private final OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    public MatchAdapter(List<MatchCard> cardList, OnCardClickListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_card, parent, false);
        return new MatchViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchCard card = cardList.get(position);
        holder.textView.setText(card.getText());

        if (card.isMatched()) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else if (card.isCorrectPair()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#66BB6A"));
            holder.itemView.setVisibility(View.VISIBLE);
        } else if (card.isWrongPair()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#EF5350"));
            holder.itemView.setVisibility(View.VISIBLE);
        } else if (card.isFlipped()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#BDBDBD"));
            holder.itemView.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!card.isMatched() && !card.isCorrectPair()) {
                listener.onCardClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textView;
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.match_card_view);
            textView = itemView.findViewById(R.id.tvMatchText);
        }
    }
}
