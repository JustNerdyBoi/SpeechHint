package ru.application.domain.usecase;

import ru.application.domain.repository.ServerRepository;

public class StartServerUseCase {
    private final ServerRepository repository;

    public StartServerUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.startServer();
    }
}