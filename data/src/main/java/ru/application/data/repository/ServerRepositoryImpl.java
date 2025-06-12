package ru.application.data.repository;

import android.content.Context;

import ru.application.data.server.RestServer;
import ru.application.data.utils.NetworkUtils;
import ru.application.domain.entity.ServerConnectionInfo;
import ru.application.domain.repository.ServerRepository;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Settings;

import java.io.IOException;

public class ServerRepositoryImpl implements ServerRepository {
    private RestServer server;
    private Listener listener;
    private static final int SERVER_PORT = 8080;
    private final Context context;

    public ServerRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public void startServer() {
        if (server == null) {
            server = new RestServer(SERVER_PORT, context);
            server.setListener(listener);
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Override
    public ServerConnectionInfo getServerConnectionInfo() {
        if (server != null) {
            String ip = NetworkUtils.getLocalIpAddress();
            return new ServerConnectionInfo(ip, SERVER_PORT);
        } else {
            return null;
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        if (server != null) {
            server.setListener(listener);
        }
    }

    @Override
    public void setServerCurrentPosition(int newCurrentPosition) {
        if (server != null) {
            server.setCurrentPosition(newCurrentPosition);
        }
    }

    @Override
    public void setServerCurrentSettings(Settings settings) {
        if (server != null) {
            server.setCurrentSettings(settings);
        }
    }

    @Override
    public void setServerCurrentDocument(Document document) {
        if (server != null) {
            server.setCurrentDocument(document);
        }
    }
}
