package ru.application.domain.usecase;

import ru.application.domain.entity.ServerConnectionInfo;
import ru.application.domain.repository.ServerRepository;

public class GetServerConnectionInfoUseCase {
    private final ServerRepository repository;

    public GetServerConnectionInfoUseCase(ServerRepository repository) {
        this.repository = repository;
    }

    public ServerConnectionInfo execute() {
        return repository.getServerConnectionInfo();
    }
}