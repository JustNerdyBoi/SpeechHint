package ru.application.data.datasource;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.application.data.utils.DocumentParser;
import ru.application.domain.entity.Document;

public class GoogleDriveDataSource {

    public static final String GOOGLE_BASIC_DOWNLOAD_LINK = "https://drive.google.com/uc?export=download&id=";
    public static final String GOOGLE_DOWNLOAD_POSTFIX = "&export=download&authuser=0";
    public static final String ID_FILTER_REGEX = "/(document|file|spreadsheets|presentation)/d/([a-zA-Z0-9-_]+)";


    public static String extractId(String url) {
        String regex = "/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        pattern = Pattern.compile(ID_FILTER_REGEX);
        matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
    public Document loadDocument(String googleDriveLink) throws Exception {
        String fileId = extractId(googleDriveLink);

        String directUrl = GOOGLE_BASIC_DOWNLOAD_LINK + fileId + GOOGLE_DOWNLOAD_POSTFIX;

        InputStream is = new URL(directUrl).openStream();

        return DocumentParser.parse(is);
    }
}
