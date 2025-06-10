package ru.application.domain.usecase;

import ru.application.domain.entity.Settings;
import ru.application.domain.repository.SettingsRepository;

public class GetSettingsUseCase {
    private final SettingsRepository settingsRepository;

    public GetSettingsUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings execute() {
        return settingsRepository.getSettings();
    }
}
