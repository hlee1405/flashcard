package com.example.flashcard.model;

public class VocabularySet {
    private String title;
    private String jsonFileName;
    private int wordCount;

    public VocabularySet(String title, String jsonFileName) {
        this.title = title;
        this.jsonFileName = jsonFileName;
        this.wordCount = 0;
    }

    public VocabularySet(String title, String jsonFileName, int wordCount) {
        this.title = title;
        this.jsonFileName = jsonFileName;
        this.wordCount = wordCount;
    }

    public String getTitle() {
        return title;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}
