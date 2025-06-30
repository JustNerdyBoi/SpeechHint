package ru.application.domain.usecase;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Word;

import java.util.ArrayList;

public class CalculatePositionUseCase {

    /**
     * Searches for the position of the word in the document, starting from currentPosition,
     * first in the after buffer (size {@link SttConfig#getSttAfterBufferSize()}),
     * then in the before buffer (size {@link SttConfig#getSttBeforeBufferSize()}).
     *
     * @param word            the word to search for
     * @param document        the document
     * @param currentPosition the current position
     * @param sttConfig       the config with buffer sizes
     * @return the position of the found word or currentPosition, -1 if not found
     */
    public Integer execute(String word, Document document, Integer currentPosition, SttConfig sttConfig) {
        if (word == null || document == null || currentPosition == null || sttConfig == null) {
            return -1;
        }

        ArrayList<Word> words = document.getWords();
        if (words == null || words.isEmpty()) {
            return -1;
        }

        if (isWordEquals(words, currentPosition, word)) {
            return currentPosition;
        }

        int afterBuffer = sttConfig.getSttAfterBufferSize();
        int beforeBuffer = sttConfig.getSttBeforeBufferSize();
        int size = words.size();

        int afterBufferStartPosition = currentPosition + 1;
        int afterBufferEndPosition = Math.min(currentPosition + afterBuffer, size - 1);
        for (int i = afterBufferStartPosition; i <= afterBufferEndPosition; i++) {
            if (isWordEquals(words, i, word)) {
                return i;
            }
        }

        int beforeBufferStartPosition = currentPosition - 1; // before buffer scanned in reverse order
        int beforeBufferEndPosition = Math.max(currentPosition - beforeBuffer, 0);
        for (int i = beforeBufferStartPosition; i >= beforeBufferEndPosition; i--) {
            if (isWordEquals(words, i, word)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isWordEquals(ArrayList<Word> words, int pos, String word) {
        if (pos < 0 || pos >= words.size()) return false;
        return words.get(pos).getText().toLowerCase().contains(word.toLowerCase());
    }
}
