package com.example.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.TextView;
import com.example.flashcard.adapter.VocabularySetAdapter;
import com.example.flashcard.dialog.AddVocabularySetDialog;
import com.example.flashcard.dialog.EditVocabularySetDialog;
import com.example.flashcard.model.VocabularySet;
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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VocabularySetAdapter adapter;
    private List<VocabularySet> vocabularySets;
    private VocabularyDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar color to green and make it transparent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_primary, null));
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        
        setContentView(R.layout.activity_main);

        dataManager = new VocabularyDataManager(this);
        recyclerView = findViewById(R.id.recyclerViewSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Nút thêm bộ từ vựng
        Button btnAddVocabularySet = findViewById(R.id.btnAddVocabularySet);
        btnAddVocabularySet.setOnClickListener(v -> showAddVocabularySetDialog());
        
        // Xử lý window insets để header không bị che và RecyclerView không bị che
        View headerView = findViewById(R.id.tvHeader);
        View addVocabularySetContainer = findViewById(R.id.addVocabularySetContainer);
        float density = getResources().getDisplayMetrics().density;
        int headerPaddingTopDp = (int) (20 * density); // 20dp padding gốc
        
        // Tìm LinearLayout bên trong CardView để set padding
        android.widget.LinearLayout buttonContainerLayout = findViewById(R.id.buttonContainerLayout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            
            // Thêm padding top cho header để kéo dài lên status bar và không bị che chữ
            if (headerView != null) {
                headerView.setPadding(
                    headerView.getPaddingLeft(),
                    headerPaddingTopDp + top, // 20dp padding gốc + status bar height
                    headerView.getPaddingRight(),
                    headerView.getPaddingBottom()
                );
            }
            
            // Thêm padding bottom cho LinearLayout bên trong container nút thêm để tránh bị che bởi navigation bar
            if (buttonContainerLayout != null) {
                int paddingBottomDp = (int) (28 * density); // 28dp padding base để đảm bảo không bị che
                buttonContainerLayout.setPadding(
                    buttonContainerLayout.getPaddingLeft(),
                    buttonContainerLayout.getPaddingTop(),
                    buttonContainerLayout.getPaddingRight(),
                    paddingBottomDp + bottom
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

        // Load danh sách bộ từ vựng
        loadVocabularySets();
        
        // Thiết lập Adapter
        adapter = new VocabularySetAdapter(
            vocabularySets,
            // Click vào item -> hiển thị danh sách từ vựng
            set -> {
                Intent intent = new Intent(MainActivity.this, VocabularyListActivity.class);
                intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
                intent.putExtra("CATEGORY_TITLE", set.getTitle());
                startActivity(intent);
            },
            // Click vào play button -> hiển thị dialog chọn chế độ
            this::showModeSelectionDialog,
            // Long click vào item -> hiển thị dialog chỉnh sửa/xóa
            set -> {
                // Chỉ cho phép chỉnh sửa/xóa bộ từ vựng do user tạo
                if (dataManager.isUserCreatedSet(set.getJsonFileName())) {
                    showEditVocabularySetDialog(set);
                } else {
                    android.widget.Toast.makeText(this, "Không thể chỉnh sửa bộ từ vựng mặc định!", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        );
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Load danh sách bộ từ vựng từ cả assets và user data
     */
    private void loadVocabularySets() {
        vocabularySets = new ArrayList<>();
        
        // Load từ assets
        String[][] allSets = {
            {"Nhà", "house.json"},
            {"Thức ăn và đồ uống", "food.json"},
            {"Diện mạo", "appearance.json"},
            {"Sức khỏe", "health.json"},
            {"Thể thao", "sports.json"},
            {"Du lịch", "travel.json"},
            {"Môi trường", "environment.json"},
            {"Công việc", "jobs.json"},
            {"Giải trí", "entertainment.json"},
            {"Giáo dục", "education.json"}
        };
        
        // Load và đếm số từ từ mỗi file JSON nếu file tồn tại
        for (String[] set : allSets) {
            String title = set[0];
            String fileName = set[1].trim();
            String actualFileName = findActualFileName(fileName);
            if (actualFileName != null) {
                int wordCount = getWordCountFromJson(actualFileName);
                if (wordCount > 0) {
                    vocabularySets.add(new VocabularySet(title, actualFileName, wordCount));
                }
            }
        }
        
        // Load từ user data
        List<VocabularySet> userSets = dataManager.getUserVocabularySets();
        for (VocabularySet set : userSets) {
            // Đếm số từ trong bộ do user tạo
            List<Word> words = dataManager.getWordsForSet(set.getJsonFileName());
            set.setWordCount(words.size());
            vocabularySets.add(set);
        }
    }
    
    /**
     * Hiển thị dialog để thêm bộ từ vựng mới
     */
    private void showAddVocabularySetDialog() {
        AddVocabularySetDialog dialog = new AddVocabularySetDialog(
            this,
            newSet -> {
                // Sau khi thêm thành công, reload danh sách
                loadVocabularySets();
                // Tạo lại adapter với danh sách mới
                adapter = new VocabularySetAdapter(
                    vocabularySets,
                    set1 -> {
                        Intent intent = new Intent(MainActivity.this, VocabularyListActivity.class);
                        intent.putExtra("JSON_FILE_NAME", set1.getJsonFileName());
                        intent.putExtra("CATEGORY_TITLE", set1.getTitle());
                        startActivity(intent);
                    },
                    this::showModeSelectionDialog,
                    set2 -> {
                        if (dataManager.isUserCreatedSet(set2.getJsonFileName())) {
                            showEditVocabularySetDialog(set2);
                        } else {
                            android.widget.Toast.makeText(this, "Không thể chỉnh sửa bộ từ vựng mặc định!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                );
                recyclerView.setAdapter(adapter);
            }
        );
        dialog.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình này
        loadVocabularySets();
        if (adapter != null && vocabularySets != null) {
            adapter = new VocabularySetAdapter(
                vocabularySets,
                set -> {
                    Intent intent = new Intent(MainActivity.this, VocabularyListActivity.class);
                    intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
                    intent.putExtra("CATEGORY_TITLE", set.getTitle());
                    startActivity(intent);
                },
                this::showModeSelectionDialog,
                set -> {
                    if (dataManager.isUserCreatedSet(set.getJsonFileName())) {
                        showEditVocabularySetDialog(set);
                    } else {
                        android.widget.Toast.makeText(this, "Không thể chỉnh sửa bộ từ vựng mặc định!", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            );
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Đọc và đếm số từ trong file JSON
     * @param fileName Tên file JSON trong assets
     * @return Số lượng từ trong file, 0 nếu có lỗi
     */
    private int getWordCountFromJson(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(is);
            Type listType = new TypeToken<List<Word>>() {}.getType();
            List<Word> words = new Gson().fromJson(reader, listType);
            reader.close();
            is.close();
            return words != null ? words.size() : 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Tìm tên file thực tế trong assets (có thể có khoảng trắng)
     * @param fileName Tên file cần tìm
     * @return Tên file thực tế nếu tìm thấy, null nếu không
     */
    private String findActualFileName(String fileName) {
        // Thử cả tên file có khoảng trắng và không có khoảng trắng
        String[] possibleNames = {fileName, fileName.trim(), " " + fileName.trim(), fileName.trim() + " "};
        
        for (String name : possibleNames) {
            try {
                InputStream is = getAssets().open(name);
                is.close();
                return name; // Trả về tên file thực tế
            } catch (IOException e) {
                // Tiếp tục thử tên khác
            }
        }
        return null;
    }
    
    /**
     * Kiểm tra xem file có tồn tại trong assets không
     * @param fileName Tên file cần kiểm tra
     * @return true nếu file tồn tại, false nếu không
     */
    private boolean fileExistsInAssets(String fileName) {
        return findActualFileName(fileName) != null;
    }

    private void showModeSelectionDialog(VocabularySet set) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_mode_selection, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvSubtitle);
        androidx.cardview.widget.CardView cardLearningMode = dialogView.findViewById(R.id.cardLearningMode);
        androidx.cardview.widget.CardView cardMatchingMode = dialogView.findViewById(R.id.cardMatchingMode);
        
        tvSubtitle.setText("Bộ từ vựng: " + set.getTitle());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        
        cardLearningMode.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, StudyActivity.class);
            intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
            startActivity(intent);
        });
        
        cardMatchingMode.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, MatchActivity.class);
            intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
            startActivity(intent);
        });
        
        dialog.show();
    }
    
    private void showEditVocabularySetDialog(VocabularySet set) {
        EditVocabularySetDialog dialog = new EditVocabularySetDialog(
            this,
            set,
            updatedSet -> {
                // Sau khi cập nhật, reload danh sách
                loadVocabularySets();
                adapter = new VocabularySetAdapter(
                    vocabularySets,
                    set1 -> {
                        Intent intent = new Intent(MainActivity.this, VocabularyListActivity.class);
                        intent.putExtra("JSON_FILE_NAME", set1.getJsonFileName());
                        intent.putExtra("CATEGORY_TITLE", set1.getTitle());
                        startActivity(intent);
                    },
                    this::showModeSelectionDialog,
                    set2 -> {
                        if (dataManager.isUserCreatedSet(set2.getJsonFileName())) {
                            showEditVocabularySetDialog(set2);
                        } else {
                            android.widget.Toast.makeText(this, "Không thể chỉnh sửa bộ từ vựng mặc định!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                );
                recyclerView.setAdapter(adapter);
            },
            deletedSet -> {
                // Sau khi xóa, reload danh sách
                loadVocabularySets();
                adapter = new VocabularySetAdapter(
                    vocabularySets,
                    set1 -> {
                        Intent intent = new Intent(MainActivity.this, VocabularyListActivity.class);
                        intent.putExtra("JSON_FILE_NAME", set1.getJsonFileName());
                        intent.putExtra("CATEGORY_TITLE", set1.getTitle());
                        startActivity(intent);
                    },
                    this::showModeSelectionDialog,
                    set2 -> {
                        if (dataManager.isUserCreatedSet(set2.getJsonFileName())) {
                            showEditVocabularySetDialog(set2);
                        } else {
                            android.widget.Toast.makeText(this, "Không thể chỉnh sửa bộ từ vựng mặc định!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                );
                recyclerView.setAdapter(adapter);
            }
        );
        dialog.show();
    }
}
