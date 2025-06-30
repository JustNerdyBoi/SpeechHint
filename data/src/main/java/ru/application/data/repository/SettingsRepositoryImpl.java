package ru.application.data.repository;

import ru.application.data.datasource.PreferencesDataSource;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.SettingsRepository;

public class SettingsRepositoryImpl implements SettingsRepository {
    private final PreferencesDataSource preferencesDataSource;

    public SettingsRepositoryImpl(PreferencesDataSource preferencesDataSource) {
        this.preferencesDataSource = preferencesDataSource;
    }

    @Override
    public Settings getSettings() {
        return preferencesDataSource.getSettings();
    }

    @Override
    public void setSettings(Settings settings) {
        preferencesDataSource.saveSettings(settings);
    }
}
