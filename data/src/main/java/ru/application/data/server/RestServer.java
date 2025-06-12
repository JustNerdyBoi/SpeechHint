package ru.application.data.server;

import android.content.Context;
import android.util.Log;

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
            if (Method.GET.equals(method)) {
                switch (uri) {
                    case "/":
                        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", loadFileFromRaw(context, R.raw.redirector));
                    case "/panel/":
                    case "/panel/index_panel.html":
                        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", loadFileFromRaw(context, R.raw.index_panel));
                    case "/panel/style_panel.css":
                        return newFixedLengthResponse(Response.Status.OK, "text/css; charset=UTF-8", loadFileFromRaw(context, R.raw.style_panel));
                    case "/panel/app_panel.js":
                        return newFixedLengthResponse(Response.Status.OK, "application/javascript; charset=UTF-8", loadFileFromRaw(context, R.raw.app_panel));
                    case "/remote/":
                    case "/remote/index_remote.html":
                        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", loadFileFromRaw(context, R.raw.index_remote));
                    case "/remote/style_remote.css":
                        return newFixedLengthResponse(Response.Status.OK, "text/css; charset=UTF-8", loadFileFromRaw(context, R.raw.style_remote));
                    case "/remote/app_remote.js":
                        return newFixedLengthResponse(Response.Status.OK, "application/javascript; charset=UTF-8", loadFileFromRaw(context, R.raw.app_remote));
                    case "/document/get/":
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", currentDocument == null ? gson.toJson(new Document()) : gson.toJson(currentDocument));
                    case "/settings/get/":
                        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", gson.toJson(currentSettings));
                    case "/position/get/":
                        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"position\":\"" + currentPosition + "\"}");
                }
            } else if (Method.POST.equals(method)) {
                if (uri.equals("/document/set/json/")) {
                    InputStream inputStream = session.getInputStream();
                    int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                    byte[] buffer = new byte[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        int read = inputStream.read(buffer, totalRead, contentLength - totalRead);
                        if (read == -1) break;
                        totalRead += read;
                    }
                    String json = new String(buffer, 0, totalRead, "UTF-8");

                    Log.i("SERVER", json);

                    if (listener != null)
                        listener.onDocumentReceived(gson.fromJson(json, Document.class));
                } else if (uri.equals("/document/set/file/")) {
                    Map<String, String> files = new HashMap<>();
                    try {
                        session.parseBody(files);
                    } catch (Exception e) {
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
                    }

                    String tmpFilePath = files.get("file");
                    if (tmpFilePath == null) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json; charset=UTF-8", "{\"error\":\"No file uploaded\"}");
                    }

                    File uploadedFile = new File(tmpFilePath);

                    Document document;
                    try (InputStream is = new FileInputStream(uploadedFile)) {
                        document = DocumentParser.parse(is);
                    } catch (Exception e) {
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                    }

                    if (listener != null) {
                        listener.onDocumentReceived(document);
                    }
                    return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");

                } else if ("/document/set/yandex-disk/".equals(uri)) {
                    Map<String, String> bodyMap = new HashMap<>();
                    session.parseBody(bodyMap);
                    String body = bodyMap.get("postData");
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String link = json.get("yandexDiskLink").getAsString();
                    try {
                        YandexDriveDataSource yandexDriveDataSource = new YandexDriveDataSource();
                        listener.onDocumentReceived(yandexDriveDataSource.loadDocument(link));
                    } catch (Exception e) {
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                    }
                    return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
                } else if ("/document/set/google-drive/".equals(uri)) {
                    Map<String, String> bodyMap = new HashMap<>();
                    session.parseBody(bodyMap);
                    String body = bodyMap.get("postData");
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String link = json.get("googleDriveLink").getAsString();
                    try {
                        GoogleDriveDataSource googleDriveDataSource = new GoogleDriveDataSource();
                        listener.onDocumentReceived(googleDriveDataSource.loadDocument(link));
                    } catch (Exception e) {
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
                    }
                    return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
                } else if ("/settings/set/".equals(uri)) {
                    Map<String, String> files = new java.util.HashMap<>();
                    session.parseBody(files);
                    String json = files.get("postData");
                    Log.i("SERVER", json);

                    if (listener != null)
                        listener.onSettingsReceived(gson.fromJson(json, Settings.class));

                    return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
                } else if ("/position/set/".equals(uri)) {
                    Map<String, String> bodyMap = new HashMap<>();
                    session.parseBody(bodyMap);
                    String body = bodyMap.get("postData");
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (listener != null)
                        listener.onCurrentPositionReceived(json.get("position").getAsInt());

                    return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
                } else if ("/scroll/".equals(uri)) {
                    Map<String, String> bodyMap = new HashMap<>();
                    session.parseBody(bodyMap);
                    String body = bodyMap.get("postData");
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (listener != null)
                        listener.onScrollReceived(json.get("scroll").getAsFloat());
                }
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
