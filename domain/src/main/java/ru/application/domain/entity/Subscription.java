package ru.application.domain.entity;

public class Subscription {
    private final String productId;
    private final boolean isActive;

    public Subscription(String productId, boolean isActive) {
        this.productId = productId;
        this.isActive = isActive;
    }

    public String getProductId() {
        return productId;
    }

    public boolean isActive() {
        return isActive;
    }
}