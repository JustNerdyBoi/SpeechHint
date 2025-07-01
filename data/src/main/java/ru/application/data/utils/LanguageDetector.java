package ru.application.data.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.application.domain.entity.Word;

/**
 * Utility class for detecting the language of a given text.
 * This class uses the ML Kit Language Identification API to determine the language.
 */
public class LanguageDetector {
    private static final int TIMEOUT = 5;
    private static final int MAX_SAMPLE_SIZE = 100;

    public static String detectLanguage(String text) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        Task<String> task = languageIdentifier.identifyLanguage(text);

        try {
            String languageCode = Tasks.await(task, TIMEOUT, TimeUnit.SECONDS);


            if ("und".equals(languageCode)) {
                Log.w("LanguageDetector", "Language not detected");
                return null;
            }
            Log.i("LanguageDetector", "Detected language: " + languageCode);
            return buildFullCode(languageCode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateSample(List<Word> wordList) {
        if (wordList == null || wordList.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        int limit = Math.min(wordList.size(), MAX_SAMPLE_SIZE);

        for (int i = 0; i < limit; i++) {
            stringBuilder.append(wordList.get(i).getText());
            if (i < limit - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    private static String buildFullCode(String langCode) {  // TODO: find way to convert ISO 639 like "en" to full BCP 47 like "en-US"
        if (langCode.equals("en")) {
            return "en-US";
        }
        else return langCode + "-" + langCode.toUpperCase();
    }
}
