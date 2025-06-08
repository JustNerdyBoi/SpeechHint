package ru.application.speechhint.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

import ru.application.data.datasource.LocalDataSource;
import ru.application.data.datasource.PreferencesDataSource;
import ru.application.data.datasource.ServerDataSource;
import ru.application.data.datasource.GoogleDriveDataSource;
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
    public ServerDataSource provideServerDataSource(@ApplicationContext Context context) {
        return new ServerDataSource(context);
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
    public PreferencesDataSource providePreferenceDataSource(@ApplicationContext Context context){
        return new PreferencesDataSource(context);
    }
}
