package ru.application.speechhint.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.entity.Settings;
import ru.application.domain.usecase.GetSettingsUseCase;
import ru.application.domain.usecase.SaveSettingsUseCase;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private final GetSettingsUseCase getSettingsUseCase;
    private final SaveSettingsUseCase saveSettingsUseCase;
    private final MutableLiveData<Settings> settingsLiveData = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(GetSettingsUseCase getSettingsUseCase, SaveSettingsUseCase saveSettingsUseCase) {
        this.getSettingsUseCase = getSettingsUseCase;
        this.saveSettingsUseCase = saveSettingsUseCase;
        loadSettings();
    }

    public LiveData<Settings> getSettingsLiveData() {
        return settingsLiveData;
    }

    public void loadSettings() {
        settingsLiveData.setValue(getSettingsUseCase.execute());
    }

    public void saveSettings(Settings settings) {
        saveSettingsUseCase.execute(settings);
        settingsLiveData.setValue(settings);
    }

    //@debug TODO: debug method, remove after testing
    public void logSettings(){
        Log.i("SETTINGS", "SCROLL CONFIG: " + settingsLiveData.getValue().getScrollConfig().isAutoScroll() + " " + settingsLiveData.getValue().getScrollConfig().getSpeed());
        Log.i("SETTINGS", "STT CONFIG: " + settingsLiveData.getValue().getSttConfig().isSttEnabled() + " " + settingsLiveData.getValue().getSttConfig().getSttBeforeBufferSize() + " " + settingsLiveData.getValue().getSttConfig().getSttAfterBufferSize());
        Log.i("SETTINGS", "UI CONFIG: " + settingsLiveData.getValue().getUiConfig().getTheme() + " " + settingsLiveData.getValue().getUiConfig().getTextScale() + " " + settingsLiveData.getValue().getUiConfig().isCurrentStringHighlight());
    }
}
