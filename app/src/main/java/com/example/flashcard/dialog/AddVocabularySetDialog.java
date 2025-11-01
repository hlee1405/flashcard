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
import com.example.flashcard.model.VocabularySet;
import com.example.flashcard.util.VocabularyDataManager;

public class AddVocabularySetDialog extends Dialog {
    
    private OnVocabularySetAddedListener listener;
    private EditText etTitle;
    private VocabularyDataManager dataManager;
    
    public interface OnVocabularySetAddedListener {
        void onVocabularySetAdded(VocabularySet set);
    }
    
    public AddVocabularySetDialog(@NonNull Context context, OnVocabularySetAddedListener listener) {
        super(context);
        this.listener = listener;
        this.dataManager = new VocabularyDataManager(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_vocabulary_set);
        
        etTitle = findViewById(R.id.etVocabularySetTitle);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnAdd = findViewById(R.id.btnAdd);
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Vui lòng nhập tên bộ từ vựng!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Tạo tên file JSON duy nhất
            String fileName = "user_" + System.currentTimeMillis() + ".json";
            
            VocabularySet newSet = new VocabularySet(title, fileName, 0);
            dataManager.addVocabularySet(newSet);
            
            if (listener != null) {
                listener.onVocabularySetAdded(newSet);
            }
            
            Toast.makeText(getContext(), "Đã thêm bộ từ vựng mới!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}

