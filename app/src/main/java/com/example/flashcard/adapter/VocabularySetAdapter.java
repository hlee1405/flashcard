package com.example.flashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.model.VocabularySet;

import java.util.ArrayList;
import java.util.List;

public class VocabularySetAdapter extends RecyclerView.Adapter<VocabularySetAdapter.SetViewHolder> {

    private List<VocabularySet> displayedSets; // Danh sách đang hiển thị (sẽ được lọc)
    private final List<VocabularySet> allSets; // Danh sách gốc đầy đủ
    private final OnItemClickListener listener;
    private final OnPlayButtonClickListener playButtonListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(VocabularySet set);
    }

    public interface OnPlayButtonClickListener {
        void onPlayButtonClick(VocabularySet set);
    }
    
    public interface OnItemLongClickListener {
        void onItemLongClick(VocabularySet set);
    }

    public VocabularySetAdapter(List<VocabularySet> vocabularySets, OnItemClickListener listener, OnPlayButtonClickListener playButtonListener) {
        // Lưu trữ danh sách gốc và khởi tạo danh sách hiển thị
        this.allSets = new ArrayList<>(vocabularySets);
        this.displayedSets = vocabularySets;
        this.listener = listener;
        this.playButtonListener = playButtonListener;
        this.longClickListener = null;
    }
    
    public VocabularySetAdapter(List<VocabularySet> vocabularySets, OnItemClickListener listener, OnPlayButtonClickListener playButtonListener, OnItemLongClickListener longClickListener) {
        // Lưu trữ danh sách gốc và khởi tạo danh sách hiển thị
        this.allSets = new ArrayList<>(vocabularySets);
        this.displayedSets = vocabularySets;
        this.listener = listener;
        this.playButtonListener = playButtonListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        // Sử dụng displayedSets cho việc hiển thị
        VocabularySet set = displayedSets.get(position);
        holder.tvSetTitle.setText(set.getTitle());
        
        // Hiển thị word count
        holder.tvWordCount.setText(set.getWordCount() + " từ");
        
        // Xử lý click vào item (không phải play button)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(set);
            }
        });
        
        // Xử lý long click vào item
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(set);
                return true;
            }
            return false;
        });
        
        // Xử lý click vào play button
        holder.playButtonContainer.setOnClickListener(v -> {
            if (playButtonListener != null) {
                playButtonListener.onPlayButtonClick(set);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedSets.size(); // Trả về kích thước của danh sách đang hiển thị
    }

    /**
     * Lọc danh sách bộ từ vựng dựa trên từ khóa tìm kiếm.
     * @param text Từ khóa tìm kiếm.
     */
    public void filterList(String text) {
        displayedSets = new ArrayList<>();
        if (text.isEmpty()) {
            // Nếu không có từ khóa, hiển thị toàn bộ danh sách
            displayedSets.addAll(allSets);
        } else {
            // Lọc danh sách dựa trên tiêu đề
            text = text.toLowerCase().trim();
            for (VocabularySet item : allSets) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    displayedSets.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvSetTitle;
        TextView tvWordCount;
        View playButtonContainer;
        
        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetTitle = itemView.findViewById(R.id.tvSetTitle);
            tvWordCount = itemView.findViewById(R.id.tvWordCount);
            playButtonContainer = itemView.findViewById(R.id.playButtonContainer);
        }
    }
}