package com.example.flashcard.model;

public class MatchCard {
    private final String text;
    private final int id;

    private boolean isMatched = false;
    private boolean isFlipped = false;
    private boolean isWrongPair = false;
    private boolean isCorrectPair = false;

    public MatchCard(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public String getText() { return text; }
    public int getId() { return id; }

    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }

    public boolean isFlipped() { return isFlipped; }
    public void setFlipped(boolean flipped) { isFlipped = flipped; }

    public boolean isWrongPair() { return isWrongPair; }
    public void setWrongPair(boolean wrongPair) { isWrongPair = wrongPair; }

    public boolean isCorrectPair() { return isCorrectPair; }
    public void setCorrectPair(boolean correctPair) { isCorrectPair = correctPair; }
}
