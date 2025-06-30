package ru.application.speechhint.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Word;
import ru.application.domain.usecase.AddWordUseCase;
import ru.application.domain.usecase.CalculatePositionUseCase;
import ru.application.domain.usecase.EditWordUseCase;
import ru.application.domain.usecase.LoadDocumentUseCase;
import ru.application.domain.usecase.RemoveWordUseCase;

@HiltViewModel
public class TeleprompterViewModel extends ViewModel {
    private final MutableLiveData<Document> documentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPositionLiveData = new MutableLiveData<>(0);
    private final LoadDocumentUseCase loadDocumentUseCase;
    private final CalculatePositionUseCase calculatePositionUseCase;
    private final AddWordUseCase addWordUseCase;
    private final EditWordUseCase editWordUseCase;
    private final RemoveWordUseCase removeWordUseCase;

    @Inject
    public TeleprompterViewModel(LoadDocumentUseCase loadDocumentUseCase,
                                 CalculatePositionUseCase calculatePositionUseCase,
                                 AddWordUseCase addWordUseCase,
                                 EditWordUseCase editWordUseCase,
                                 RemoveWordUseCase removeWordUseCase) {
        this.loadDocumentUseCase = loadDocumentUseCase;
        this.calculatePositionUseCase = calculatePositionUseCase;
        this.addWordUseCase = addWordUseCase;
        this.editWordUseCase = editWordUseCase;
        this.removeWordUseCase = removeWordUseCase;
    }

    public void LoadLocalDocument(Uri uri) {
        new Thread(() -> {
            try {
                documentLiveData.postValue(loadDocumentUseCase.execute(DocumentSource.LOCAL, uri.toString()));
            } catch (Exception ignored) {
            }
        }).start();
    }

    public void LoadGoogleDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                documentLiveData.postValue(loadDocumentUseCase.execute(DocumentSource.GOOGLE_DRIVE, shortenedUrl));
            } catch (Exception ignored) {
            }

        }).start();
    }

    public void LoadYandexDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                documentLiveData.postValue(loadDocumentUseCase.execute(DocumentSource.YANDEX_DRIVE, shortenedUrl));
            } catch (Exception ignored) {
            }

        }).start();
    }

    public void editWord(int pos, String text) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            documentLiveData.setValue(editWordUseCase.execute(document, pos, text));
        }
    }

    public void addWord(int pos, String text) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            documentLiveData.setValue(addWordUseCase.execute(document, pos, text));
        }
    }

    public void removeWord(int pos) {
        Document document = documentLiveData.getValue();
        if (document != null) {
            documentLiveData.setValue(removeWordUseCase.execute(document, pos));
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

    public LiveData<Document> getDocumentLiveData() {
        return documentLiveData;
    }

    public void setDocument(Document document) {
        documentLiveData.setValue(document);
    }

    public LiveData<Integer> getCurrentPositionLiveData() {
        return currentPositionLiveData;
    }

    public void setCurrentPosition(int position) {
        currentPositionLiveData.setValue(position);
    }
}
