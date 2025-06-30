package ru.application.domain.repository;

public interface SpeechRecognitionRepository {
    void startRecognition(Listener listener);
    void stopRecognition();

    interface Listener {
        void onWordRecognized(String word);
        void onError(Throwable throwable);
    }
}
