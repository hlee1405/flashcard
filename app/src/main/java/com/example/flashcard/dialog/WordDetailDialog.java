package com.example.flashcard.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.flashcard.R;
import com.example.flashcard.model.Word;

public class WordDetailDialog extends Dialog {
    
    private Word word;
    
    public WordDetailDialog(@NonNull Context context, Word word) {
        super(context);
        this.word = word;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_word_detail);
        
        TextView tvWordTitle = findViewById(R.id.tvWordTitle);
        TextView tvPronunciation = findViewById(R.id.tvPronunciation);
        TextView tvExample = findViewById(R.id.tvExample);
        TextView tvMemoryTip = findViewById(R.id.tvMemoryTip);
        Button btnClose = findViewById(R.id.btnClose);
        
        if (word != null && !TextUtils.isEmpty(word.getEnglish())) {
            tvWordTitle.setText(word.getEnglish());
        }
        
        if (word != null && !TextUtils.isEmpty(word.getPronunciation())) {
            tvPronunciation.setText("[" + word.getPronunciation() + "]");
            tvPronunciation.setVisibility(View.VISIBLE);
        } else {
            tvPronunciation.setVisibility(View.GONE);
        }
        
        if (word != null && !TextUtils.isEmpty(word.getExample())) {
            tvExample.setText(word.getExample());
            tvExample.setVisibility(View.VISIBLE);
        } else {
            tvExample.setVisibility(View.GONE);
        }
        
        if (word != null && !TextUtils.isEmpty(word.getMemoryTip())) {
            tvMemoryTip.setText(word.getMemoryTip());
            tvMemoryTip.setVisibility(View.VISIBLE);
        } else {
            tvMemoryTip.setVisibility(View.GONE);
        }
        
        btnClose.setOnClickListener(v -> dismiss());
    }
}


