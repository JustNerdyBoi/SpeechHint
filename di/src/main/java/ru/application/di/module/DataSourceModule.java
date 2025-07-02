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

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {

    @Provides
    @Singleton
    public LocalDataSource provideLocalDataSource(@ApplicationContext Context context) {
        return new LocalDataSource(context);
    }

    @Provides
    @Singleton
    public GoogleDriveDataSource provideGoogleDriveDataSource() {
        return new GoogleDriveDataSource();
    }
    @Provides
    @Singleton
    public YandexDriveDataSource provideYandexDriveDataSource() {
        return new YandexDriveDataSource();
    }

    @Provides
    @Singleton
    public FileDataSource providePreferenceDataSource(@ApplicationContext Context context){
        return new FileDataSource(context);
    }
}
