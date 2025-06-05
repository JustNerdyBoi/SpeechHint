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

    public static String getExtensionFromUri(InputStream inputStream,Uri uri) throws IOException {
        if (inputStream == null) {throw new IllegalArgumentException("Input stream cannot be null");}
        byte[] header = new byte[4];
        int bytesRead = inputStream.read(header, 0, 4);
        if (bytesRead >= 4) {
            if ((header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) ||  // PK\x03\x04
                (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x05 && header[3] == 0x06) ||  // PK\x05\x06
                (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x07 && header[3] == 0x08)) {  // PK\x07\x08
                return "docx";
            }
            return "txt";
        } else {
            return "txt";
        }
    }
}
