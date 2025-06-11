package ru.application.data.server;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.iki.elonen.NanoHTTPD;
import ru.application.data.R;
import ru.application.data.datasource.DocumentParser;
import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.ServerRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RestServer extends NanoHTTPD {
    private final Gson gson = new Gson();
    private ServerRepository.Listener listener;
    private int currentPosition;
    private Settings currentSettings;
    private Document currentDocument;
    private final Context context;

    public RestServer(int port, Context context) {
        super(port);
        this.context = context;
    }

    public void setListener(ServerRepository.Listener listener) {
        this.listener = listener;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        try {
            if ("/document/set/file/".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> files = new java.util.HashMap<>();
                try {
                    session.parseBody(files);
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
                }

                String tmpFilePath = files.get("file");
                if (tmpFilePath == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"No file uploaded\"}");
                }

                File uploadedFile = new File(tmpFilePath);

                Document document;
                try (InputStream is = new FileInputStream(uploadedFile)) {
                    document = DocumentParser.parse(is);
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                }

                if (listener != null) {
                    listener.onDocumentReceived(document);
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }

            if ("/document/set/yandex-disk/".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> bodyMap = new HashMap<>();
                session.parseBody(bodyMap);
                String body = bodyMap.get("postData");
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String link = json.get("yandexDiskLink").getAsString();
                try {
                    YandexDriveDataSource yandexDriveDataSource = new YandexDriveDataSource();
                    listener.onDocumentReceived(yandexDriveDataSource.loadDocument(link));
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }

            if ("/document/set/google-drive/".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> bodyMap = new HashMap<>();
                session.parseBody(bodyMap);
                String body = bodyMap.get("postData");
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String link = json.get("googleDriveLink").getAsString();
                try {
                    GoogleDriveDataSource googleDriveDataSource = new GoogleDriveDataSource();
                    listener.onDocumentReceived(googleDriveDataSource.loadDocument(link));
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }

            if ("/document/get/".equals(uri) && Method.GET.equals(method)) {
                return newFixedLengthResponse(Response.Status.OK, "text/plain", currentDocument == null ? gson.toJson(new Document()) : gson.toJson(currentDocument));
            }

            if ("/settings/set/".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String json = files.get("postData");
                Settings settings = gson.fromJson(json, Settings.class);

                if (listener != null) listener.onSettingsReceived(settings);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }

            if ("/settings/get/".equals(uri) && Method.GET.equals(method)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(currentSettings));
            }

            if ("/position/set/".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> bodyMap = new HashMap<>();
                session.parseBody(bodyMap);
                String body = bodyMap.get("postData");
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                int position = json.get("position").getAsInt();
                if (listener != null) listener.onCurrentPositionReceived(position);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }

            if ("/position/get/".equals(uri) && Method.GET.equals(method)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"position\":\"" + currentPosition + "\"}");
            }

            switch (uri) {
                case "/panel/":
                case "panel/index.html":
                    return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", loadFileFromRaw(context, R.raw.index));
                case "/panel/style.css":
                    return newFixedLengthResponse(Response.Status.OK, "text/css; charset=UTF-8", loadFileFromRaw(context, R.raw.style));
                case "/panel/app.js":
                    return newFixedLengthResponse(Response.Status.OK, "application/javascript; charset=UTF-8", loadFileFromRaw(context, R.raw.app));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
    }

    public void setCurrentPosition(int pos) {
        this.currentPosition = pos;
    }

    public void setCurrentSettings(Settings settings) {
        this.currentSettings = settings;
    }

    public void setCurrentDocument(Document document) {
        this.currentDocument = document;
    }

    public static String loadFileFromRaw(Context context, int rawResId) {
        try (InputStream is = context.getResources().openRawResource(rawResId)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
