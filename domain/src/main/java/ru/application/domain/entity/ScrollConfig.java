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
}