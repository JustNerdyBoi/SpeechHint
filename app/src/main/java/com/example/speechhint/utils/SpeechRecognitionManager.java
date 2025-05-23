package com.example.speechhint.utils;

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

public class SpeechRecognitionManager {
    private static final int RESTART_DELAY_MS = 1000;

    private final Context context;
    private final Handler mainHandler;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private OnWordDetectedListener wordDetectedListener;

    public interface OnWordDetectedListener {
        void onWordDetected(String word);
    }

    public SpeechRecognitionManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setWordDetectedListener(OnWordDetectedListener listener) {
        this.wordDetectedListener = listener;
    }

    public void initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            setupRecognitionListener();
            startListening();
        }
    }

    private void setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Optional: Can be used to show audio level
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Optional: Raw audio buffer
            }

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                scheduleRestart();
            }

            @Override
            public void onError(int error) {
                isListening = false;
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Audio recording error";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Client side error";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Network error";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Network timeout";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "No match found";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "RecognitionService busy";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "Server error";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "No speech input";
                        break;
                    default:
                        errorMessage = "Unknown error";
                        break;
                }
                Log.e("SpeechManager", "Speech recognition error: " + errorMessage);
                scheduleRestart();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty() && wordDetectedListener != null) {
                    String lastWord = extractLastWord(matches.get(0));
                    if (!lastWord.isEmpty()) {
                        wordDetectedListener.onWordDetected(lastWord);
                    }
                }
                scheduleRestart();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty() && wordDetectedListener != null) {
                    String lastWord = extractLastWord(matches.get(0));
                    if (!lastWord.isEmpty()) {
                        wordDetectedListener.onWordDetected(lastWord);
                    }
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void scheduleRestart() {
        mainHandler.postDelayed(this::startListening, RESTART_DELAY_MS);
    }

    private void startListening() {
        if (!isListening) {
            isListening = true;
            Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            try {
                speechRecognizer.startListening(speechIntent);
            } catch (Exception e) {
                isListening = false;
                scheduleRestart();
            }
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    private String extractLastWord(String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return "";
        }
        String[] words = phrase.trim().split("\\s+");
        return words[words.length - 1];
    }
}