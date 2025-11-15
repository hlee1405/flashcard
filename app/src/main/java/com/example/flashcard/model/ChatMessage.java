package com.example.flashcard.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public enum Sender {
        USER, AI
    }
    
    private Sender sender;
    private String content;
    private long timestamp;
    
    public ChatMessage(Sender sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(Sender sender, String content, long timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    public Sender getSender() {
        return sender;
    }
    
    public void setSender(Sender sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

