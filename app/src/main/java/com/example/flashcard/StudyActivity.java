package com.example.flashcard;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flashcard.model.Word;
import com.example.flashcard.util.VocabularyDataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudyActivity extends AppCompatActivity {

    private List<Word> wordList;
    private int currentWordIndex = 0;
    private VocabularyDataManager dataManager;

    private TextView tvEnglish, tvVietnamese, tvPronunciation;
    private Button btnPrevious, btnNext;
    private ImageButton btnSpeak;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        dataManager = new VocabularyDataManager(this);

        View mainContent = findViewById(R.id.mainContent);
        View navigationButtons = findViewById(R.id.navigation_buttons);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            
            if (mainContent != null) {
                float density = getResources().getDisplayMetrics().density;
                int paddingTopDp = (int) (20 * density);
                mainContent.setPadding(
                    mainContent.getPaddingLeft(),
                    paddingTopDp + top,
                    mainContent.getPaddingRight(),
                    mainContent.getPaddingBottom()
                );
            }
            
            if (navigationButtons != null) {
                float density = getResources().getDisplayMetrics().density;
                int paddingBottomDp = (int) (16 * density);
                navigationButtons.setPadding(
                    navigationButtons.getPaddingLeft(),
                    navigationButtons.getPaddingTop(),
                    navigationButtons.getPaddingRight(),
                    paddingBottomDp + bottom
                );
            }
            return windowInsets;
        });

        String jsonFileName = getIntent().getStringExtra("JSON_FILE_NAME");
        loadWords(jsonFileName);

        initViews();
        setupTextToSpeech();
        setupClickListeners();

        if (wordList != null && !wordList.isEmpty()) {
            Collections.shuffle(wordList);
            displayCurrentWord();
        } else {
            Toast.makeText(this, "Không thể tải bộ từ vựng!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có dữ liệu
        }
    }

    private void initViews() {
        tvEnglish = findViewById(R.id.tvEnglish);
        tvVietnamese = findViewById(R.id.tvVietnamese);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSpeak = findViewById(R.id.btnSpeak);
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            } else {
                Toast.makeText(this, "Khởi tạo TextToSpeech thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> showNextWord());
        btnPrevious.setOnClickListener(v -> showPreviousWord());
        btnSpeak.setOnClickListener(v -> speakWord(tvEnglish.getText().toString()));
    }

    private void loadWords(String fileName) {
        wordList = new ArrayList<>();
        
        boolean isUserCreated = dataManager.isUserCreatedSet(fileName);
        
        if (isUserCreated) {
            wordList = dataManager.getWordsForSet(fileName);
        } else {
            try {
                InputStream is = getAssets().open(fileName);
                InputStreamReader reader = new InputStreamReader(is);
                Type listType = new TypeToken<List<Word>>() {}.getType();
                List<Word> assetWords = new Gson().fromJson(reader, listType);
                reader.close();
                is.close();
                
                if (assetWords != null) {
                    wordList.addAll(assetWords);
                }
            } catch (IOException e) {
                Log.e("StudyActivity", "Error loading words from assets", e);
            }
            
            List<Word> userWords = dataManager.getWordsForSet(fileName);
            wordList.addAll(userWords);
        }
        
        if (wordList == null) {
            wordList = new ArrayList<>();
        }
    }

    private void displayCurrentWord() {
        if (currentWordIndex >= 0 && currentWordIndex < wordList.size()) {
            Word word = wordList.get(currentWordIndex);
            tvEnglish.setText(word.getEnglish());
            tvVietnamese.setText(word.getVietnamese());

            if (word.getPronunciation() != null && !word.getPronunciation().isEmpty()) {
                tvPronunciation.setText(word.getPronunciation());
                tvPronunciation.setVisibility(View.VISIBLE);
            } else {
                tvPronunciation.setVisibility(View.GONE);
            }
        }
    }

    private void speakWord(String word) {
        if (tts != null && word != null && !word.isEmpty()) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void showNextWord() {
        currentWordIndex = (currentWordIndex + 1) % wordList.size();
        displayCurrentWord();
    }



    private void showPreviousWord() {
        if (currentWordIndex > 0) {
            currentWordIndex--;
        } else {
            currentWordIndex = wordList.size() - 1;
        }
        displayCurrentWord();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
