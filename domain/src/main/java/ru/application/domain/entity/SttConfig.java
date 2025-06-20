package ru.application.domain.entity;

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
        config.setSttEnabled(false);
        config.setSttBeforeBufferSize(5);
        config.setSttAfterBufferSize(10);
        return config;
    }
}
