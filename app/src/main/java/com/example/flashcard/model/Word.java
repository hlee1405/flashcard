package com.example.flashcard.model;

import java.io.Serializable;

public class Word implements Serializable {

    private String english;
    private String vietnamese;
    private String pronunciation;
    private String example;
    private String memoryTip;

    public Word(String english, String vietnamese, String pronunciation) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.pronunciation = pronunciation;
        this.example = "";
        this.memoryTip = "";
    }

    public Word(String english, String vietnamese, String pronunciation, String example, String memoryTip) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.pronunciation = pronunciation;
        this.example = example != null ? example : "";
        this.memoryTip = memoryTip != null ? memoryTip : "";
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

    public String getExample() {
        return example != null ? example : "";
    }

    public String getMemoryTip() {
        return memoryTip != null ? memoryTip : "";
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setMemoryTip(String memoryTip) {
        this.memoryTip = memoryTip;
    }
}
