package com.example.flashcard.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;

import com.example.flashcard.BuildConfig;
import com.example.flashcard.R;
import com.example.flashcard.model.VocabularySet;
import com.example.flashcard.model.Word;
import com.example.flashcard.util.GPTApiService;
import com.example.flashcard.util.VocabularyDataManager;

import java.util.List;

public class AIVocabularyDialog extends Dialog {

    private final OnVocabularyGeneratedListener listener;
    private EditText etTopic;
    private EditText etWordCount;
    private EditText etInterests;
    private Spinner spinnerStyle;
    private android.view.View loadingContainer;
    private ContentLoadingProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvLoadingSubtext;
    private Button btnGenerate;
    private Button btnCancel;
    private final VocabularyDataManager dataManager;
    private final GPTApiService gptApiService;
    private final String configApiKey;
    private static final String PREFS_NAME = "flashcard_prefs";
    private static final String KEY_INTERESTS = "user_interests";
    private static final String KEY_STYLE = "user_style";
    private ProgressDialog loadingDialog;
    
    public interface OnVocabularyGeneratedListener {
        void onVocabularyGenerated(VocabularySet set, List<Word> words);
    }
    
    public AIVocabularyDialog(@NonNull Context context, OnVocabularyGeneratedListener listener) {
        super(context);
        this.listener = listener;
        this.dataManager = new VocabularyDataManager(context);
        this.gptApiService = new GPTApiService();
        this.configApiKey = BuildConfig.OPENAI_API_KEY;
    }
    
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ai_vocabulary);

        etTopic = findViewById(R.id.etTopic);
        etWordCount = findViewById(R.id.etWordCount);
        etInterests = findViewById(R.id.etInterests);
        spinnerStyle = findViewById(R.id.spinnerStyle);
        loadingContainer = findViewById(R.id.loadingContainer);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup Spinner với các phong cách
        String[] styles = {"Mặc định", "Đơn giản", "Hài hước", "Học thuật", "Trẻ em"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, styles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStyle.setAdapter(adapter);

        // Load saved preferences
        loadPreferences();

        btnCancel.setOnClickListener(v -> dismiss());

        btnGenerate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(configApiKey)) {
                Toast.makeText(getContext(), getContext().getString(R.string.missing_api_key_config), Toast.LENGTH_SHORT).show();
                return;
            }

            String topic = etTopic.getText().toString().trim();
            String wordCountStr = etWordCount.getText().toString().trim();

            if (TextUtils.isEmpty(topic)) {
                Toast.makeText(getContext(), getContext().getString(R.string.please_enter_topic), Toast.LENGTH_SHORT).show();
                return;
            }

            int wordCount;
            try {
                wordCount = Integer.parseInt(wordCountStr);
                if (wordCount <= 0 || wordCount > 50) {
                    Toast.makeText(getContext(), getContext().getString(R.string.word_count_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_word_count), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get interests and style
            String interests = etInterests.getText().toString().trim();
            String style = spinnerStyle.getSelectedItem().toString();
            
            // Save preferences
            savePreferences(interests, style);

            // Start generation
            generateVocabulary(configApiKey, topic, wordCount, interests, style);
        });
    }
    
    private void generateVocabulary(String apiKey, String topic, int wordCount, String interests, String style) {
        // Đóng dialog tạo từ vựng
        dismiss();
        
        // Hiển thị loading dialog riêng
        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setMessage("Đang tạo từ vựng...\nVui lòng đợi trong giây lát...");
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        
        gptApiService.setApiKey(apiKey);
        gptApiService.generateVocabulary(topic, wordCount, interests, style, new GPTApiService.GPTResponseCallback() {
            @Override
            public void onSuccess(List<Word> words) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Đóng loading dialog
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    
                    if (words == null || words.isEmpty()) {
                        Toast.makeText(getContext(), getContext().getString(R.string.no_vocabulary_created), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Create vocabulary set
                    String fileName = "ai_" + System.currentTimeMillis() + ".json";
                    String title = getContext().getString(R.string.ai_prefix, topic);
                    VocabularySet newSet = new VocabularySet(title, fileName, words.size());
                    dataManager.addVocabularySet(newSet);
                    dataManager.saveWordsForSet(fileName, words);
                    
                    String successMessage = getContext().getString(R.string.vocabulary_created_success, words.size());
                    Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                    
                    if (listener != null) {
                        listener.onVocabularyGenerated(newSet, words);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Đóng loading dialog
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    
                    String errorMessage = getContext().getString(R.string.error_prefix, error);
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Lỗi: " + error;
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void savePreferences(String interests, String style) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_INTERESTS, interests);
        editor.putString(KEY_STYLE, style);
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedInterests = prefs.getString(KEY_INTERESTS, "");
        String savedStyle = prefs.getString(KEY_STYLE, "Mặc định");
        
        if (!savedInterests.isEmpty()) {
            etInterests.setText(savedInterests);
        }
        
        // Set spinner to saved style
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerStyle.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(savedStyle);
            if (position >= 0) {
                spinnerStyle.setSelection(position);
            }
        }
    }
}

