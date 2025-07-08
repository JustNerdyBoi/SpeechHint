package ru.application.speechhint.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Subscription;
import ru.application.domain.entity.SubscriptionState;
import ru.application.domain.repository.BillingRepository;
import ru.application.domain.usecase.CheckSubscriptionUseCase;
import ru.application.domain.usecase.PurchaseSubscriptionUseCase;

@HiltViewModel
public class BillingViewModel extends ViewModel {
    private static final String[] PRODUCT_IDS = {
            "speech_recognition_purchase",
            "remote_control_purchase"
    };
    private final CheckSubscriptionUseCase checkSubscriptionUseCase;
    private final PurchaseSubscriptionUseCase purchaseSubscriptionUseCase;
    Map<String, Boolean> subscriptionsMap = new HashMap<>();

    @Inject
    public BillingViewModel(CheckSubscriptionUseCase checkSubscriptionUseCase,
                            PurchaseSubscriptionUseCase purchaseSubscriptionUseCase) {
        this.checkSubscriptionUseCase = checkSubscriptionUseCase;
        this.purchaseSubscriptionUseCase = purchaseSubscriptionUseCase;
    }

    public SubscriptionState checkSubscription(String productId) {
        if (subscriptionsMap.containsKey(productId)) {
            return subscriptionsMap.get(productId) ? SubscriptionState.ACTIVE : SubscriptionState.NOT_ACTIVE;
        } else {
            checkAllSubscriptions();
            return SubscriptionState.NO_DATA;
        }
    }

    public void purchaseSubscription(String productId) {
        purchaseSubscriptionUseCase.execute(productId, new BillingRepository.BillingCallback() {
            @Override
            public void onSuccess(Subscription subscription) {
                updateSubscriptionMap(subscription.getProductId(), subscription.isActive());
            }

            @Override
            public void onError(String error) {
                Log.e("BILLING", error);
            }
        });
    }
    public void checkAllSubscriptions() {
        if (PRODUCT_IDS == null) {
            return;
        }

        for (String productId : PRODUCT_IDS) {
            checkSubscriptionUseCase.execute(productId, new BillingRepository.BillingCallback() {
                @Override
                public void onSuccess(Subscription subscription) {
                    updateSubscriptionMap(subscription.getProductId(), subscription.isActive());
                }

                @Override
                public void onError(String error) {
                    Log.e("BILLING", error);
                }
            });
        }
    }
    private void updateSubscriptionMap(String productId, boolean isActive) {
        if (subscriptionsMap == null) {
            subscriptionsMap = new HashMap<>();
        }
        subscriptionsMap.put(productId, isActive);
        Log.i("BILLING", subscriptionsMap.toString());
    }
}