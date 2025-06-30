package ru.application.domain.entity;

public class Word {
    private final String text;

    public Word(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}