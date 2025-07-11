package ru.application.data.datasource;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import ru.application.data.utils.DocumentParser;
import ru.application.domain.entity.Document;

public class YandexDriveDataSource {

    public static final String YANDEX_BASIC_DOWNLOAD_LINK = "https://cloud-api.yandex.net/v1/disk/public/resources/download?public_key=";

    public Document loadDocument(String yandexPublicLink) throws Exception {
        String apiUrl = YANDEX_BASIC_DOWNLOAD_LINK + URLEncoder.encode(yandexPublicLink, StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JSONObject json = new JSONObject(sb.toString());
        String downloadUrl = json.getString("href");
        
        InputStream is = new URL(downloadUrl).openStream();

        return DocumentParser.parse(is);
    }
}
