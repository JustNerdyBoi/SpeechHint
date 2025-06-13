package ru.application.domain.entity;

public class UIConfig {
    private Theme theme;
    private boolean currentStringHighlight;
    private HighlightType highlightType;
    private float highlightHeight;
    private boolean currentWordHighlightFollow;
    private int textScale;
    private boolean mirrorText;

    public boolean isCurrentStringHighlight() {
        return currentStringHighlight;
    }

    public void setCurrentStringHighlight(boolean currentStringHighlight) {
        this.currentStringHighlight = currentStringHighlight;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
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
        config.setHighlightType(HighlightType.LINE);
        config.setHighlightHeight(0.5f);
        config.setCurrentWordHighlightFollow(true);
        config.setMirrorText(false);
        config.setTheme(Theme.DARK);
        config.setTextScale(20);
        return config;
    }

    public boolean isMirrorText() {
        return mirrorText;
    }

    public void setMirrorText(boolean mirrorText) {
        this.mirrorText = mirrorText;
    }

    public HighlightType getHighlightType() {
        return highlightType;
    }

    public void setHighlightType(HighlightType highlightType) {
        this.highlightType = highlightType;
    }

    public boolean isCurrentWordHighlightFollow() {
        return currentWordHighlightFollow;
    }

    public void setCurrentWordHighlightFollow(boolean currentWordHighlightFollow) {
        this.currentWordHighlightFollow = currentWordHighlightFollow;
    }

    public float getHighlightHeight() {
        return highlightHeight;
    }

    public void setHighlightHeight(float highlightHeight) {
        this.highlightHeight = highlightHeight;
    }
}
