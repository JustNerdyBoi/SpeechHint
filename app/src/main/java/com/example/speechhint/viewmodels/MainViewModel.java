package com.example.speechhint.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.speechhint.config.DefaultConfigs;

public class MainViewModel extends AndroidViewModel {
    private final SharedPreferences prefs;
    
    // LiveData для настроек
    private final MutableLiveData<Integer> textScale = new MutableLiveData<>();
    private final MutableLiveData<Boolean> useVoiceDetection = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCurrentWord = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showBuffer = new MutableLiveData<>();
    private final MutableLiveData<Integer> autoscrollSpeed = new MutableLiveData<>();
    private final MutableLiveData<Integer> beforeBufferSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> afterBufferSize = new MutableLiveData<>();
    private final MutableLiveData<Boolean> useAutoscroll = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        loadSettings();
    }

    private void loadSettings() {
        textScale.setValue(prefs.getInt(DefaultConfigs.KEY_TEXT_SCALE, DefaultConfigs.DEFAULT_TEXT_SCALE));
        useVoiceDetection.setValue(prefs.getBoolean(DefaultConfigs.KEY_USE_VOICE_DETECTION, DefaultConfigs.DEFAULT_USE_VOICE_DETECTION));
        showCurrentWord.setValue(prefs.getBoolean(DefaultConfigs.KEY_SHOW_CURRENT_WORD, DefaultConfigs.DEFAULT_SHOW_CURRENT_WORD));
        showBuffer.setValue(prefs.getBoolean(DefaultConfigs.KEY_SHOW_BUFFER, DefaultConfigs.DEFAULT_SHOW_BUFFER));
        autoscrollSpeed.setValue(prefs.getInt(DefaultConfigs.KEY_AUTOSCROLL_SPEED, DefaultConfigs.DEFAULT_AUTOSCROLL_SPEED));
        beforeBufferSize.setValue(prefs.getInt(DefaultConfigs.KEY_BEFORE_BUFFER_SIZE, DefaultConfigs.DEFAULT_BEFORE_BUFFER_SIZE));
        afterBufferSize.setValue(prefs.getInt(DefaultConfigs.KEY_AFTER_BUFFER_SIZE, DefaultConfigs.DEFAULT_AFTER_BUFFER_SIZE));
        useAutoscroll.setValue(prefs.getBoolean(DefaultConfigs.KEY_USE_AUTOSCROLL, DefaultConfigs.DEFAULT_USE_AUTOSCROLL));
    }

    // Геттеры для LiveData
    public LiveData<Integer> getTextScale() {
        return textScale;
    }

    public LiveData<Boolean> getUseVoiceDetection() {
        return useVoiceDetection;
    }

    public LiveData<Boolean> getShowCurrentWord() {
        return showCurrentWord;
    }

    public LiveData<Boolean> getShowBuffer() {
        return showBuffer;
    }

    public LiveData<Integer> getAutoscrollSpeed() {
        return autoscrollSpeed;
    }

    public LiveData<Integer> getBeforeBufferSize() {
        return beforeBufferSize;
    }

    public LiveData<Integer> getAfterBufferSize() {
        return afterBufferSize;
    }

    public LiveData<Boolean> getUseAutoscroll() {
        return useAutoscroll;
    }

    // Методы для обновления настроек
    public void updateTextScale(int scale) {
        textScale.setValue(scale);
        prefs.edit().putInt(DefaultConfigs.KEY_TEXT_SCALE, scale).apply();
    }

    public void updateUseVoiceDetection(boolean use) {
        useVoiceDetection.setValue(use);
        prefs.edit().putBoolean(DefaultConfigs.KEY_USE_VOICE_DETECTION, use).apply();
    }

    public void updateShowCurrentWord(boolean show) {
        showCurrentWord.setValue(show);
        prefs.edit().putBoolean(DefaultConfigs.KEY_SHOW_CURRENT_WORD, show).apply();
    }

    public void updateShowBuffer(boolean show) {
        showBuffer.setValue(show);
        prefs.edit().putBoolean(DefaultConfigs.KEY_SHOW_BUFFER, show).apply();
    }

    public void updateAutoscrollSpeed(int speed) {
        autoscrollSpeed.setValue(speed);
        prefs.edit().putInt(DefaultConfigs.KEY_AUTOSCROLL_SPEED, speed).apply();
    }

    public void updateBeforeBufferSize(int size) {
        beforeBufferSize.setValue(size);
        prefs.edit().putInt(DefaultConfigs.KEY_BEFORE_BUFFER_SIZE, size).apply();
    }

    public void updateAfterBufferSize(int size) {
        afterBufferSize.setValue(size);
        prefs.edit().putInt(DefaultConfigs.KEY_AFTER_BUFFER_SIZE, size).apply();
    }

    public void updateUseAutoscroll(boolean use) {
        useAutoscroll.setValue(use);
        prefs.edit().putBoolean(DefaultConfigs.KEY_USE_AUTOSCROLL, use).apply();
    }
} 