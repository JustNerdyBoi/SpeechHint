package ru.application.domain.usecase;

import ru.application.domain.entity.Settings;
import ru.application.domain.repository.SettingsRepository;

public class SaveSettingsUseCase {
    private final SettingsRepository settingsRepository;

    public SaveSettingsUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void execute(Settings settings) {
        settingsRepository.saveSettings(settings);
    }
}
