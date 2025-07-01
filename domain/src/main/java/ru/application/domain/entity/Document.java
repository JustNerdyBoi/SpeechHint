package ru.application.domain.entity;

import java.util.ArrayList;

public class Document {
    private final ArrayList<Word> words;
    private final String language;

    public Document(ArrayList<Word> words, String language) {
        this.words = words;
        this.language = language;
    }

    public Document() {
        this.words = new ArrayList<>();
        this.language = null;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    public String getLanguage() {
        return language;
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