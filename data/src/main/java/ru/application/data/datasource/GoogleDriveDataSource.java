package ru.application.data.datasource;
import android.util.Log;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;

import java.io.InputStream;
import java.net.URL;
public class GoogleDriveDataSource {
    public static String extractId(String url) {
        String regex = "/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        regex = "/(document|file|spreadsheets|presentation)/d/([a-zA-Z0-9-_]+)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
    public Document loadDocument(String googleDriveLink) throws Exception { // TODO: fix links to docx
        String fileId = extractId(googleDriveLink);

        // Construct direct download URL
        String directUrl = "https://drive.google.com/uc?export=download&id=" + fileId;


        // Open InputStream to the file
        InputStream is = new URL(directUrl).openStream();
        String extension = ExtensionReceiver.getExtensionFromUrl(is, directUrl);


        return DocumentParser.parse(is, extension);
    }
}
