package ru.application.domain.repository;

public interface SpeechRecognitionRepository {
    void startRecognition(Listener listener);
    void stopRecognition();
    void setLanguage(String language);

    interface Listener {
        void onWordRecognized(String word);
        void onError(Throwable throwable);
    }
}
