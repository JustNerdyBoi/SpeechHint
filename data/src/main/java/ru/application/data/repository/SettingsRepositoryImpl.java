package ru.application.data.repository;

import ru.application.data.datasource.FileDataSource;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.SettingsRepository;

public class SettingsRepositoryImpl implements SettingsRepository {
    private final FileDataSource fileDataSource;

    public SettingsRepositoryImpl(FileDataSource fileDataSource) {
        this.fileDataSource = fileDataSource;
    }

    @Override
    public Settings getSettings() {
        return fileDataSource.getSettings();
    }

    @Override
    public void setSettings(Settings settings) {
        fileDataSource.saveSettings(settings);
    }
}
