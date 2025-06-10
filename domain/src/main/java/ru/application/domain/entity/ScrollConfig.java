package ru.application.domain.entity;

public class ScrollConfig {
    private float speed;
    private boolean autoScroll; // false = stop, true = scroll

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isAutoScroll() {
        return autoScroll;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public static ScrollConfig defaultConfig() {
        ScrollConfig config = new ScrollConfig();
        config.setAutoScroll(false);
        config.setSpeed(100.0f);
        return config;
    }
}