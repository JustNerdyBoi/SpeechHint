package ru.application.speechhint.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import ru.application.speechhint.R;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.domain.entity.*;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // UIConfig
    private SeekBar textScaleSeekBar;
    private TextView textScaleValue;
    private RadioGroup themeRadioGroup;
    private Switch currentStringHighlightSwitch;
    private Switch mirrorTextSwitch;

    // ScrollConfig
    private Switch autoScrollSwitch;
    private TextView autoscrollSpeedLabel;
    private SeekBar autoscrollSpeedSeekBar;
    private TextView autoscrollSpeedValue;

    // SttConfig
    private TextView sttConfigLabel;
    private Switch sttEnabledSwitch;
    private SeekBar beforeBufferSizeSeekBar;
    private TextView beforeBufferSizeValue;
    private SeekBar afterBufferSizeSeekBar;
    private TextView afterBufferSizeValue;

    private boolean isInternalUpdate = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        // UIConfig
        textScaleSeekBar = root.findViewById(R.id.textScaleSeekBar);
        textScaleValue = root.findViewById(R.id.textScaleValue);
        themeRadioGroup = root.findViewById(R.id.themeRadioGroup);
        currentStringHighlightSwitch = root.findViewById(R.id.currentStringHighlightSwitch);
        mirrorTextSwitch = root.findViewById(R.id.mirrorTextSwitch);

        // ScrollConfig
        autoScrollSwitch = root.findViewById(R.id.autoScrollSwitch);
        autoscrollSpeedLabel = root.findViewById(R.id.autoscrollSpeedLabel);
        autoscrollSpeedSeekBar = root.findViewById(R.id.autoscrollSpeedSeekBar);
        autoscrollSpeedValue = root.findViewById(R.id.autoscrollSpeedValue);

        // SttConfig
        sttConfigLabel = root.findViewById(R.id.sttConfigLabel);
        sttEnabledSwitch = root.findViewById(R.id.sttEnabledSwitch);
        beforeBufferSizeSeekBar = root.findViewById(R.id.beforeBufferSizeSeekBar);
        beforeBufferSizeValue = root.findViewById(R.id.beforeBufferSizeValue);
        afterBufferSizeSeekBar = root.findViewById(R.id.afterBufferSizeSeekBar);
        afterBufferSizeValue = root.findViewById(R.id.afterBufferSizeValue);

        applySettings(viewModel.getSettingsLiveData().getValue());
        viewModel.getSettingsLiveData().observe(getViewLifecycleOwner(), this::applySettings);

        setupListeners();

        return root;
    }

    private void applySettings(Settings settings) {
        isInternalUpdate = true;

        // UIConfig
        UIConfig ui = settings.getUiConfig();
        textScaleSeekBar.setProgress(ui.getTextScale());
        textScaleValue.setText(String.valueOf(ui.getTextScale()));
        currentStringHighlightSwitch.setChecked(ui.isCurrentStringHighlight());
        mirrorTextSwitch.setChecked(ui.isMirrorText());
        if ("DARK".equals(ui.getTheme())) {
            themeRadioGroup.check(R.id.darkThemeRadio);
        } else {
            themeRadioGroup.check(R.id.lightThemeRadio);
        }

        // ScrollConfig
        ScrollConfig scroll = settings.getScrollConfig();
        autoScrollSwitch.setChecked(scroll.isAutoScroll());
        autoscrollSpeedSeekBar.setProgress((int) scroll.getSpeed());
        autoscrollSpeedValue.setText(String.valueOf((int) scroll.getSpeed()));

        // STT Config
        SttConfig stt = settings.getSttConfig();
        sttEnabledSwitch.setChecked(stt.isSttEnabled());
        beforeBufferSizeSeekBar.setProgress(stt.getSttBeforeBufferSize());
        beforeBufferSizeValue.setText(String.valueOf(stt.getSttBeforeBufferSize()));
        afterBufferSizeSeekBar.setProgress(stt.getSttAfterBufferSize());
        afterBufferSizeValue.setText(String.valueOf(stt.getSttAfterBufferSize()));

        setSttConfigAvailability(scroll.isAutoScroll(), stt.isSttEnabled());
        setUseSttSwitchDependenciesAvailability(stt.isSttEnabled(), scroll.isAutoScroll());

        isInternalUpdate = false;
    }

    private void setupListeners() {
        // UIConfig
        textScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaleValue.setText(String.valueOf(progress));
                if (!isInternalUpdate && fromUser) updateUiConfig();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        currentStringHighlightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        mirrorTextSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateUiConfig();
        });
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isInternalUpdate) updateUiConfig();
        });

        // ScrollConfig
        autoScrollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInternalUpdate) updateScrollConfig();
        });
        autoscrollSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                autoscrollSpeedValue.setText(String.valueOf(progress));
                if (!isInternalUpdate && fromUser) updateScrollConfig();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // SttConfig
        sttEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            if (!isInternalUpdate) updateSttConfig();
        });
        beforeBufferSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                beforeBufferSizeValue.setText(String.valueOf(progress));
                if (!isInternalUpdate && fromUser) updateSttConfig();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        afterBufferSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                afterBufferSizeValue.setText(String.valueOf(progress));
                if (!isInternalUpdate && fromUser) updateSttConfig();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateUiConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        UIConfig ui = settings.getUiConfig();
        ui.setTextScale(textScaleSeekBar.getProgress());
        ui.setCurrentStringHighlight(currentStringHighlightSwitch.isChecked());
        ui.setMirrorText(mirrorTextSwitch.isChecked());
        ui.setTheme(themeRadioGroup.getCheckedRadioButtonId() == R.id.darkThemeRadio ? "DARK" : "LIGHT");
        viewModel.saveSettings(settings);
    }

    private void updateScrollConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        ScrollConfig scroll = settings.getScrollConfig();
        boolean autoScroll = autoScrollSwitch.isChecked();
        scroll.setAutoScroll(autoScroll);
        scroll.setSpeed(autoscrollSpeedSeekBar.getProgress());
        viewModel.saveSettings(settings);
    }

    private void updateSttConfig() {
        Settings settings = viewModel.getSettingsLiveData().getValue();
        if (settings == null) return;
        SttConfig stt = settings.getSttConfig();
        boolean sttEnabled = sttEnabledSwitch.isChecked();
        stt.setSttEnabled(sttEnabled);
        stt.setSttBeforeBufferSize(beforeBufferSizeSeekBar.getProgress());
        stt.setSttAfterBufferSize(afterBufferSizeSeekBar.getProgress());
        viewModel.saveSettings(settings);
    }

    private void setBufferSizeControlsEnabled(boolean enabled) {
        beforeBufferSizeSeekBar.setEnabled(enabled);
        afterBufferSizeSeekBar.setEnabled(enabled);
        beforeBufferSizeValue.setEnabled(enabled);
        afterBufferSizeValue.setEnabled(enabled);
    }

    private void setSttConfigAvailability(boolean autoScroll, boolean sttEnabled) {
        sttConfigLabel.setEnabled(autoScroll);
        sttEnabledSwitch.setEnabled(autoScroll);

        setBufferSizeControlsEnabled(autoScroll && sttEnabled);
    }

    private void setUseSttSwitchDependenciesAvailability(boolean sttEnabled, boolean autoScrollChecked) {
        boolean autoScrollControlsEnabled = !sttEnabled && autoScrollChecked;
        autoscrollSpeedLabel.setEnabled(autoScrollControlsEnabled);
        autoscrollSpeedSeekBar.setEnabled(autoScrollControlsEnabled);
        autoscrollSpeedValue.setEnabled(autoScrollControlsEnabled);

        setBufferSizeControlsEnabled(sttEnabled && autoScrollChecked);
    }
}