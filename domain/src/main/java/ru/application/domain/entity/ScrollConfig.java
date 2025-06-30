package ru.application.domain.entity;

import ru.application.domain.constants.DefaultConfigs;

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
        config.setEnableAutoScroll(DefaultConfigs.DEFAULT_ENABLE_AUTO_SCROLL);
        config.setSpeed(DefaultConfigs.DEFAULT_SPEED);
        return config;
    }
}