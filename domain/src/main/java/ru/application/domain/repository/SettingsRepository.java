package ru.application.domain.repository;

import ru.application.domain.entity.Settings;

public interface SettingsRepository {
    Settings getSettings();
    void setSettings(Settings settings);
}