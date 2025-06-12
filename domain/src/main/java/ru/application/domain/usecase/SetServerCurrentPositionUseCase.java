package ru.application.domain.usecase;

import ru.application.domain.repository.ServerRepository;

public class SetServerCurrentPositionUseCase {
    private final ServerRepository repository;

    public SetServerCurrentPositionUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public void execute(int position) {
        repository.setServerCurrentPosition(position);
    }
}