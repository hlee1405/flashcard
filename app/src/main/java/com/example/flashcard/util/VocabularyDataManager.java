package com.example.flashcard.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.flashcard.model.VocabularySet;
import com.example.flashcard.model.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class VocabularyDataManager {
    private static final String PREFS_NAME = "flashcard_prefs";
    private static final String KEY_VOCABULARY_SETS = "vocabulary_sets";
    private static final String KEY_WORDS_PREFIX = "words_";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public VocabularyDataManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Lấy tất cả các bộ từ vựng do người dùng tạo
     */
    public List<VocabularySet> getUserVocabularySets() {
        String json = prefs.getString(KEY_VOCABULARY_SETS, "[]");
        Type type = new TypeToken<List<VocabularySet>>() {}.getType();
        List<VocabularySet> sets = gson.fromJson(json, type);
        return sets != null ? sets : new ArrayList<>();
    }
    
    /**
     * Thêm bộ từ vựng mới
     */
    public void addVocabularySet(VocabularySet set) {
        List<VocabularySet> sets = getUserVocabularySets();
        sets.add(set);
        saveVocabularySets(sets);
    }
    
    /**
     * Lưu danh sách bộ từ vựng
     */
    public void saveVocabularySets(List<VocabularySet> sets) {
        String json = gson.toJson(sets);
        prefs.edit().putString(KEY_VOCABULARY_SETS, json).apply();
    }
    
    /**
     * Lấy danh sách từ vựng của một bộ từ vựng
     */
    public List<Word> getWordsForSet(String jsonFileName) {
        String key = KEY_WORDS_PREFIX + jsonFileName;
        String json = prefs.getString(key, "[]");
        Type type = new TypeToken<List<Word>>() {}.getType();
        List<Word> words = gson.fromJson(json, type);
        return words != null ? words : new ArrayList<>();
    }
    
    /**
     * Thêm từ vựng vào bộ từ vựng
     */
    public void addWordToSet(String jsonFileName, Word word) {
        List<Word> words = getWordsForSet(jsonFileName);
        words.add(word);
        saveWordsForSet(jsonFileName, words);
    }
    
    /**
     * Lưu danh sách từ vựng của một bộ
     */
    public void saveWordsForSet(String jsonFileName, List<Word> words) {
        String key = KEY_WORDS_PREFIX + jsonFileName;
        String json = gson.toJson(words);
        prefs.edit().putString(key, json).apply();
    }
    
    /**
     * Xóa bộ từ vựng
     */
    public void deleteVocabularySet(String jsonFileName) {
        List<VocabularySet> sets = getUserVocabularySets();
        sets.removeIf(set -> set.getJsonFileName().equals(jsonFileName));
        saveVocabularySets(sets);
        
        // Xóa từ vựng của bộ đó
        String key = KEY_WORDS_PREFIX + jsonFileName;
        prefs.edit().remove(key).apply();
    }
    
    /**
     * Kiểm tra xem bộ từ vựng có phải do người dùng tạo không
     */
    public boolean isUserCreatedSet(String jsonFileName) {
        List<VocabularySet> sets = getUserVocabularySets();
        return sets.stream().anyMatch(set -> set.getJsonFileName().equals(jsonFileName));
    }
}

