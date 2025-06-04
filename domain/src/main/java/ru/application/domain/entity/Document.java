package ru.application.domain.entity;

import java.util.LinkedList;

public class Document {
    private LinkedList<Word> words;
    private String title;

    public LinkedList<Word> getWords() {
        return words;
    }

    public void setWords(LinkedList<Word> words) {
        this.words = words;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}