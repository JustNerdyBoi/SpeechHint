package ru.application.data.repository;

import java.util.List;

import ru.application.domain.entity.Subscription;
import ru.application.domain.repository.BillingRepository;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase;

public class BillingRepositoryImpl implements BillingRepository {
    private final PurchasesUseCase purchasesUseCase;

    public BillingRepositoryImpl(RuStoreBillingClient billingClient) {
        this.purchasesUseCase = billingClient.getPurchases();
    }

    @Override
    public void checkSubscription(String productId, BillingCallback callback) {
        purchasesUseCase.getPurchases().addOnSuccessListener(purchases -> {
            callback.onSuccess(new Subscription(productId, isPurchaseActive(purchases, productId)));
        }).addOnFailureListener(throwable -> callback.onError(throwable.getMessage()));
    }

    @Override
    public void purchaseSubscription(String productId, BillingCallback callback) {
        purchasesUseCase.purchaseProduct(productId, null, 1, null).addOnSuccessListener(result -> {
            if (result instanceof PaymentResult.Success) {
                checkSubscription(productId, callback);
            } else if (result instanceof PaymentResult.Cancelled) {
                callback.onError("Payment cancelled");
            } else if (result instanceof PaymentResult.Failure) {
                callback.onError("Payment failed");
            } else if (result instanceof PaymentResult.InvalidPaymentState) {
                callback.onError("Invalid payment state");
            }
        }).addOnFailureListener(throwable -> callback.onError(throwable.getMessage()));
    }

    private boolean isPurchaseActive(List<Purchase> purchases, String productId) {
        for (Purchase purchase : purchases) {
            if (productId.equals(purchase.getProductId()) && purchase.getPurchaseState() == PurchaseState.CONFIRMED) {
                return true;
            }
        }
        return false;
    }
}