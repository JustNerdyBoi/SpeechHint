package ru.application.speechhint.viewmodel;

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

    private final MutableLiveData<Document> documentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Settings> settingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> scrollLiveData = new MutableLiveData<>();

    @Inject
    public ServerViewModel(StartServerUseCase startServerUseCase,
                           StopServerUseCase stopServerUseCase,
                           GetServerConnectionInfoUseCase getServerConnectionInfoUseCase,
                           SetServerCurrentPositionUseCase setPositionUseCase,
                           SetServerCurrentSettingsUseCase setServerCurrentSettingsUseCase,
                           ServerRepository serverRepository) {
        this.startServerUseCase = startServerUseCase;
        this.stopServerUseCase = stopServerUseCase;
        this.getServerConnectionInfoUseCase = getServerConnectionInfoUseCase;
        this.setServerCurrentSettingsUseCase = setServerCurrentSettingsUseCase;
        this.setServerCurrentPositionUseCase = setPositionUseCase;

        serverRepository.setListener(new ServerRepository.Listener() {
            @Override
            public void onCurrentPositionReceived(int newCurrentPosition) {
                positionLiveData.postValue(newCurrentPosition);
            }
            @Override
            public void onScrollReceived(int scrollY) {
                scrollLiveData.postValue(scrollY);
            }
            @Override
            public void onDocumentReceived(Document documents) {
                documentLiveData.postValue(documents);
            }
            @Override
            public void onSettingsReceived(Settings settings) {
                settingsLiveData.postValue(settings);
            }
        });
    }

    public void startServer() {
        startServerUseCase.execute();
    }
    public void stopServer() {
        stopServerUseCase.execute();
    }
    public LiveData<Document> getDocumentLiveData() { return documentLiveData; }
    public LiveData<Settings> getSettingsLiveData() { return settingsLiveData; }
    public LiveData<Integer> getPositionLiveData() { return positionLiveData; }
    public LiveData<Integer> getScrollLiveData() { return scrollLiveData; }
    public ServerConnectionInfo getServerConnectionInfo() {
        return getServerConnectionInfoUseCase.execute();
    }
    public void setServerCurrentPosition(int pos) {
        setServerCurrentPositionUseCase.execute(pos);
    }

    public void setServerCurrentSettings(Settings settings){
        setServerCurrentSettingsUseCase.execute(settings);
    }
}
