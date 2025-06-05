package ru.application.domain.usecase;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.repository.DocumentRepository;

public class LoadDocumentUseCase {
    private final DocumentRepository repository;
    public LoadDocumentUseCase(DocumentRepository repository) {
        this.repository = repository;
    }
    public Document execute(DocumentSource source, String uri) throws Exception {
        return repository.loadDocument(source, uri);
    }
}
