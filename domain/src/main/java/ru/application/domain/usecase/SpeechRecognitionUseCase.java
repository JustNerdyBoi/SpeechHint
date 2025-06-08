package ru.application.domain.usecase;

import ru.application.domain.repository.SpeechRecognitionRepository;

public class SpeechRecognitionUseCase {
    private final SpeechRecognitionRepository repository;

    public SpeechRecognitionUseCase(SpeechRecognitionRepository repository) {
        this.repository = repository;
    }

    public void execute(SpeechRecognitionRepository.Listener listener) {
        repository.startRecognition(listener);
    }

    public void stop() {
        repository.stopRecognition();
    }
}
