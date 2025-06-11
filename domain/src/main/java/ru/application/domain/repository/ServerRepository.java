package ru.application.domain.repository;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.ServerConnectionInfo;
import ru.application.domain.entity.Settings;

public interface ServerRepository {
    void startServer();

    void stopServer();

    ServerConnectionInfo getServerConnectionInfo();

    void setListener(Listener listener);

    void setServerCurrentPosition(int newCurrentPosition);

    void setServerCurrentSettings(Settings settings);

    void setServerCurrentDocument(Document document);

    interface Listener {
        void onCurrentPositionReceived(int newCurrentPosition);

        void onScrollReceived(int scrollY);

        void onDocumentReceived(Document documents);

        void onSettingsReceived(Settings settings);
    }
}