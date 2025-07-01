package ru.application.speechhint.viewmodel;

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
}
