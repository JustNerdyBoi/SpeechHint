package ru.application.domain.usecase;

import java.util.ArrayList;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

public class AddWordUseCase {
    public Document execute(Document oldDocument, int pos, String text) {
        ArrayList<Word> words = oldDocument.getWords();

        if (pos < 0) return oldDocument;
        if (pos > words.size() - 1) {
            words.add(new Word(text));
            return oldDocument;
        }
        words.add(pos, new Word(text));

        return new Document(words, oldDocument.getLanguage());
    }
}
