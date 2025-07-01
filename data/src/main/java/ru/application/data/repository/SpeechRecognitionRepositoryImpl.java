package ru.application.data.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.application.domain.interactor.SpeechRecognitionInteractor;
import ru.application.domain.repository.SpeechRecognitionRepository;

public final class SpeechRecognitionRepositoryImpl implements SpeechRecognitionRepository {

    public static final int DELAY_MILLIS = 300;
    private final Context context;
    private final Set<String> lastWordsSet = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer speechRecognizer;
    private Listener listener;
    private boolean isListening = false;
    private String language;

    public SpeechRecognitionRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        createSpeechRecognizer();
    }

    private void createSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new ContinuousRecognitionListener());
        }
    }

    @Override
    public void startRecognition(Listener listener) {
        this.listener = listener;
        isListening = true;
        lastWordsSet.clear();
        createSpeechRecognizer();
        startListening();
    }

    private void startListening() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (language != null) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
        }

        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        try {
            speechRecognizer.startListening(intent);
        } catch (Throwable t) {
            if (listener != null) listener.onError(t);
        }
    }

    @Override
    public void stopRecognition() {
        isListening = false;
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void setLanguage(String language) {
        Log.i("SpeechRecognitionRepository", "Setting language: " + language);
        this.language = language;
    }

    private class ContinuousRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            if (isListening) {
                handler.postDelayed(SpeechRecognitionRepositoryImpl.this::startListening, DELAY_MILLIS);
            }
        }

        @Override
        public void onError(int error) {
            if (listener != null) {
                listener.onError(new Exception("Speech error: " + error));
            }

            if (isListening) {
                handler.postDelayed(SpeechRecognitionRepositoryImpl.this::startListening, DELAY_MILLIS);
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = SpeechRecognitionInteractor.findUnduplicatedWord(matches, lastWordsSet);

            if (word != null && listener != null) {
                listener.onWordRecognized(word);
            }

            if (isListening) {
                handler.postDelayed(SpeechRecognitionRepositoryImpl.this::startListening, DELAY_MILLIS);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = SpeechRecognitionInteractor.findUnduplicatedWord(matches, lastWordsSet);

            if (word != null && listener != null) {
                listener.onWordRecognized(word);
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
