package ru.application.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ru.application.domain.repository.DocumentRepository;
import ru.application.domain.usecase.LoadDocumentUseCase;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {
    
    @Provides
    public LoadDocumentUseCase provideLoadDocumentUseCase(DocumentRepository repository) {
        return new LoadDocumentUseCase(repository);
    }
}