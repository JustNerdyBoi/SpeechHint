package ru.application.speechhint.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.ServerConnectionInfo;
import ru.application.domain.entity.Settings;
import ru.application.domain.repository.ServerRepository;
import ru.application.domain.usecase.GetServerConnectionInfoUseCase;
import ru.application.domain.usecase.SetServerCurrentDocumentUseCase;
import ru.application.domain.usecase.SetServerCurrentPositionUseCase;
import ru.application.domain.usecase.SetServerCurrentSettingsUseCase;
import ru.application.domain.usecase.StartServerUseCase;
import ru.application.domain.usecase.StopServerUseCase;

@HiltViewModel
public class ServerViewModel extends ViewModel {
    private final StartServerUseCase startServerUseCase;
    private final StopServerUseCase stopServerUseCase;
    private final GetServerConnectionInfoUseCase getServerConnectionInfoUseCase;
    private final SetServerCurrentPositionUseCase setServerCurrentPositionUseCase;
    private final SetServerCurrentSettingsUseCase setServerCurrentSettingsUseCase;
    private final SetServerCurrentDocumentUseCase setServerCurrentDocumentUseCase;

    private final MutableLiveData<Document> receivedDocumentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Settings> receivedSettingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> receivedPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> receivedScrollLiveData = new MutableLiveData<>();

    @Inject
    public ServerViewModel(StartServerUseCase startServerUseCase,
                           StopServerUseCase stopServerUseCase,
                           GetServerConnectionInfoUseCase getServerConnectionInfoUseCase,
                           SetServerCurrentPositionUseCase setPositionUseCase,
                           SetServerCurrentSettingsUseCase setServerCurrentSettingsUseCase,
                           SetServerCurrentDocumentUseCase setServerCurrentDocumentUseCase,
                           ServerRepository serverRepository) {
        this.startServerUseCase = startServerUseCase;
        this.stopServerUseCase = stopServerUseCase;
        this.getServerConnectionInfoUseCase = getServerConnectionInfoUseCase;
        this.setServerCurrentSettingsUseCase = setServerCurrentSettingsUseCase;
        this.setServerCurrentPositionUseCase = setPositionUseCase;
        this.setServerCurrentDocumentUseCase = setServerCurrentDocumentUseCase;

        serverRepository.setListener(new ServerRepository.Listener() {
            @Override
            public void onCurrentPositionReceived(int newCurrentPosition) {
                receivedPositionLiveData.postValue(newCurrentPosition);
            }

            @Override
            public void onScrollReceived(int scrollY) {
                receivedScrollLiveData.postValue(scrollY);
            }

            @Override
            public void onDocumentReceived(Document documents) {
                receivedDocumentLiveData.postValue(documents);
            }

            @Override
            public void onSettingsReceived(Settings settings) {
                receivedSettingsLiveData.postValue(settings);
            }
        });
    }

    public void startServer() {
        startServerUseCase.execute();
    }

    public void stopServer() {
        stopServerUseCase.execute();
    }

    public LiveData<Document> getReceivedDocumentLiveData() {
        return receivedDocumentLiveData;
    }

    public void clearReceivedDocumentLiveData() {
        receivedDocumentLiveData.setValue(null);
    }

    public LiveData<Settings> getReceivedSettingsLiveData() {
        return receivedSettingsLiveData;
    }

    public void clearReceivedSettingsLiveData() {
        receivedSettingsLiveData.setValue(null);
    }

    public LiveData<Integer> getReceivedPositionLiveData() {
        return receivedPositionLiveData;
    }

    public void clearReceivedPositionLiveData() {
        receivedPositionLiveData.setValue(null);
    }

    public LiveData<Integer> getReceivedScrollLiveData() {
        return receivedScrollLiveData;
    }

    public void clearReceivedScrollLiveData() {
        receivedScrollLiveData.setValue(null);
    }

    public ServerConnectionInfo getServerConnectionInfo() {
        return getServerConnectionInfoUseCase.execute();
    }

    public void setServerCurrentPosition(int pos) {
        if (getServerConnectionInfo() != null) {
            setServerCurrentPositionUseCase.execute(pos);
        }
    }

    public void setServerCurrentSettings(Settings settings) {
        if (getServerConnectionInfo() != null) {
            setServerCurrentSettingsUseCase.execute(settings);
        }
    }

    public void setServerCurrentDocument(Document document) {
        Log.i("SERVER", document == null ? "NULLDOC" : document.toString());
        if (getServerConnectionInfo() != null) {
            setServerCurrentDocumentUseCase.execute(document);
        }
    }
}
