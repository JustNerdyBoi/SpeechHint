package ru.application.speechhint.viewmodel;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.usecase.LoadDocumentUseCase;

@HiltViewModel
public class TeleprompterViewModel extends ViewModel {
    private final MutableLiveData<Document> documentLiveData = new MutableLiveData<>();
    private final LoadDocumentUseCase loadDocumentUseCase;

    @Inject
    public TeleprompterViewModel(LoadDocumentUseCase loadDocumentUseCase) {
        this.loadDocumentUseCase = loadDocumentUseCase;
    }

    public void LoadLocalDocument(Uri uri) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.LOCAL, uri.toString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void LoadGoogleDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.GOOGLE_DRIVE, shortenedUrl));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    public void LoadYandexDocument(String shortenedUrl) {
        new Thread(() -> {
            try {
                getDocumentLiveData().postValue(loadDocumentUseCase.execute(DocumentSource.YANDEX_DRIVE, shortenedUrl));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    public MutableLiveData<Document> getDocumentLiveData() {
        return documentLiveData;
    }
}
