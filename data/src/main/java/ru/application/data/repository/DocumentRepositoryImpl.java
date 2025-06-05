package ru.application.data.repository;

import android.net.Uri;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.repository.DocumentRepository;
import ru.application.data.datasource.*;

public class DocumentRepositoryImpl implements DocumentRepository {
    private final LocalDataSource localDataSource;
    private final GoogleDriveDataSource googleDriveDataSource;
    private final YandexDriveDataSource yandexDriveDataSource;
    private final ServerDataSource serverDataSource;

    public DocumentRepositoryImpl(
            LocalDataSource local,
            GoogleDriveDataSource google,
            YandexDriveDataSource yandex,
            ServerDataSource server
    ) {
        this.localDataSource = local;
        this.googleDriveDataSource = google;
        this.yandexDriveDataSource = yandex;
        this.serverDataSource = server;
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
            case SERVER:
                return serverDataSource.loadDocument(uri);
            default:
                throw new IllegalArgumentException("Unknown source");
        }
    }
}
