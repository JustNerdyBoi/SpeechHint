package com.example.speechhint;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileManager {
    private static final String TAG = "FileManager";

    /**
     * Loads a document (.docx, or .txt) from a Uri and converts it into a LinkedText object.
     * Each word in the document becomes a node in the LinkedText.
     */
    public static LinkedText loadDocument(Context context, Uri uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("Uri cannot be null");
        }

        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String fileName = uri.getLastPathSegment();
            if (fileName == null) {
                throw new IllegalArgumentException("Cannot determine file type");
            }
            String extension = getFileExtension(fileName);
            if (!isSupportedExtension(extension)) {
                throw new IllegalArgumentException("Unsupported file format: " + extension);
            }
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (!isSupportedMimeType(mimeType)) {
            throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }

        LinkedText text = new LinkedText();
        
        try {
            if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                loadDocxFile(context, uri, text);
            } else if (mimeType.equals("text/plain")) {
                loadTextFile(context, uri, text);
            } else {
                throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading file: " + e.getMessage(), e);
            throw new IOException("Error reading file: " + e.getMessage(), e);
        }

        return text;
    }

    private static void loadDocxFile(Context context, Uri uri, LinkedText text) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Could not open input stream for file");
            }

            try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.getName().equals("word/document.xml")) {
                        // Read the entire content into a byte array first
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipStream.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        
                        // Convert to string using UTF-8
                        String xmlContent = baos.toString(StandardCharsets.UTF_8.name());
                        String textContent = extractTextFromXml(xmlContent);
                        
                        // Split content into words and add to LinkedText
                        String[] words = textContent.split("\\s+");
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                text.addWord(word);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading DOCX file: " + e.getMessage(), e);
            throw new IOException("Error reading DOCX file: " + e.getMessage(), e);
        }
    }

    private static String extractTextFromXml(String xmlContent) {
        StringBuilder text = new StringBuilder();
        boolean inText = false;
        int i = 0;
        while (i < xmlContent.length()) {
            if (xmlContent.startsWith("<w:t", i)) {
                inText = true;
                i = xmlContent.indexOf(">", i) + 1;
            } else if (xmlContent.startsWith("</w:t>", i)) {
                inText = false;
                i += 6;
            } else if (inText) {
                char c = xmlContent.charAt(i);
                // Handle XML entities
                if (c == '&') {
                    int end = xmlContent.indexOf(';', i);
                    if (end > i) {
                        String entity = xmlContent.substring(i, end + 1);
                        switch (entity) {
                            case "&amp;": text.append('&'); break;
                            case "&lt;": text.append('<'); break;
                            case "&gt;": text.append('>'); break;
                            case "&quot;": text.append('"'); break;
                            case "&apos;": text.append('\''); break;
                            default: text.append(c);
                        }
                        i = end + 1;
                        continue;
                    }
                }
                text.append(c);
                i++;
            } else {
                i++;
            }
        }
        return text.toString().replaceAll("\\s+", " ").trim();
    }

    private static void loadTextFile(Context context, Uri uri, LinkedText text) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        text.addWord(word);
                    }
                }
            }
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private static boolean isSupportedExtension(String extension) {
        return extension.equals("txt") || extension.equals("docx");
    }

    private static boolean isSupportedMimeType(String mimeType) {
        return mimeType != null && (
            mimeType.equals("text/plain") ||
            mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
} 