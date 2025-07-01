package ru.application.data.datasource;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ru.application.domain.entity.Settings;

public class FileDataSource {
    private static final String FILE_NAME = "settings.json";
    private final File settingsFile;
    private final Gson gson = new Gson();

    public FileDataSource(Context context) {
        settingsFile = new File(context.getFilesDir(), FILE_NAME);
    }

    public Settings getSettings() {
        if (!settingsFile.exists()) {
            return Settings.defaultSettings();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            return gson.fromJson(reader, Settings.class);
        } catch (IOException e) {
            Log.e("FileDataSource", "Error reading settings file", e);
            return Settings.defaultSettings();
        }
    }

    public void saveSettings(Settings settings) {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            Log.e("FileDataSource", "Error writing settings file", e);
        }
    }
}
