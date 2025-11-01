package com.example.flashcard.model;

public class VocabularySet {
    private String title;
    private String jsonFileName;

    public VocabularySet(String title, String jsonFileName) {
        this.title = title;
        this.jsonFileName = jsonFileName;
    }

    public String getTitle() {
        return title;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }
}
