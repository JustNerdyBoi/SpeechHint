package ru.application.data.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.application.domain.repository.SpeechRecognitionRepository;

public class SpeechRecognitionRepositoryImpl implements SpeechRecognitionRepository {

    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private Listener listener;
    private boolean isListening = false;
    private Set<String> lastWordsSet = new HashSet<>();
    public SpeechRecognitionRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void startRecognition(Listener listener) {
        this.listener = listener;
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new ContinuousRecognitionListener());
        }
        isListening = true;
        lastWordsSet = new HashSet<>();
        startListening();
    }

    private void startListening() {
        if (!isListening) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.startListening(intent);
    }

    @Override
    public void stopRecognition() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
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
                startListening();
            }
        }

        @Override
        public void onError(int error) {
            if (listener != null) {
                listener.onError(new Exception("Speech error: " + error));
            }
            if (isListening) {
                startListening();
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty() && listener != null) {
                String[] words = matches.get(0).trim().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty() && lastWordsSet.add(word.toLowerCase())) {
                        listener.onWordRecognized(word);
                    }
                }
            }
            if (isListening) {
                startListening();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty() && listener != null) {
                String[] words = matches.get(0).trim().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty() && lastWordsSet.add(word.toLowerCase())) {
                        listener.onWordRecognized(word);
                    }
                }
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
