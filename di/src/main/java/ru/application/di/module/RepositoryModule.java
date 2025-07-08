package ru.application.di.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import ru.application.data.datasource.FileDataSource;
import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.LocalDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.data.repository.BillingRepositoryImpl;
import ru.application.data.repository.DocumentRepositoryImpl;
import ru.application.data.repository.ServerRepositoryImpl;
import ru.application.data.repository.SettingsRepositoryImpl;
import ru.application.data.repository.SpeechRecognitionRepositoryImpl;
import ru.application.domain.repository.BillingRepository;
import ru.application.domain.repository.DocumentRepository;
import ru.application.domain.repository.ServerRepository;
import ru.application.domain.repository.SettingsRepository;
import ru.application.domain.repository.SpeechRecognitionRepository;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public DocumentRepository provideDocumentRepository(
            LocalDataSource localDataSource,
            GoogleDriveDataSource googleDriveDataSource,
            YandexDriveDataSource yandexDriveDataSource
    ) {
        return new DocumentRepositoryImpl(
                localDataSource,
                googleDriveDataSource,
                yandexDriveDataSource
        );
    }

    @Provides
    @Singleton
    public SpeechRecognitionRepository provideSpeechRecognitionRepository(@ApplicationContext Context context) {
        return new SpeechRecognitionRepositoryImpl(context);
    }

    @Singleton
    @Provides
    public SettingsRepository provideSettingsRepository(FileDataSource fileDataSource) {
        return new SettingsRepositoryImpl(fileDataSource);
    }

    @Provides
    @Singleton
    public ServerRepository provideServerRepository(@ApplicationContext Context context) {
        return new ServerRepositoryImpl(context);
    }

    @Singleton
    @Provides
    public BillingRepository provideBillingRepository(RuStoreBillingClient billingClient) {
        return new BillingRepositoryImpl(billingClient);
    }

}