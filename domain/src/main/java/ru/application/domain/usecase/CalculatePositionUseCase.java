package ru.application.domain.usecase;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Word;

import java.util.ArrayList;

public class CalculatePositionUseCase {

    /**
     * Поиск позиции слова word в документе, начиная с currentPosition,
     * сначала в after-буфере, затем в before-буфере.
     * Если не найдено или найдено на currentPosition, возвращается currentPosition.
     *
     * @param word            слово для поиска
     * @param document        документ
     * @param currentPosition текущая позиция
     * @param sttConfig       конфиг с размерами буферов
     * @return позиция найденного слова или currentPosition
     */
    public Integer execute(String word, Document document, Integer currentPosition, SttConfig sttConfig) {
        if (word == null || document == null || currentPosition == null || sttConfig == null) {
            return currentPosition;
        }

        ArrayList<Word> words = document.getWords();
        if (words == null || words.isEmpty()) {
            return currentPosition;
        }

        if (isWordEquals(words, currentPosition, word)) {
            return currentPosition;
        }

        int afterBuffer = sttConfig.getSttAfterBufferSize();
        int beforeBuffer = sttConfig.getSttBeforeBufferSize();
        int size = words.size();

        int afterStart = currentPosition + 1;
        int afterEnd = Math.min(currentPosition + afterBuffer, size - 1);
        for (int i = afterStart; i <= afterEnd; i++) {
            if (isWordEquals(words, i, word)) {
                return i;
            }
        }

        int beforeStart = Math.max(currentPosition - beforeBuffer, 0);
        int beforeEnd = currentPosition - 1;
        for (int i = beforeStart; i <= beforeEnd; i++) {
            if (isWordEquals(words, i, word)) {
                return i;
            }
        }

        return currentPosition;
    }

    private boolean isWordEquals(ArrayList<Word> words, int pos, String word) {
        if (pos < 0 || pos >= words.size()) return false;
        return words.get(pos).getText().toLowerCase().contains(word.toLowerCase());
    }
}
