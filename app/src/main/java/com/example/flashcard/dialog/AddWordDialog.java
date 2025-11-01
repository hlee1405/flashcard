package com.example.flashcard.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.flashcard.R;
import com.example.flashcard.model.Word;
import com.example.flashcard.util.VocabularyDataManager;

public class AddWordDialog extends Dialog {
    
    private OnWordAddedListener listener;
    private EditText etEnglish, etVietnamese, etPronunciation;
    private String jsonFileName;
    private VocabularyDataManager dataManager;
    
    public interface OnWordAddedListener {
        void onWordAdded(Word word);
    }
    
    public AddWordDialog(@NonNull Context context, String jsonFileName, OnWordAddedListener listener) {
        super(context);
        this.jsonFileName = jsonFileName;
        this.listener = listener;
        this.dataManager = new VocabularyDataManager(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_word);
        
        etEnglish = findViewById(R.id.etEnglish);
        etVietnamese = findViewById(R.id.etVietnamese);
        etPronunciation = findViewById(R.id.etPronunciation);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnAdd = findViewById(R.id.btnAdd);
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String english = etEnglish.getText().toString().trim();
            String vietnamese = etVietnamese.getText().toString().trim();
            String pronunciation = etPronunciation.getText().toString().trim();
            
            if (TextUtils.isEmpty(english)) {
                Toast.makeText(getContext(), "Vui lòng nhập từ tiếng Anh!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(vietnamese)) {
                Toast.makeText(getContext(), "Vui lòng nhập nghĩa tiếng Việt!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Word newWord = new Word(english, vietnamese, pronunciation);
            dataManager.addWordToSet(jsonFileName, newWord);
            
            if (listener != null) {
                listener.onWordAdded(newWord);
            }
            
            Toast.makeText(getContext(), "Đã thêm từ vựng mới!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}

