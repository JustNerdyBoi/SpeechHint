package ru.application.domain.usecase;

import ru.application.domain.repository.BillingRepository;

public class CheckSubscriptionUseCase {
    private final BillingRepository repository;

    public CheckSubscriptionUseCase(BillingRepository repository) {
        this.repository = repository;
    }

    public void execute(String productId, BillingRepository.BillingCallback callback) {
        repository.checkSubscription(productId, callback);
    }
}