package com.example.flashcard.model;

import java.io.Serializable;

public class Word implements Serializable {

    private String english;
    private String vietnamese;
    private String pronunciation;


    public Word(String english, String vietnamese, String pronunciation) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.pronunciation = pronunciation;
    }

    public String getEnglish() {
        return english;
    }

    public String getVietnamese() {
        return vietnamese;
    }

    public String getPronunciation() {
        return pronunciation;
    }
}
