package ru.application.speechhint.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import ru.application.domain.entity.HighlightType;
import ru.application.domain.entity.ScrollConfig;
import ru.application.domain.entity.Settings;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Theme;
import ru.application.domain.entity.UIConfig;
import ru.application.speechhint.R;
import ru.application.speechhint.databinding.SettingsFragmentBinding;
import ru.application.speechhint.viewmodel.SettingsViewModel;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private SettingsFragmentBinding binding;
    private boolean isInternalUpdate = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        applySettings(viewModel.getSettingsLiveData().getValue());
        viewModel.getSettingsLiveData().observe(getViewLifecycleOwner(), this::applySettings);

        setupListeners();

        return binding.getRoot();
    }

    private void applySettings(Settings settings) {
        if (settings == null) return;
        isInternalUpdate = true;

        // UIConfig
        UIConfig ui = settings.getUiConfig();
        binding.uiConfig.textScaleSlider.setValue(ui.getTextScale());
        binding.uiConfig.currentStringHighlightSwitch.setChecked(ui.isCurrentStringHighlight());
        binding.uiConfig.highlightHeightSlider.setValue((int) (ui.getHighlightHeight() * 100));
        binding.uiConfig.highlightFollowSwitch.setChecked(ui.isCurrentWordHighlightFollow());
        binding.uiConfig.mirrorTextSwitch.setChecked(ui.isMirrorText());

        switch (ui.getTheme()) {
            case DARK:
                binding.uiConfig.themeRadioGroup.check(R.id.darkThemeRadio);
                break;
            case LIGHT:
                binding.uiConfig.themeRadioGroup.check(R.id.lightThemeRadio);
                break;
        }

        switch (ui.getHighlightType()) {
            case LINE:
                binding.uiConfig.highlightRadioGroup.check(R.id.highlightLine);
                break;
            case POINTER:
                binding.uiConfig.highlightRadioGroup.check(R.id.highlightPointer);
                break;
            case LIGHT_ZONE:
                binding.uiConfig.highlightRadioGroup.check(R.id.highlightLightZone);
                break;
        }

        // ScrollConfig
        ScrollConfig scroll = settings.getScrollConfig();
        binding.scrollConfig.autoScrollSwitch.setChecked(scroll.isEnableAutoScroll());
        binding.scrollConfig.autoscrollSpeedSlider.setValue(Math.min((int) scroll.getSpeed(), 500));

        // STT Config
        SttConfig stt = settings.getSttConfig();
        binding.sttConfig.sttEnabledSwitch.setChecked(stt.isSttEnabled());
        binding.sttConfig.beforeBufferSizeSlider.setValue(Math.min(stt.getSttBeforeBufferSize(), 20));
        binding.sttConfig.afterBufferSizeSlider.setValue(Math.min(stt.getSttAfterBufferSize(), 20));

        setSttConfigAvailability(scroll.isEnableAutoScroll(), stt.isSttEnabled());
        setUseSttSwitchDependenciesAvailability(stt.isSttEnabled(), scroll.isEnableAutoScroll());
        setHighlightControlsAvailability(ui.isCurrentStringHighlight());

        isInternalUpdate = false;
    }

    private void setupListeners() {
        binding.uiConfig.textScaleSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateUiConfig();
        });
        binding.uiConfig.highlightHeightSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateUiConfig();
        });
        binding.uiConfig.currentStringHighlightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        binding.uiConfig.highlightFollowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        binding.uiConfig.mirrorTextSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        binding.uiConfig.themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        binding.uiConfig.highlightRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isInternalUpdate) updateUiConfig();
        });

        binding.scrollConfig.autoScrollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateScrollConfig();
        });
        binding.scrollConfig.autoscrollSpeedSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateScrollConfig();
        });

        binding.sttConfig.sttEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            if (!isInternalUpdate) updateSttConfig();
        });
        binding.sttConfig.beforeBufferSizeSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateSttConfig();
        });
        binding.sttConfig.afterBufferSizeSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateSttConfig();
        });

        binding.setDocumentButton.setOnClickListener(view ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrameLayout, new FileSelectFragment())
                        .commit()
        );
    }

    private void updateUiConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        UIConfig ui = settings.getUiConfig();
        ui.setTextScale((int) binding.uiConfig.textScaleSlider.getValue());
        ui.setCurrentStringHighlight(binding.uiConfig.currentStringHighlightSwitch.isChecked());
        ui.setCurrentWordHighlightFollow(binding.uiConfig.highlightFollowSwitch.isChecked());
        ui.setHighlightHeight(binding.uiConfig.highlightHeightSlider.getValue() / 100f);
        ui.setMirrorText(binding.uiConfig.mirrorTextSwitch.isChecked());
        ui.setTheme(binding.uiConfig.themeRadioGroup.getCheckedRadioButtonId() == R.id.darkThemeRadio ? Theme.DARK : Theme.LIGHT);

        if (binding.uiConfig.highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightLine) {
            ui.setHighlightType(HighlightType.LINE);
        } else if (binding.uiConfig.highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightPointer) {
            ui.setHighlightType(HighlightType.POINTER);
        } else if (binding.uiConfig.highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightLightZone) {
            ui.setHighlightType(HighlightType.LIGHT_ZONE);
        }

        viewModel.saveSettings(settings);
    }

    private void updateScrollConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        ScrollConfig scroll = settings.getScrollConfig();
        scroll.setEnableAutoScroll(binding.scrollConfig.autoScrollSwitch.isChecked());
        scroll.setSpeed(binding.scrollConfig.autoscrollSpeedSlider.getValue());
        viewModel.saveSettings(settings);
    }

    private void updateSttConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        SttConfig stt = settings.getSttConfig();
        stt.setSttEnabled(binding.sttConfig.sttEnabledSwitch.isChecked());
        stt.setSttBeforeBufferSize((int) binding.sttConfig.beforeBufferSizeSlider.getValue());
        stt.setSttAfterBufferSize((int) binding.sttConfig.afterBufferSizeSlider.getValue());
        viewModel.saveSettings(settings);
    }

    private void setBufferSizeControlsEnabled(boolean enabled) {
        binding.sttConfig.beforeBufferSizeSlider.setEnabled(enabled);
        binding.sttConfig.afterBufferSizeSlider.setEnabled(enabled);
    }

    private void setSttConfigAvailability(boolean autoScroll, boolean sttEnabled) {
        binding.sttConfig.sttConfigLabel.setEnabled(autoScroll);
        binding.sttConfig.sttEnabledSwitch.setEnabled(autoScroll);
        setBufferSizeControlsEnabled(autoScroll && sttEnabled);
    }

    private void setUseSttSwitchDependenciesAvailability(boolean sttEnabled, boolean autoScrollChecked) {
        boolean autoScrollControlsEnabled = !sttEnabled && autoScrollChecked;
        binding.scrollConfig.autoscrollSpeedLabel.setEnabled(autoScrollControlsEnabled);
        binding.scrollConfig.autoscrollSpeedSlider.setEnabled(autoScrollControlsEnabled);
        setBufferSizeControlsEnabled(sttEnabled && autoScrollChecked);
    }

    private void setHighlightControlsAvailability(boolean highlightEnabled) {
        binding.uiConfig.highlightLine.setEnabled(highlightEnabled);
        binding.uiConfig.highlightPointer.setEnabled(highlightEnabled);
        binding.uiConfig.highlightLightZone.setEnabled(highlightEnabled);
        boolean heightAvailable = highlightEnabled && !binding.uiConfig.highlightFollowSwitch.isChecked();
        binding.uiConfig.highlightHeightLabel.setEnabled(heightAvailable);
        binding.uiConfig.highlightHeightSlider.setEnabled(heightAvailable);
        binding.uiConfig.highlightFollowSwitch.setEnabled(highlightEnabled);
    }
}