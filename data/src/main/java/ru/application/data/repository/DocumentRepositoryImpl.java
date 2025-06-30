package ru.application.data.repository;

import android.net.Uri;

import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.LocalDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.repository.DocumentRepository;

public class DocumentRepositoryImpl implements DocumentRepository {
    private final LocalDataSource localDataSource;
    private final GoogleDriveDataSource googleDriveDataSource;
    private final YandexDriveDataSource yandexDriveDataSource;

    public DocumentRepositoryImpl(
            LocalDataSource local,
            GoogleDriveDataSource google,
            YandexDriveDataSource yandex
    ) {
        this.localDataSource = local;
        this.googleDriveDataSource = google;
        this.yandexDriveDataSource = yandex;
    }

    @Override
    public Document loadDocument(DocumentSource source, String stringUri) throws Exception {
        Uri uri = Uri.parse(stringUri);
        switch (source) {
            case LOCAL:
                return localDataSource.loadDocument(uri);
            case GOOGLE_DRIVE:
                return googleDriveDataSource.loadDocument(stringUri);
            case YANDEX_DRIVE:
                return yandexDriveDataSource.loadDocument(stringUri);
            default:
                throw new IllegalArgumentException("Unknown source");
        }
    }
}
