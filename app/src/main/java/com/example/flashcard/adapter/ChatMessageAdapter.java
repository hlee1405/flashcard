package com.example.flashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    
    private final List<ChatMessage> messages;
    
    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getSender() == ChatMessage.Sender.USER ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_ai, parent, false);
            return new AIMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).tvMessage.setText(message.getContent());
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).tvMessage.setText(message.getContent());
        }
    }
    
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
    
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
    
    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        public AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}

