package ru.application.domain.repository;

public interface SpeechRecognitionRepository {
    interface Listener {
        void onWordRecognized(String word);
        void onError(Throwable throwable);
    }

    void startRecognition(Listener listener);
    void stopRecognition();
}
