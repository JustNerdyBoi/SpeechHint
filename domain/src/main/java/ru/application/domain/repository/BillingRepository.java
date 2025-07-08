package ru.application.domain.repository;

import ru.application.domain.entity.Subscription;

public interface BillingRepository {
    void checkSubscription(String productId, BillingCallback callback);

    void purchaseSubscription(String productId, BillingCallback callback);

    interface BillingCallback {
        void onSuccess(Subscription subscription);

        void onError(String error);
    }
}