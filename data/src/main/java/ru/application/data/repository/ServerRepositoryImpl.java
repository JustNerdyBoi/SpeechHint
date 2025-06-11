package ru.application.data.repository;

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

    @Override
    public void startServer() {
        if (server == null) {
            server = new RestServer(SERVER_PORT);
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
    public boolean isRunning() {
        return server != null;
    }

    @Override
    public ServerConnectionInfo getServerConnectionInfo() {
        String ip = NetworkUtils.getLocalIpAddress();
        return new ServerConnectionInfo(ip, SERVER_PORT);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        if (server != null) {
            server.setListener(listener);
        }
    }

    @Override
    public void setServerCurrentPosition(int newCurrentPosition) { /* ... */ }

    @Override
    public void setServerCurrentSettings(Settings settings) { /* ... */ }
}
