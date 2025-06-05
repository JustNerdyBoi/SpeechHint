package ru.application.data.datasource;

import android.content.Context;
import android.net.Uri;

import ru.application.domain.entity.Document;

public class ServerDataSource {
    private final Context context;

    public ServerDataSource(Context context) {
        this.context = context;
    }

    public Document loadDocument(Uri uri) throws Exception {
        // TODO: реализовать получение InputStream и имени файла с сервера
        throw new UnsupportedOperationException("Server document loading not implemented yet");
    }
}