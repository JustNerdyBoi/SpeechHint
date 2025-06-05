package ru.application.domain.entity;

import java.util.LinkedList;

public class Document {
    private LinkedList<Word> words;
    public LinkedList<Word> getWords() {
        return words;
    }

    public void setWords(LinkedList<Word> words) {
        this.words = words;
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