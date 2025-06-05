package ru.application.data.datasource;
import android.util.Log;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;

import java.io.InputStream;
import java.net.URL;
public class GoogleDriveDataSource {
    public Document loadDocument(String googleDriveLink) throws Exception { // TODO: fix links to docx
        String regex = "/d/([a-zA-Z0-9_-]+)";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(googleDriveLink);
        if (!matcher.find()) throw new IllegalArgumentException("Invalid Google Drive link");
        String fileId = matcher.group(1);

        // Construct direct download URL
        String directUrl = "https://drive.google.com/uc?export=download&id=" + fileId;


        // Open InputStream to the file
        InputStream is = new URL(directUrl).openStream();
        String extension = ExtensionReceiver.getExtensionFromUrl(is, directUrl);


        return DocumentParser.parse(is, extension);
    }
}