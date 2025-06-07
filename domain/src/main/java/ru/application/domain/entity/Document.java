package ru.application.domain.entity;

import java.util.LinkedList;

public class Document {
    private LinkedList<Word> words;
    public LinkedList<Word> getWords() {
        return words;
    }

    public void addWord(int pos, Word word){
        if (pos < 0) return;
        if (pos > words.size() - 1) {
            words.addLast(word);
            return;
        }
        words.add(pos, word);
    }

    public void editWord(int pos, String word){
        if (pos < 0 || pos > words.size() - 1) return;
        words.get(pos).setText(word);
    }

    public void removeWord(int pos) {
        if (pos < 0 || pos > words.size() - 1) return;
        words.remove(pos);
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