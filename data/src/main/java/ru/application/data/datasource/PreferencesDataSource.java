package ru.application.data.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import ru.application.domain.entity.Settings;

public class PreferencesDataSource {
    private static final String KEY_SETTINGS = "settings";
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public PreferencesDataSource(Context context) {
        sharedPreferences = context.getSharedPreferences("teleprompter_prefs", Context.MODE_PRIVATE);
    }

    public Settings getSettings() {
        String json = sharedPreferences.getString(KEY_SETTINGS, null);
        if (json == null) return Settings.defaultSettings();
        try {
            Log.i("SETTINGS", json);
            return gson.fromJson(json, Settings.class);
        } catch (Exception e){
            return Settings.defaultSettings();
        }
    }

    public void saveSettings(Settings settings) {
        String json = gson.toJson(settings);
        sharedPreferences.edit().putString(KEY_SETTINGS, json).apply();
    }
}
