package ru.application.domain.usecase;

import ru.application.domain.entity.Document;
import ru.application.domain.repository.ServerRepository;

public class SetServerCurrentDocumentUseCase {
    private final ServerRepository repository;

    public SetServerCurrentDocumentUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public void execute(Document document) {
        repository.setServerCurrentDocument(document);
    }
}