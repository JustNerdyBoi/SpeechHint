package ru.application.domain.entity;

import ru.application.domain.constants.DefaultConfigs;

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

    public static UIConfig defaultConfig() {
        UIConfig config = new UIConfig();
        config.setCurrentStringHighlight(DefaultConfigs.DEFAULT_CURRENT_STRING_HIGHLIGHT);
        config.setHighlightType(DefaultConfigs.DEFAULT_HIGHLIGHT_TYPE);
        config.setHighlightHeight(DefaultConfigs.DEFAULT_HIGHLIGHT_HEIGHT);
        config.setCurrentWordHighlightFollow(DefaultConfigs.DEFAULT_CURRENT_WORD_HIGHLIGHT_FOLLOW);
        config.setMirrorText(DefaultConfigs.DEFAULT_MIRROR_TEXT);
        config.setTheme(DefaultConfigs.DEFAULT_THEME);
        config.setTextScale(DefaultConfigs.DEFAULT_TEXT_SCALE);
        return config;
    }
}
