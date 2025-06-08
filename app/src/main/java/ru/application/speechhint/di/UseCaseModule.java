package ru.application.speechhint.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ru.application.domain.repository.DocumentRepository;
import ru.application.domain.repository.SpeechRecognitionRepository;
import ru.application.domain.usecase.LoadDocumentUseCase;
import ru.application.domain.usecase.SpeechRecognitionUseCase;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    public LoadDocumentUseCase provideLoadDocumentUseCase(DocumentRepository repository) {
        return new LoadDocumentUseCase(repository);
    }

    @Provides
    @Singleton
    public SpeechRecognitionUseCase provideSpeechRecognitionUseCase(SpeechRecognitionRepository repository) {
        return new SpeechRecognitionUseCase(repository);
    }
}