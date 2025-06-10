package ru.application.speechhint.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Word;
import ru.application.domain.usecase.CalculatePositionUseCase;
import ru.application.domain.usecase.LoadDocumentUseCase;

@HiltViewModel
public class TeleprompterViewModel extends ViewModel {
    private final MutableLiveData<Document> documentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPositionLiveData = new MutableLiveData<>(0);
    private final LoadDocumentUseCase loadDocumentUseCase;
    private final CalculatePositionUseCase calculatePositionUseCase;

    @Inject
    public TeleprompterViewModel(LoadDocumentUseCase loadDocumentUseCase, CalculatePositionUseCase calculatePositionUseCase) {
        this.loadDocumentUseCase = loadDocumentUseCase;
        this.calculatePositionUseCase = calculatePositionUseCase;
    }

    public void LoadLocalDocument(Uri uri) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.LOCAL, uri.toString()));
            } catch (Exception e) {
                // TODO show error message
            }
        }).start();
    }

    public void LoadGoogleDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.GOOGLE_DRIVE, shortenedUrl));
            } catch (Exception e) {
            }

        }).start();
    }

    public void LoadYandexDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.YANDEX_DRIVE, shortenedUrl));
            } catch (Exception e) {
            }

        }).start();
    }

    public void editWord(int pos, String text) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            document.editWord(pos, text);
            documentLiveData.setValue(document);
        }
    }

    public void addWord(int pos, String text) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            document.addWord(pos, new Word(text));
            documentLiveData.setValue(document);
        }
    }

    public void removeWord(int pos) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            document.removeWord(pos);
            documentLiveData.setValue(document);
        }
    }

    public void onWordRecognized(String recognizedWord, Document document, SttConfig sttConfig) {
        Integer currentPosition = currentPositionLiveData.getValue();
        if (currentPosition == null) currentPosition = 0;
        Integer newPosition = calculatePositionUseCase.execute(recognizedWord, document, currentPosition, sttConfig);
        Log.i("SR", recognizedWord + " " + newPosition);
        if (!newPosition.equals(currentPosition)) {
            currentPositionLiveData.setValue(newPosition);
        }
    }

    public MutableLiveData<Document> getDocumentLiveData() {
        return documentLiveData;
    }

    public MutableLiveData<Integer> getCurrentPositionLiveData() {
        return currentPositionLiveData;
    }

    public void setCurrentPosition(int position) {
        currentPositionLiveData.setValue(position);
    }
}
