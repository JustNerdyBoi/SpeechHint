package ru.application.speechhint.viewmodel;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.DocumentSource;
import ru.application.domain.entity.Word;
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

    public void removeWord(int pos){
        Document document = documentLiveData.getValue();
        if (document != null) {
            document.removeWord(pos);
            documentLiveData.setValue(document);
        }
    }

    public MutableLiveData<Document> getDocumentLiveData() {
        return documentLiveData;
    }
}
