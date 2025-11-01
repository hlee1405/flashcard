package com.example.flashcard.model; // Thay bằng package của bạn

import java.io.Serializable;

public class Word implements Serializable {

    // Khai báo các trường dữ liệu.
    // Tên các trường này phải khớp với key trong file JSON để thư viện Gson có thể tự động map dữ liệu.
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
