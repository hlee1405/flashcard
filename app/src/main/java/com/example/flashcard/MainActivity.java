package com.example.flashcard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.adapter.VocabularySetAdapter;
import com.example.flashcard.model.VocabularySet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách các bộ từ vựng
        List<VocabularySet> vocabularySets = new ArrayList<>();
        vocabularySets.add(new VocabularySet("Thức ăn và Đồ uống", "food.json"));
        vocabularySets.add(new VocabularySet("Sức khỏe", "health.json"));
        vocabularySets.add(new VocabularySet("Thể thao", "sports.json"));
        vocabularySets.add(new VocabularySet("Diện mạo", "appearance.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Du lịch", "travel.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Môi trường", "environment.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Nhà cửa", "house.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Công việc", "jobs.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Giải trí", "entertainment.json")); // <-- Thêm mới
        vocabularySets.add(new VocabularySet("Giáo dục", "education.json")); // <-- Thêm mới

        // ...

        // Thiết lập Adapter
        VocabularySetAdapter adapter = new VocabularySetAdapter(vocabularySets, set -> {
            // Khi người dùng chọn một bộ từ, hiển thị dialog chọn chế độ
            showModeSelectionDialog(set);
        });
        recyclerView.setAdapter(adapter);
    }

    private void showModeSelectionDialog(VocabularySet set) {
        final String[] modes = {"Chế độ Học", "Chế độ Ghép đôi"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn chế độ cho bộ '" + set.getTitle() + "'")
                .setItems(modes, (dialog, which) -> {
                    if (which == 0) {
                        // Chế độ Học
                        Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                        intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
                        startActivity(intent);
                    } else {
                        // Chế độ Ghép đôi
                        Intent intent = new Intent(MainActivity.this, MatchActivity.class);
                        intent.putExtra("JSON_FILE_NAME", set.getJsonFileName());
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }
}
