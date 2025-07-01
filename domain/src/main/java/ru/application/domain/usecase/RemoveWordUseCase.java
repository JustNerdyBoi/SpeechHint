package ru.application.domain.usecase;

import java.util.ArrayList;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

public class RemoveWordUseCase {
    public Document execute(Document oldDocument, int pos) {
        ArrayList<Word> words = oldDocument.getWords();

        if (pos < 0 || pos > words.size() - 1) return oldDocument;
        words.remove(pos);

        return new Document(words, oldDocument.getLanguage());
    }
}
