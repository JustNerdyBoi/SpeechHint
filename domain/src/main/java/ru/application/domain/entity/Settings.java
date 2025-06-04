package ru.application.domain.entity;

public class Settings {
    private ScrollConfig scrollConfig;
    private SttConfig sttConfig;
    private UIConfig uiConfig;

    public ScrollConfig getScrollConfig() {
        return scrollConfig;
    }

    public void setScrollConfig(ScrollConfig scrollConfig) {
        this.scrollConfig = scrollConfig;
    }

    public SttConfig getSttConfig() {
        return sttConfig;
    }

    public void setSttConfig(SttConfig sttConfig) {
        this.sttConfig = sttConfig;
    }

    public UIConfig getUiConfig() {
        return uiConfig;
    }

    public void setUiConfig(UIConfig uiConfig) {
        this.uiConfig = uiConfig;
    }
}