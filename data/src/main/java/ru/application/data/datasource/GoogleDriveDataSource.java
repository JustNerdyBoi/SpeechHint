package ru.application.data.datasource;

import android.util.Log;

import ru.application.data.utils.DocumentParser;
import ru.application.domain.entity.Document;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Document loadDocument(String googleDriveLink) throws Exception {
        String fileId = extractId(googleDriveLink);

        // Construct direct download URL
        String directUrl = "https://drive.google.com/uc?export=download&id=" + fileId + "&export=download&authuser=0";  // TODO: fix links to TXT, somehow returns HTML

        Log.i("directUrlLog", directUrl);


        // Open InputStream to the file
        InputStream is = new URL(directUrl).openStream();

        return DocumentParser.parse(is);
    }
}
