package ru.application.domain.usecase;

import ru.application.domain.repository.BillingRepository;

public class PurchaseSubscriptionUseCase {
    private final BillingRepository repository;

    public PurchaseSubscriptionUseCase(BillingRepository repository) {
        this.repository = repository;
    }

    public void execute(String productId, BillingRepository.BillingCallback callback) {
        repository.purchaseSubscription(productId, callback);
    }
}