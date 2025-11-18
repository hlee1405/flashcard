package com.example.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.flashcard.adapter.VocabularySetAdapter;
import com.example.flashcard.dialog.AddVocabularySetDialog;
import com.example.flashcard.dialog.AIVocabularyDialog;
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

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.green_primary)
        );

        setContentView(R.layout.activity_main);

        dataManager = new VocabularyDataManager(this);
        recyclerView = findViewById(R.id.recyclerViewSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> showMenuPopup(v));
        }

        Button btnAddVocabularySet = findViewById(R.id.btnAddVocabularySet);
        btnAddVocabularySet.setOnClickListener(v -> showAddVocabularySetDialog());

        View headerView = findViewById(R.id.tvHeader);
        float density = getResources().getDisplayMetrics().density;
        int headerPaddingTopDp = (int) (20 * density);

        android.widget.LinearLayout buttonContainerLayout = findViewById(R.id.buttonContainerLayout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            if (headerView != null) {
                headerView.setPadding(
                        headerView.getPaddingLeft(),
                        headerPaddingTopDp + top,
                        headerView.getPaddingRight(),
                        headerView.getPaddingBottom()
                );
            }

            if (buttonContainerLayout != null) {
                int paddingBottomDp = (int) (28 * density);
                buttonContainerLayout.setPadding(
                        buttonContainerLayout.getPaddingLeft(),
                        buttonContainerLayout.getPaddingTop(),
                        buttonContainerLayout.getPaddingRight(),
                        paddingBottomDp + bottom
                );
            }

            int paddingBottomDp = (int) (24 * density);
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    bottom > 0 ? bottom + paddingBottomDp : paddingBottomDp
            );

            return windowInsets;
        });

        loadVocabularySets();

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
                        android.widget.Toast.makeText(this, getString(R.string.cannot_edit_default_set), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private void loadVocabularySets() {
        vocabularySets = new ArrayList<>();

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

        List<VocabularySet> userSets = dataManager.getUserVocabularySets();
        for (VocabularySet set : userSets) {
            List<Word> words = dataManager.getWordsForSet(set.getJsonFileName());
            set.setWordCount(words.size());
            vocabularySets.add(set);
        }
    }

    private void showAIVocabularyDialog() {
        AIVocabularyDialog dialog = new AIVocabularyDialog(
                this,
                (set, words) -> {
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

    private void showAddVocabularySetDialog() {
        AddVocabularySetDialog dialog = new AddVocabularySetDialog(
                this,
                newSet -> {
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

    @Override
    protected void onResume() {
        super.onResume();
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
            Log.e("MainActivity", "Error reading word count from " + fileName, e);
            return 0;
        }
    }

    private String findActualFileName(String fileName) {
        String[] possibleNames = {fileName, fileName.trim(), " " + fileName.trim(), fileName.trim() + " "};

        for (String name : possibleNames) {
            try {
                InputStream is = getAssets().open(name);
                is.close();
                return name;
            } catch (IOException e) {
            }
        }
        return null;
    }


    private void showModeSelectionDialog(VocabularySet set) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_mode_selection, null);

        TextView tvSubtitle = dialogView.findViewById(R.id.tvSubtitle);
        androidx.cardview.widget.CardView cardLearningMode = dialogView.findViewById(R.id.cardLearningMode);
        androidx.cardview.widget.CardView cardMatchingMode = dialogView.findViewById(R.id.cardMatchingMode);

        tvSubtitle.setText(getString(R.string.vocabulary_set_prefix, set.getTitle()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

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

    private void showMenuPopup(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(0, 1, 0, "💬 AI Chat");
        popupMenu.getMenu().add(0, 2, 0, "🤖 Tạo bộ từ vựng bằng AI");
        
        popupMenu.setGravity(android.view.Gravity.END | android.view.Gravity.TOP);
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == 2) {
                showAIVocabularyDialog();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
        
        try {
            java.lang.reflect.Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            Object menuPopupHelper = mPopup.get(popupMenu);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            
            java.lang.reflect.Method setOffset = classPopupHelper.getMethod("setHorizontalOffset", int.class);
            setOffset.invoke(menuPopupHelper, -(int)(40 * getResources().getDisplayMetrics().density));
            
            java.lang.reflect.Method setVerticalOffset = classPopupHelper.getMethod("setVerticalOffset", int.class);
            setVerticalOffset.invoke(menuPopupHelper, -(int)(20 * getResources().getDisplayMetrics().density));
        } catch (Exception e) {
            Log.e("MainActivity", "Error adjusting popup position", e);
        }
    }

    private void showEditVocabularySetDialog(VocabularySet set) {
        EditVocabularySetDialog dialog = new EditVocabularySetDialog(
                this,
                set,
                updatedSet -> {
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