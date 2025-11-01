package com.example.flashcard;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import com.example.flashcard.adapter.WordAdapter;
import com.example.flashcard.dialog.AddWordDialog;
import com.example.flashcard.dialog.EditWordDialog;
import com.example.flashcard.model.Word;
import com.example.flashcard.util.VocabularyDataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyListActivity extends AppCompatActivity {

    private List<Word> wordList;
    private WordAdapter adapter;
    private TextToSpeech tts;
    private VocabularyDataManager dataManager;
    private String jsonFileName;
    private String categoryTitle;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_primary, null));
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        
        setContentView(R.layout.activity_vocabulary_list);

        dataManager = new VocabularyDataManager(this);
        
        // Lấy tên file JSON từ Intent
        jsonFileName = getIntent().getStringExtra("JSON_FILE_NAME");
        categoryTitle = getIntent().getStringExtra("CATEGORY_TITLE");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryTitle != null ? categoryTitle : "Từ vựng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Đảm bảo nút back có màu trắng
        if (toolbar.getNavigationIcon() != null) {
            android.graphics.drawable.Drawable navigationIcon = toolbar.getNavigationIcon();
            navigationIcon = DrawableCompat.wrap(navigationIcon);
            DrawableCompat.setTint(navigationIcon, getResources().getColor(R.color.white, null));
            toolbar.setNavigationIcon(navigationIcon);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Load từ vựng từ JSON hoặc user data
        loadWords();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewWords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WordAdapter(wordList, this::speakWord, (word, position) -> {
            // Long click để chỉnh sửa/xóa từ vựng
            showEditWordDialog(word);
        });
        recyclerView.setAdapter(adapter);
        
        // Nút thêm từ vựng
        Button btnAddWord = findViewById(R.id.btnAddWord);
        btnAddWord.setOnClickListener(v -> showAddWordDialog());

        // Xử lý window insets để toolbar không bị che, RecyclerView không bị che
        View addWordContainer = findViewById(R.id.addWordContainer);
        float density = getResources().getDisplayMetrics().density;
        int paddingTopDp = (int) (8 * density); // 8dp chuyển sang pixel
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            
            // Thêm padding top cho toolbar để kéo dài lên status bar và không bị che chữ
            toolbar.setPadding(
                toolbar.getPaddingLeft(),
                top + paddingTopDp, // Status bar height + 8dp padding
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom()
            );
            
            // Thêm padding bottom cho container nút thêm để tránh bị che bởi navigation bar
            if (addWordContainer != null) {
                int paddingBottomDp = (int) (12 * density);
                addWordContainer.setPadding(
                    addWordContainer.getPaddingLeft(),
                    addWordContainer.getPaddingTop(),
                    addWordContainer.getPaddingRight(),
                    bottom + paddingBottomDp
                );
            }
            
            // Thêm padding bottom cho RecyclerView để tránh bị che bởi navigation bar
            int paddingBottomDp = (int) (24 * density);
            recyclerView.setPadding(
                recyclerView.getPaddingLeft(),
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight(),
                bottom > 0 ? bottom + paddingBottomDp : paddingBottomDp
            );
            
            return windowInsets;
        });

        // Setup TextToSpeech
        setupTextToSpeech();
    }

    /**
     * Load từ vựng từ cả assets và user data
     */
    private void loadWords() {
        wordList = new ArrayList<>();
        
        // Kiểm tra xem có phải bộ từ vựng do user tạo không
        boolean isUserCreated = dataManager.isUserCreatedSet(jsonFileName);
        
        if (isUserCreated) {
            // Load từ user data
            wordList = dataManager.getWordsForSet(jsonFileName);
        } else {
            // Load từ assets
            try {
                InputStream is = getAssets().open(jsonFileName);
                InputStreamReader reader = new InputStreamReader(is);
                Type listType = new TypeToken<List<Word>>() {}.getType();
                List<Word> assetWords = new Gson().fromJson(reader, listType);
                reader.close();
                is.close();
                
                if (assetWords != null) {
                    wordList.addAll(assetWords);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi tải từ vựng từ assets!", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Load thêm từ user data nếu có (có thể user đã thêm từ vào bộ assets)
        if (!isUserCreated) {
            List<Word> userWords = dataManager.getWordsForSet(jsonFileName);
            wordList.addAll(userWords);
        }

        if (wordList == null || wordList.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng nào!", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Hiển thị dialog để thêm từ vựng mới
     */
    private void showAddWordDialog() {
        AddWordDialog dialog = new AddWordDialog(
            this,
            jsonFileName,
            word -> {
                // Sau khi thêm thành công, reload danh sách
                loadWords();
                adapter = new WordAdapter(wordList, this::speakWord, (w, position) -> {
                    showEditWordDialog(w);
                });
                recyclerView.setAdapter(adapter);
            }
        );
        dialog.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình này
        loadWords();
        if (adapter != null && wordList != null) {
            adapter = new WordAdapter(wordList, this::speakWord, (word, position) -> {
                showEditWordDialog(word);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(this, "Khởi tạo TextToSpeech thất bại!", Toast.LENGTH_SHORT).show();
            } else {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void speakWord(String word) {
        if (tts != null && word != null && !word.isEmpty()) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void showEditWordDialog(Word word) {
        EditWordDialog dialog = new EditWordDialog(
            this,
            jsonFileName,
            word,
            updatedWord -> {
                // Sau khi cập nhật, reload danh sách
                loadWords();
                adapter = new WordAdapter(wordList, this::speakWord, (w, position) -> {
                    showEditWordDialog(w);
                });
                recyclerView.setAdapter(adapter);
            },
            deletedWord -> {
                // Sau khi xóa, reload danh sách
                loadWords();
                adapter = new WordAdapter(wordList, this::speakWord, (w, position) -> {
                    showEditWordDialog(w);
                });
                recyclerView.setAdapter(adapter);
            }
        );
        dialog.show();
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

