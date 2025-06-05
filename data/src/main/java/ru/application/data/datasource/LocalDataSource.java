package ru.application.data.datasource;

import android.content.Context;
import android.net.Uri;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;
import java.io.*;

public class LocalDataSource {

    private final Context context;

    public LocalDataSource(Context context) {
        this.context = context.getApplicationContext();
    }

    public Document loadDocument(Uri uri) throws Exception {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            String extension = ExtensionReceiver.getExtensionFromUri(context, uri);
            return DocumentParser.parse(is, extension);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
