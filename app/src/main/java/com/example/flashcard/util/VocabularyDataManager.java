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

    public List<VocabularySet> getUserVocabularySets() {
        String json = prefs.getString(KEY_VOCABULARY_SETS, "[]");
        Type type = new TypeToken<List<VocabularySet>>() {}.getType();
        List<VocabularySet> sets = gson.fromJson(json, type);
        return sets != null ? sets : new ArrayList<>();
    }
    
    public void addVocabularySet(VocabularySet set) {
        List<VocabularySet> sets = getUserVocabularySets();
        sets.add(set);
        saveVocabularySets(sets);
    }
    
    public void saveVocabularySets(List<VocabularySet> sets) {
        String json = gson.toJson(sets);
        prefs.edit().putString(KEY_VOCABULARY_SETS, json).apply();
    }
    
    public List<Word> getWordsForSet(String jsonFileName) {
        String key = KEY_WORDS_PREFIX + jsonFileName;
        String json = prefs.getString(key, "[]");
        Type type = new TypeToken<List<Word>>() {}.getType();
        List<Word> words = gson.fromJson(json, type);
        return words != null ? words : new ArrayList<>();
    }
    
    public void addWordToSet(String jsonFileName, Word word) {
        List<Word> words = getWordsForSet(jsonFileName);
        words.add(word);
        saveWordsForSet(jsonFileName, words);
    }
    
    public void saveWordsForSet(String jsonFileName, List<Word> words) {
        String key = KEY_WORDS_PREFIX + jsonFileName;
        String json = gson.toJson(words);
        prefs.edit().putString(key, json).apply();
    }

    public void deleteVocabularySet(String jsonFileName) {
        List<VocabularySet> sets = getUserVocabularySets();
        sets.removeIf(set -> set.getJsonFileName().equals(jsonFileName));
        saveVocabularySets(sets);
        
        String key = KEY_WORDS_PREFIX + jsonFileName;
        prefs.edit().remove(key).apply();
    }

    public boolean isUserCreatedSet(String jsonFileName) {
        List<VocabularySet> sets = getUserVocabularySets();
        return sets.stream().anyMatch(set -> set.getJsonFileName().equals(jsonFileName));
    }

    public void updateVocabularySet(VocabularySet updatedSet) {
        List<VocabularySet> sets = getUserVocabularySets();
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).getJsonFileName().equals(updatedSet.getJsonFileName())) {
                sets.set(i, updatedSet);
                break;
            }
        }
        saveVocabularySets(sets);
    }

    public void deleteWordFromSet(String jsonFileName, Word wordToDelete) {
        List<Word> words = getWordsForSet(jsonFileName);
        words.removeIf(word -> 
            word.getEnglish().equals(wordToDelete.getEnglish()) && 
            word.getVietnamese().equals(wordToDelete.getVietnamese())
        );
        saveWordsForSet(jsonFileName, words);
    }

    public void updateWordInSet(String jsonFileName, Word oldWord, Word newWord) {
        List<Word> words = getWordsForSet(jsonFileName);
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            if (word.getEnglish().equals(oldWord.getEnglish()) && 
                word.getVietnamese().equals(oldWord.getVietnamese())) {
                words.set(i, newWord);
                break;
            }
        }
        saveWordsForSet(jsonFileName, words);
    }
}

