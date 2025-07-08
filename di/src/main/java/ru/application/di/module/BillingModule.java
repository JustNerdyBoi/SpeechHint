package ru.application.di.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;

@Module
@InstallIn(SingletonComponent.class)
public class BillingModule {
    final String CONSOLE_APP_ID = "2063641768";
    final String DEEPLINK_SCHEME = "billingscheme";

    @Provides
    @Singleton
    public RuStoreBillingClient provideRuStoreBillingClient(@ApplicationContext Context context) {
        return RuStoreBillingClientFactory.INSTANCE.create(context, CONSOLE_APP_ID, DEEPLINK_SCHEME);
    }
}
