package ru.application.domain.repository;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;

public interface DocumentRepository {
    Document loadDocument(DocumentSource source, String uri)  throws Exception;
}
