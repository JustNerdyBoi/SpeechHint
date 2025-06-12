package ru.application.domain.usecase;

import ru.application.domain.entity.Settings;
import ru.application.domain.repository.ServerRepository;

public class SetServerCurrentSettingsUseCase {
    private final ServerRepository repository;

    public SetServerCurrentSettingsUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public void execute(Settings settings) {
        repository.setServerCurrentSettings(settings);
    }
}