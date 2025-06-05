package ru.application.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ru.application.data.datasource.GoogleDriveDataSource;
import ru.application.data.datasource.LocalDataSource;
import ru.application.data.datasource.ServerDataSource;
import ru.application.data.datasource.YandexDriveDataSource;
import ru.application.data.repository.DocumentRepositoryImpl;
import ru.application.domain.repository.DocumentRepository;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    
    @Provides
    public DocumentRepository provideDocumentRepository(
            LocalDataSource localDataSource,
            GoogleDriveDataSource googleDriveDataSource,
            YandexDriveDataSource yandexDriveDataSource,
            ServerDataSource serverDataSource
    ) {
        return new DocumentRepositoryImpl(
                localDataSource,
                googleDriveDataSource,
                yandexDriveDataSource,
                serverDataSource
        );
    }
}