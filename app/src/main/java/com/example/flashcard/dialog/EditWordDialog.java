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

public class EditWordDialog extends Dialog {
    
    private OnWordUpdatedListener listener;
    private OnWordDeletedListener deleteListener;
    private EditText etEnglish, etVietnamese, etPronunciation;
    private String jsonFileName;
    private VocabularyDataManager dataManager;
    private Word word;
    
    public interface OnWordUpdatedListener {
        void onWordUpdated(Word word);
    }
    
    public interface OnWordDeletedListener {
        void onWordDeleted(Word word);
    }
    
    public EditWordDialog(@NonNull Context context, String jsonFileName, Word word,
                         OnWordUpdatedListener listener,
                         OnWordDeletedListener deleteListener) {
        super(context);
        this.jsonFileName = jsonFileName;
        this.word = word;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.dataManager = new VocabularyDataManager(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_word);
        
        etEnglish = findViewById(R.id.etEnglish);
        etVietnamese = findViewById(R.id.etVietnamese);
        etPronunciation = findViewById(R.id.etPronunciation);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        
        etEnglish.setText(word.getEnglish());
        etVietnamese.setText(word.getVietnamese());
        if (word.getPronunciation() != null) {
            etPronunciation.setText(word.getPronunciation());
        }
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
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
            
            Word updatedWord = new Word(english, vietnamese, pronunciation);
            dataManager.updateWordInSet(jsonFileName, word, updatedWord);
            
            if (listener != null) {
                listener.onWordUpdated(updatedWord);
            }
            
            Toast.makeText(getContext(), "Đã cập nhật từ vựng!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa từ vựng '" + word.getEnglish() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dataManager.deleteWordFromSet(jsonFileName, word);
                    
                    if (deleteListener != null) {
                        deleteListener.onWordDeleted(word);
                    }
                    
                    Toast.makeText(getContext(), "Đã xóa từ vựng!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}













