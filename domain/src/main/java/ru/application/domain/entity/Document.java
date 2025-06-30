package ru.application.domain.entity;

import java.util.ArrayList;

public class Document {
    private final ArrayList<Word> words;

    public Document(ArrayList<Word> words) {
        this.words = words;
    }

    public ArrayList<Word> getWords() {
        return words;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (words != null) {
            for (Word word : words) {
                sb.append(word.getText()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}