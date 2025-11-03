package com.example.flashcard;

// ... (Các import không đổi)
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.adapter.MatchAdapter;
import com.example.flashcard.model.MatchCard;
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


public class MatchActivity extends AppCompatActivity {

    // ... (Các biến khai báo không đổi)
    private List<Word> fullWordList;
    private List<MatchCard> currentMatchCards;
    private MatchAdapter adapter;
    private RecyclerView recyclerView;
    private int firstCardPosition = -1;
    private int matchedPairs = 0;
    private final int PAIRS_TO_MATCH = 5;
    private boolean isChecking = false;
    private VocabularyDataManager dataManager;

    // ... (onCreate, loadWordsFromJson, setupNewGame không đổi)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        dataManager = new VocabularyDataManager(this);
        
        String jsonFileName = getIntent().getStringExtra("JSON_FILE_NAME");
        loadWords(jsonFileName);

        recyclerView = findViewById(R.id.gridRecyclerView);
        Button btnReplay = findViewById(R.id.btnReplay);
        TextView tvMatchTitle = findViewById(R.id.tvMatchTitle);

        // Xử lý window insets để tránh nội dung bị che
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            
            // Thêm padding top cho title để tránh bị che bởi status bar
            if (tvMatchTitle != null) {
                float density = getResources().getDisplayMetrics().density;
                int paddingTopDp = (int) (20 * density);
                tvMatchTitle.setPadding(
                    tvMatchTitle.getPaddingLeft(),
                    paddingTopDp + top,
                    tvMatchTitle.getPaddingRight(),
                    tvMatchTitle.getPaddingBottom()
                );
            }
            
            // Thêm padding bottom cho RecyclerView và button để tránh bị che bởi navigation bar
            if (recyclerView != null) {
                float density = getResources().getDisplayMetrics().density;
                int paddingBottomDp = (int) (24 * density);
                recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    recyclerView.getPaddingBottom() + paddingBottomDp
                );
            }
            
            if (btnReplay != null) {
                float density = getResources().getDisplayMetrics().density;
                int marginBottomDp = (int) (20 * density);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnReplay.getLayoutParams();
                params.bottomMargin = marginBottomDp + bottom;
                btnReplay.setLayoutParams(params);
            }
            
            return windowInsets;
        });

        if (fullWordList == null || fullWordList.size() < PAIRS_TO_MATCH) {
            Toast.makeText(this, "Không đủ từ vựng để chơi (cần ít nhất 5 từ)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupNewGame();
        btnReplay.setOnClickListener(v -> setupNewGame());
    }

    /**
     * Load từ vựng từ cả assets và user data
     */
    private void loadWords(String fileName) {
        fullWordList = new ArrayList<>();
        
        // Kiểm tra xem có phải bộ từ vựng do user tạo không
        boolean isUserCreated = dataManager.isUserCreatedSet(fileName);
        
        if (isUserCreated) {
            // Load từ user data
            fullWordList = dataManager.getWordsForSet(fileName);
        } else {
            // Load từ assets
            try {
                InputStream is = getAssets().open(fileName);
                InputStreamReader reader = new InputStreamReader(is);
                Type listType = new TypeToken<List<Word>>() {}.getType();
                List<Word> assetWords = new Gson().fromJson(reader, listType);
                reader.close();
                is.close();
                
                if (assetWords != null) {
                    fullWordList.addAll(assetWords);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Load thêm từ user data nếu có
            List<Word> userWords = dataManager.getWordsForSet(fileName);
            fullWordList.addAll(userWords);
        }
        
        if (fullWordList == null) {
            fullWordList = new ArrayList<>();
        }
    }

    private void setupNewGame() {
        matchedPairs = 0;
        firstCardPosition = -1;
        isChecking = false;
        currentMatchCards = new ArrayList<>();

        Collections.shuffle(fullWordList);
        List<Word> gameWords = fullWordList.subList(0, PAIRS_TO_MATCH);

        for (int i = 0; i < gameWords.size(); i++) {
            Word word = gameWords.get(i);
            currentMatchCards.add(new MatchCard(word.getEnglish(), i));
            currentMatchCards.add(new MatchCard(word.getVietnamese(), i));
        }

        Collections.shuffle(currentMatchCards);

        adapter = new MatchAdapter(currentMatchCards, this::handleCardClick);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }


    private void handleCardClick(int position) {
        if (isChecking) {
            return;
        }

        MatchCard clickedCard = currentMatchCards.get(position);

        if (firstCardPosition == -1) {
            // CHỌN THẺ ĐẦU TIÊN
            firstCardPosition = position;
            clickedCard.setFlipped(true);
            adapter.notifyItemChanged(position);
        } else {
            // CHỌN THẺ THỨ HAI
            isChecking = true;
            MatchCard firstCard = currentMatchCards.get(firstCardPosition);
            clickedCard.setFlipped(true);
            adapter.notifyItemChanged(position);

            if (firstCard.getId() == clickedCard.getId()) {
                // ✅ TRƯỜNG HỢP GHÉP ĐÚNG
                // Bước 1: Đổi cả 2 thẻ sang màu xanh
                firstCard.setCorrectPair(true);
                clickedCard.setCorrectPair(true);
                adapter.notifyItemChanged(firstCardPosition);
                adapter.notifyItemChanged(position);

                // Bước 2: Sau 0.5 giây, đánh dấu là đã khớp (sẽ bị ẩn đi)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    firstCard.setMatched(true);
                    clickedCard.setMatched(true);
                    matchedPairs++;
                    adapter.notifyItemChanged(firstCardPosition);
                    adapter.notifyItemChanged(position);

                    firstCardPosition = -1;
                    isChecking = false; // Mở khóa click

                    if (matchedPairs == PAIRS_TO_MATCH) {
                        showCompletionDialog();
                    }
                }, 500); // Delay 0.5 giây để người dùng thấy màu xanh

            } else {
                // ❌ TRƯỜNG HỢP GHÉP SAI (Logic không đổi)
                firstCard.setWrongPair(true);
                clickedCard.setWrongPair(true);
                adapter.notifyItemChanged(firstCardPosition);
                adapter.notifyItemChanged(position);

                shakeCard(recyclerView.findViewHolderForAdapterPosition(firstCardPosition).itemView);
                shakeCard(recyclerView.findViewHolderForAdapterPosition(position).itemView);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    firstCard.setFlipped(false);
                    clickedCard.setFlipped(false);
                    firstCard.setWrongPair(false);
                    clickedCard.setWrongPair(false);

                    adapter.notifyItemChanged(firstCardPosition);
                    adapter.notifyItemChanged(position);

                    firstCardPosition = -1;
                    isChecking = false;
                }, 1000);
            }
        }
    }

    // ... (shakeCard và showCompletionDialog không đổi)
    private void shakeCard(View view) {
        if (view == null) return;
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(1));
        shake.start();
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành!")
                .setMessage("Bạn đã ghép đúng tất cả các cặp từ.")
                .setPositiveButton("Chơi lại", (dialog, which) -> setupNewGame())
                .setNegativeButton("Thoát", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
