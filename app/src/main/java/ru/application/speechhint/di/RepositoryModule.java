package ru.application.speechhint.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.LocalDataSource;
import ru.application.data.datasource.PreferencesDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.data.repository.DocumentRepositoryImpl;
import ru.application.data.repository.ServerRepositoryImpl;
import ru.application.data.repository.SettingsRepositoryImpl;
import ru.application.data.repository.SpeechRecognitionRepositoryImpl;
import ru.application.domain.repository.DocumentRepository;
import ru.application.domain.repository.ServerRepository;
import ru.application.domain.repository.SettingsRepository;
import ru.application.domain.repository.SpeechRecognitionRepository;

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
    public SettingsRepository provideSettingsRepository(PreferencesDataSource preferencesDataSource){
        return new SettingsRepositoryImpl(preferencesDataSource);
    }

    @Provides
    @Singleton
    public ServerRepository provideServerRepository(@ApplicationContext Context context) {
        return new ServerRepositoryImpl(context);
    }

}