package ru.application.domain.interactor;

import java.util.ArrayList;
import java.util.Set;

public class SpeechRecognitionInteractor {
    public static final String WORD_SEPARATOR_REGEX = "\\s+";

    public static String findUnduplicatedWord(ArrayList<String> matches, Set<String> lastWordsSet) {
        if (matches != null && !matches.isEmpty()) {
            String[] words = matches.get(0).trim().split(WORD_SEPARATOR_REGEX);
            for (String word : words) {
                if (!word.isEmpty() && lastWordsSet.add(word.toLowerCase())) {
                    return word;
                }
            }
        }
        return null;
    }
}
