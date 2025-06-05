package ru.application.data.utils;

import android.net.Uri;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class ExtensionReceiver {
    public static String getExtensionFromUri(Context context, Uri uri) {
        String extension = null;

        // First, try to get extension from Uri
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            // Try to get extension from mime type
            ContentResolver cr = context.getContentResolver();
            String mime = cr.getType(uri);
            if (mime != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            }

            // If that didn't work, try to get file name and extract extension
            if (extension == null) {
                Cursor cursor = cr.query(uri, null, null, null, null);
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        String name = cursor.getString(nameIndex);
                        int dotIndex = name.lastIndexOf('.');
                        if (dotIndex != -1) {
                            extension = name.substring(dotIndex + 1);
                        }
                    }
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            String path = uri.getPath();
            if (path != null) {
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex != -1) {
                    extension = path.substring(dotIndex + 1);
                }
            }
        }

        return extension;
    }

    public static String getExtensionFromUrl(InputStream is, String fileNameOrUrl) throws Exception { // TODO: make this stuff work
        String mime = null;
        if (fileNameOrUrl != null) {
            mime = getMimeTypeFromUrl(fileNameOrUrl);
        }
        if (mime == null) {
            mime = guessMimeType(is);
        }
        return mime != null ? mime : "application/octet-stream";
    }

    public static String guessMimeType(InputStream is) throws IOException {
        // Mark the stream to reset after reading
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(1024);
        String mime = URLConnection.guessContentTypeFromStream(is);
        is.reset();
        return mime;
    }
    public static String getMimeTypeFromUrl(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}