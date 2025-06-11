package ru.application.data.server;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.ServerRepository;

import java.util.Map;

public class RestServer extends NanoHTTPD {
    private final Gson gson = new Gson();
    private ServerRepository.Listener listener;
    private int currentPosition;
    private Settings currentSettings;
    private Document currentDocument;

    public RestServer(int port) {
        super(port);
    }

    public void setListener(ServerRepository.Listener listener) {
        this.listener = listener;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        try {
            if ("/document".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String json = files.get("postData");
                Document document = gson.fromJson(json, Document.class);

                if (listener != null) listener.onDocumentReceived(document);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }
            if ("/settings".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String json = files.get("postData");
                Settings settings = gson.fromJson(json, Settings.class);

                if (listener != null) listener.onSettingsReceived(settings);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }
            if ("/position".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String json = files.get("postData");
                int position = Integer.parseInt(json);

                if (listener != null) listener.onCurrentPositionReceived(position);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"ok\"}");
            }
            if ("/status".equals(uri) && Method.GET.equals(method)) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"running\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
    }

    public void setCurrentPosition(int pos){
        this.currentPosition = pos;
    }

    public void setCurrentSettings(Settings settings){
        this.currentSettings = settings;
    }

    public void setCurrentDocument(Document document){
        this.currentDocument = document;
    }
}
