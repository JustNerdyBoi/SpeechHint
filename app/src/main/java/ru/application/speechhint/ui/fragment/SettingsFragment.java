package ru.application.speechhint.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import dagger.hilt.android.AndroidEntryPoint;
import ru.application.domain.entity.HighlightType;
import ru.application.domain.entity.ScrollConfig;
import ru.application.domain.entity.Settings;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.Theme;
import ru.application.domain.entity.UIConfig;
import ru.application.speechhint.R;
import ru.application.speechhint.viewmodel.SettingsViewModel;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // UIConfig
    private Slider textScaleSlider;
    private RadioGroup themeRadioGroup;
    private MaterialSwitch currentStringHighlightSwitch;
    private RadioGroup highlightRadioGroup;
    private RadioButton highlightLine;
    private RadioButton highlightPointer;
    private RadioButton highlightLightZone;
    private TextView highlightHeightLabel;
    private Slider highlightHeightSlider;
    private MaterialSwitch highlightFollowSwitch;
    private MaterialSwitch mirrorTextSwitch;

    // ScrollConfig
    private MaterialSwitch autoScrollSwitch;
    private TextView autoscrollSpeedLabel;
    private Slider autoscrollSpeedSlider;

    // SttConfig
    private TextView sttConfigLabel;
    private MaterialSwitch sttEnabledSwitch;
    private Slider beforeBufferSizeSlider;
    private Slider afterBufferSizeSlider;

    private boolean isInternalUpdate = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        // UIConfig
        textScaleSlider = root.findViewById(R.id.textScaleSlider);
        themeRadioGroup = root.findViewById(R.id.themeRadioGroup);
        currentStringHighlightSwitch = root.findViewById(R.id.currentStringHighlightSwitch);
        highlightRadioGroup = root.findViewById(R.id.highlightRadioGroup);
        highlightLine = root.findViewById(R.id.highlightLine);
        highlightPointer = root.findViewById(R.id.highlightPointer);
        highlightLightZone = root.findViewById(R.id.highlightLightZone);
        highlightHeightLabel = root.findViewById(R.id.highlightHeightLabel);
        highlightHeightSlider = root.findViewById(R.id.highlightHeightSlider);
        highlightFollowSwitch = root.findViewById(R.id.highlightFollowSwitch);
        mirrorTextSwitch = root.findViewById(R.id.mirrorTextSwitch);

        // ScrollConfig
        autoScrollSwitch = root.findViewById(R.id.autoScrollSwitch);
        autoscrollSpeedLabel = root.findViewById(R.id.autoscrollSpeedLabel);
        autoscrollSpeedSlider = root.findViewById(R.id.autoscrollSpeedSlider);

        // SttConfig
        sttConfigLabel = root.findViewById(R.id.sttConfigLabel);
        sttEnabledSwitch = root.findViewById(R.id.sttEnabledSwitch);
        beforeBufferSizeSlider = root.findViewById(R.id.beforeBufferSizeSlider);
        afterBufferSizeSlider = root.findViewById(R.id.afterBufferSizeSlider);

        Button setDocumentButton = root.findViewById(R.id.setDocumentButton);
        setDocumentButton.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, new FileSelectFragment()).commit();
        });

        applySettings(viewModel.getSettingsLiveData().getValue());
        viewModel.getSettingsLiveData().observe(getViewLifecycleOwner(), this::applySettings);

        setupListeners();

        return root;
    }

    private void applySettings(Settings settings) {
        isInternalUpdate = true;

        // UIConfig
        UIConfig ui = settings.getUiConfig();
        textScaleSlider.setValue(ui.getTextScale());
        currentStringHighlightSwitch.setChecked(ui.isCurrentStringHighlight());
        highlightHeightSlider.setValue((int) (ui.getHighlightHeight() * 100));
        highlightFollowSwitch.setChecked(ui.isCurrentWordHighlightFollow());
        mirrorTextSwitch.setChecked(ui.isMirrorText());
        switch (ui.getTheme()) {
            case DARK:
                themeRadioGroup.check(R.id.darkThemeRadio);
                break;
            case LIGHT:
                themeRadioGroup.check(R.id.lightThemeRadio);
                break;
        }
        switch (ui.getHighlightType()) {
            case LINE:
                highlightRadioGroup.check(R.id.highlightLine);
                break;
            case POINTER:
                highlightRadioGroup.check(R.id.highlightPointer);
                break;
            case LIGHT_ZONE:
                highlightRadioGroup.check(R.id.highlightLightZone);
        }

        // ScrollConfig
        ScrollConfig scroll = settings.getScrollConfig();
        autoScrollSwitch.setChecked(scroll.isEnableAutoScroll());
        autoscrollSpeedSlider.setValue((int) scroll.getSpeed());

        // STT Config
        SttConfig stt = settings.getSttConfig();
        sttEnabledSwitch.setChecked(stt.isSttEnabled());
        beforeBufferSizeSlider.setValue(stt.getSttBeforeBufferSize());
        afterBufferSizeSlider.setValue(stt.getSttAfterBufferSize());

        setSttConfigAvailability(scroll.isEnableAutoScroll(), stt.isSttEnabled());
        setUseSttSwitchDependenciesAvailability(stt.isSttEnabled(), scroll.isEnableAutoScroll());
        setHighlightControlsAvailability(ui.isCurrentStringHighlight());

        isInternalUpdate = false;
    }

    private void setupListeners() {
        // UIConfig
        textScaleSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateUiConfig();
        });
        highlightHeightSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateUiConfig();
        });
        currentStringHighlightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        highlightFollowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        mirrorTextSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        highlightRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isInternalUpdate) updateUiConfig();
        });

        // ScrollConfig
        autoScrollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateScrollConfig();
        });
        autoscrollSpeedSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateScrollConfig();
        });

        // SttConfig
        sttEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            if (!isInternalUpdate) updateSttConfig();
        });
        beforeBufferSizeSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateSttConfig();
        });
        afterBufferSizeSlider.addOnChangeListener((s, value, fromUser) -> {
            if (!isInternalUpdate && fromUser) updateSttConfig();
        });
    }

    private void updateUiConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        UIConfig ui = settings.getUiConfig();
        ui.setTextScale((int) textScaleSlider.getValue());
        ui.setCurrentStringHighlight(currentStringHighlightSwitch.isChecked());
        ui.setCurrentWordHighlightFollow(highlightFollowSwitch.isChecked());
        ui.setHighlightHeight(highlightHeightSlider.getValue() / 100f);
        ui.setMirrorText(mirrorTextSwitch.isChecked());
        ui.setTheme(themeRadioGroup.getCheckedRadioButtonId() == R.id.darkThemeRadio ? Theme.DARK : Theme.LIGHT);
        if (highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightLine) {
            ui.setHighlightType(HighlightType.LINE);
        } else if (highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightPointer) {
            ui.setHighlightType(HighlightType.POINTER);
        } else if (highlightRadioGroup.getCheckedRadioButtonId() == R.id.highlightLightZone) {
            ui.setHighlightType(HighlightType.LIGHT_ZONE);
        }
        viewModel.saveSettings(settings);
    }

    private void updateScrollConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        ScrollConfig scroll = settings.getScrollConfig();
        boolean autoScroll = autoScrollSwitch.isChecked();
        scroll.setEnableAutoScroll(autoScroll);
        scroll.setSpeed(autoscrollSpeedSlider.getValue());
        viewModel.saveSettings(settings);
    }

    private void updateSttConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        SttConfig stt = settings.getSttConfig();
        boolean sttEnabled = sttEnabledSwitch.isChecked();
        stt.setSttEnabled(sttEnabled);
        stt.setSttBeforeBufferSize((int) beforeBufferSizeSlider.getValue());
        stt.setSttAfterBufferSize((int) afterBufferSizeSlider.getValue());
        viewModel.saveSettings(settings);
    }

    private void setBufferSizeControlsEnabled(boolean enabled) {
        beforeBufferSizeSlider.setEnabled(enabled);
        afterBufferSizeSlider.setEnabled(enabled);
    }

    private void setSttConfigAvailability(boolean autoScroll, boolean sttEnabled) {
        sttConfigLabel.setEnabled(autoScroll);
        sttEnabledSwitch.setEnabled(autoScroll);

        setBufferSizeControlsEnabled(autoScroll && sttEnabled);
    }

    private void setUseSttSwitchDependenciesAvailability(boolean sttEnabled, boolean autoScrollChecked) {
        boolean autoScrollControlsEnabled = !sttEnabled && autoScrollChecked;
        autoscrollSpeedLabel.setEnabled(autoScrollControlsEnabled);
        autoscrollSpeedSlider.setEnabled(autoScrollControlsEnabled);

        setBufferSizeControlsEnabled(sttEnabled && autoScrollChecked);
    }

    private void setHighlightControlsAvailability(boolean highlightEnabled) {
        highlightLine.setEnabled(highlightEnabled);
        highlightPointer.setEnabled(highlightEnabled);
        highlightLightZone.setEnabled(highlightEnabled);
        boolean highlightHeightAvailability = highlightEnabled && !highlightFollowSwitch.isChecked();
        highlightHeightLabel.setEnabled(highlightHeightAvailability);
        highlightHeightSlider.setEnabled(highlightHeightAvailability);
        highlightFollowSwitch.setEnabled(highlightEnabled);
    }
}