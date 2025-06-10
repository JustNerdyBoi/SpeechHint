package ru.application.domain.entity;

public class UIConfig {
    private boolean currentStringHighlight;
    private String theme; // "DARK", "LIGHT"
    private int textScale;
    private boolean mirrorText;

    public boolean isCurrentStringHighlight() {
        return currentStringHighlight;
    }

    public void setCurrentStringHighlight(boolean currentStringHighlight) {
        this.currentStringHighlight = currentStringHighlight;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getTextScale() {
        return textScale;
    }

    public void setTextScale(int textScale) {
        this.textScale = textScale;
    }

    public static UIConfig defaultConfig() {
        UIConfig config = new UIConfig();
        config.setCurrentStringHighlight(false);
        config.setMirrorText(false);
        config.setTheme("DARK");
        config.setTextScale(20);
        return config;
    }

    public boolean isMirrorText() {
        return mirrorText;
    }

    public void setMirrorText(boolean mirrorText) {
        this.mirrorText = mirrorText;
    }
}
