package ru.application.domain.usecase;

import ru.application.domain.repository.ServerRepository;

public class StopServerUseCase {
    private final ServerRepository repository;

    public StopServerUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.stopServer();
    }
}