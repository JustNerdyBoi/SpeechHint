package ru.application.data.server;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import ru.application.data.R;
import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.data.utils.DocumentParser;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.ServerRepository;

public final class RestServer extends NanoHTTPD {
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
                return handleGetRequests(uri);
            } else if (Method.POST.equals(method)) {
                return handlePostRequests(uri, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
    }

    private Response handleGetRequests(String uri) {
        switch (uri) {
            case "/":
                return serveFile(R.raw.redirector, "text/html; charset=UTF-8");
            case "/panel/":
            case "/panel/index_panel.html":
                return serveFile(R.raw.index_panel, "text/html; charset=UTF-8");
            case "/panel/style_panel.css":
                return serveFile(R.raw.style_panel, "text/css; charset=UTF-8");
            case "/panel/app_panel.js":
                return serveFile(R.raw.app_panel, "application/javascript; charset=UTF-8");
            case "/remote/":
            case "/remote/index_remote.html":
                return serveFile(R.raw.index_remote, "text/html; charset=UTF-8");
            case "/remote/style_remote.css":
                return serveFile(R.raw.style_remote, "text/css; charset=UTF-8");
            case "/remote/app_remote.js":
                return serveFile(R.raw.app_remote, "application/javascript; charset=UTF-8");
            case "/document/get/":
                return serveJson(currentDocument == null ? new Document() : currentDocument);
            case "/settings/get/":
                return serveJson(currentSettings);
            case "/position/get/":
                return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"position\":\"" + currentPosition + "\"}");
            default:
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
        }
    }

    private Response handlePostRequests(String uri, IHTTPSession session) throws Exception {
        switch (uri) {
            case "/document/set/json/":
                return handleSetJsonDocument(session);
            case "/document/set/file/":
                return handleSetFileDocument(session);
            case "/document/set/yandex-disk/":
                return handleSetYandexDiskDocument(session);
            case "/document/set/google-drive/":
                return handleSetGoogleDriveDocument(session);
            case "/settings/set/":
                return handleSetSettings(session);
            case "/position/set/":
                return handleSetPosition(session);
            case "/scroll/":
                return handleScroll(session);
            default:
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
        }
    }

    private Response handleSetJsonDocument(IHTTPSession session) throws IOException {
        String json = readRequestBody(session);
        Log.i("SERVER", json);
        if (listener != null) {
            listener.onDocumentReceived(gson.fromJson(json, Document.class));
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
    }

    private Response handleSetFileDocument(IHTTPSession session) throws Exception {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String tmpFilePath = files.get("file");
        if (tmpFilePath == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json; charset=UTF-8", "{\"error\":\"No file uploaded\"}");
        }

        try (InputStream is = new FileInputStream(new File(tmpFilePath))) {
            Document document = DocumentParser.parse(is);
            if (listener != null) {
                listener.onDocumentReceived(document);
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
        }
    }

    private Response handleSetYandexDiskDocument(IHTTPSession session) throws Exception {
        Map<String, String> bodyMap = new HashMap<>();
        session.parseBody(bodyMap);
        String body = bodyMap.get("postData");
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        String link = json.get("yandexDiskLink").getAsString();

        try {
            YandexDriveDataSource yandexDriveDataSource = new YandexDriveDataSource();
            Document document = yandexDriveDataSource.loadDocument(link);
            if (listener != null) {
                listener.onDocumentReceived(document);
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
        }
    }

    private Response handleSetGoogleDriveDocument(IHTTPSession session) throws Exception {
        Map<String, String> bodyMap = new HashMap<>();
        session.parseBody(bodyMap);
        String body = bodyMap.get("postData");
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        String link = json.get("googleDriveLink").getAsString();

        try {
            GoogleDriveDataSource googleDriveDataSource = new GoogleDriveDataSource();
            Document document = googleDriveDataSource.loadDocument(link);
            if (listener != null) {
                listener.onDocumentReceived(document);
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json; charset=UTF-8", "{\"error\":\"Failed to parse file: " + e.getMessage() + "\"}");
        }
    }

    private Response handleSetSettings(IHTTPSession session) throws IOException {
        String json = readRequestBody(session);
        if (listener != null) {
            listener.onSettingsReceived(gson.fromJson(json, Settings.class));
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
    }

    private Response handleSetPosition(IHTTPSession session) throws IOException {
        String body = readRequestBody(session);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        if (listener != null) {
            listener.onCurrentPositionReceived(json.get("position").getAsInt());
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
    }

    private Response handleScroll(IHTTPSession session) throws IOException {
        String body = readRequestBody(session);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        if (listener != null) {
            listener.onScrollReceived(json.get("scroll").getAsFloat());
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", "{\"status\":\"ok\"}");
    }

    private String readRequestBody(IHTTPSession session) throws IOException {
        InputStream inputStream = session.getInputStream();
        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buffer = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = inputStream.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }
        return new String(buffer, 0, totalRead, StandardCharsets.UTF_8);
    }

    private Response serveFile(int resourceId, String mimeType) {
        return newFixedLengthResponse(Response.Status.OK, mimeType, loadFileFromRaw(context, resourceId));
    }

    private Response serveJson(Object obj) {
        return newFixedLengthResponse(Response.Status.OK, "application/json; charset=UTF-8", gson.toJson(obj));
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
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
