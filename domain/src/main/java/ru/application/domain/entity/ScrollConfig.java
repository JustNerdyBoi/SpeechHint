package ru.application.domain.entity;

public class ScrollConfig {
    private float speed;
    private boolean enableAutoScroll;
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isEnableAutoScroll() {
        return enableAutoScroll;
    }

    public void setEnableAutoScroll(boolean enableAutoScroll) {
        this.enableAutoScroll = enableAutoScroll;
    }

    public static ScrollConfig defaultConfig() {
        ScrollConfig config = new ScrollConfig();
        config.setEnableAutoScroll(false);
        config.setSpeed(100.0f);
        return config;
    }
}