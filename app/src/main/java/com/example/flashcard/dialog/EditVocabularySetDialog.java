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

public class EditVocabularySetDialog extends Dialog {
    
    private OnVocabularySetUpdatedListener listener;
    private OnVocabularySetDeletedListener deleteListener;
    private EditText etTitle;
    private VocabularyDataManager dataManager;
    private VocabularySet vocabularySet;
    
    public interface OnVocabularySetUpdatedListener {
        void onVocabularySetUpdated(VocabularySet set);
    }
    
    public interface OnVocabularySetDeletedListener {
        void onVocabularySetDeleted(VocabularySet set);
    }
    
    public EditVocabularySetDialog(@NonNull Context context, VocabularySet vocabularySet, 
                                   OnVocabularySetUpdatedListener listener,
                                   OnVocabularySetDeletedListener deleteListener) {
        super(context);
        this.vocabularySet = vocabularySet;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.dataManager = new VocabularyDataManager(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_vocabulary_set);
        
        etTitle = findViewById(R.id.etVocabularySetTitle);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        
        etTitle.setText(vocabularySet.getTitle());
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Vui lòng nhập tên bộ từ vựng!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            VocabularySet updatedSet = new VocabularySet(title, vocabularySet.getJsonFileName(), vocabularySet.getWordCount());
            dataManager.updateVocabularySet(updatedSet);
            
            if (listener != null) {
                listener.onVocabularySetUpdated(updatedSet);
            }
            
            Toast.makeText(getContext(), "Đã cập nhật bộ từ vựng!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ từ vựng '" + vocabularySet.getTitle() + "'? Tất cả từ vựng trong bộ này cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dataManager.deleteVocabularySet(vocabularySet.getJsonFileName());
                    
                    if (deleteListener != null) {
                        deleteListener.onVocabularySetDeleted(vocabularySet);
                    }
                    
                    Toast.makeText(getContext(), "Đã xóa bộ từ vựng!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}





