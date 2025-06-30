package ru.application.domain.entity;

import ru.application.domain.constants.DefaultConfigs;

public class SttConfig {
    private boolean sttEnabled;
    private int sttBeforeBufferSize;
    private int sttAfterBufferSize;

    public boolean isSttEnabled() {
        return sttEnabled;
    }

    public void setSttEnabled(boolean sttEnabled) {
        this.sttEnabled = sttEnabled;
    }

    public int getSttBeforeBufferSize() {
        return sttBeforeBufferSize;
    }

    public void setSttBeforeBufferSize(int sttBeforeBufferSize) {
        this.sttBeforeBufferSize = sttBeforeBufferSize;
    }

    public int getSttAfterBufferSize() {
        return sttAfterBufferSize;
    }

    public void setSttAfterBufferSize(int sttAfterBufferSize) {
        this.sttAfterBufferSize = sttAfterBufferSize;
    }

    public static SttConfig defaultConfig() {
        SttConfig config = new SttConfig();
        config.setSttEnabled(DefaultConfigs.DEFAULT_STT_ENABLED);
        config.setSttBeforeBufferSize(DefaultConfigs.DEFAULT_STT_BEFORE_BUFFER_SIZE);
        config.setSttAfterBufferSize(DefaultConfigs.DEFAULT_STT_AFTER_BUFFER_SIZE);
        return config;
    }
}
