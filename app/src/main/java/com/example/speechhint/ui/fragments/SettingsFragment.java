package com.example.speechhint.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.speechhint.R;
import com.example.speechhint.config.DefaultConfigs;
import com.example.speechhint.viewmodels.MainViewModel;

public class SettingsFragment extends Fragment {
    private MainViewModel viewModel;
    
    // UI Elements
    private SeekBar textScaleSeekBar;
    private Switch voiceDetectionSwitch;
    private Switch showCurrentWordSwitch;
    private Switch showBufferSwitch;
    private Switch autoscrollSwitch;
    private SeekBar autoscrollSpeedSeekBar;
    private SeekBar beforeBufferSizeSeekBar;
    private SeekBar afterBufferSizeSeekBar;
    private TextView beforeBufferSizeLabel;
    private TextView afterBufferSizeLabel;
    private TextView autoscrollSpeedLabel;
    
    // Value displays
    private TextView textScaleValue;
    private TextView beforeBufferSizeValue;
    private TextView afterBufferSizeValue;
    private TextView autoscrollSpeedValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View settingsView = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        initializeViews(settingsView);
        setupListeners();
        loadSavedSettings();
        return settingsView;
    }

    private void initializeViews(View settingsView) {
        textScaleSeekBar = settingsView.findViewById(R.id.textScaleSeekBar);
        voiceDetectionSwitch = settingsView.findViewById(R.id.voiceDetectionSwitch);
        showCurrentWordSwitch = settingsView.findViewById(R.id.showCurrentWordSwitch);
        showBufferSwitch = settingsView.findViewById(R.id.showBufferSwitch);
        autoscrollSwitch = settingsView.findViewById(R.id.useAutoscrollSwitch);
        autoscrollSpeedSeekBar = settingsView.findViewById(R.id.autoscrollSpeedSeekBar);
        beforeBufferSizeSeekBar = settingsView.findViewById(R.id.beforeBufferSizeSeekBar);
        afterBufferSizeSeekBar = settingsView.findViewById(R.id.afterBufferSizeSeekBar);
        beforeBufferSizeLabel = settingsView.findViewById(R.id.beforeBufferSizeLabel);
        afterBufferSizeLabel = settingsView.findViewById(R.id.afterBufferSizeLabel);
        autoscrollSpeedLabel = settingsView.findViewById(R.id.autoscrollSpeedLabel);
        
        // Initialize value displays
        textScaleValue = settingsView.findViewById(R.id.textScaleValue);
        beforeBufferSizeValue = settingsView.findViewById(R.id.beforeBufferSizeValue);
        afterBufferSizeValue = settingsView.findViewById(R.id.afterBufferSizeValue);
        autoscrollSpeedValue = settingsView.findViewById(R.id.autoscrollSpeedValue);
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        
        int textScale = prefs.getInt(DefaultConfigs.KEY_TEXT_SCALE, DefaultConfigs.DEFAULT_TEXT_SCALE);
        textScaleSeekBar.setProgress(textScale);
        textScaleValue.setText(String.valueOf(textScale));
        
        voiceDetectionSwitch.setChecked(prefs.getBoolean(DefaultConfigs.KEY_USE_VOICE_DETECTION, DefaultConfigs.DEFAULT_USE_VOICE_DETECTION));
        showCurrentWordSwitch.setChecked(prefs.getBoolean(DefaultConfigs.KEY_SHOW_CURRENT_WORD, DefaultConfigs.DEFAULT_SHOW_CURRENT_WORD));
        showBufferSwitch.setChecked(prefs.getBoolean(DefaultConfigs.KEY_SHOW_BUFFER, DefaultConfigs.DEFAULT_SHOW_BUFFER));
        autoscrollSwitch.setChecked(prefs.getBoolean(DefaultConfigs.KEY_USE_AUTOSCROLL, DefaultConfigs.DEFAULT_USE_AUTOSCROLL));
        
        int autoscrollSpeed = prefs.getInt(DefaultConfigs.KEY_AUTOSCROLL_SPEED, DefaultConfigs.DEFAULT_AUTOSCROLL_SPEED);
        autoscrollSpeedSeekBar.setProgress(autoscrollSpeed);
        autoscrollSpeedValue.setText(String.valueOf(autoscrollSpeed));
        
        int beforeBufferSize = prefs.getInt(DefaultConfigs.KEY_BEFORE_BUFFER_SIZE, DefaultConfigs.DEFAULT_BEFORE_BUFFER_SIZE);
        beforeBufferSizeSeekBar.setProgress(beforeBufferSize);
        beforeBufferSizeValue.setText(String.valueOf(beforeBufferSize));
        
        int afterBufferSize = prefs.getInt(DefaultConfigs.KEY_AFTER_BUFFER_SIZE, DefaultConfigs.DEFAULT_AFTER_BUFFER_SIZE);
        afterBufferSizeSeekBar.setProgress(afterBufferSize);
        afterBufferSizeValue.setText(String.valueOf(afterBufferSize));

        updateDependentSwitches(voiceDetectionSwitch.isChecked(), autoscrollSwitch.isChecked());
    }

    private void setupListeners() {
        setupTextScaleListener();
        setupVoiceDetectionListener();
        setupShowCurrentWordListener();
        setupShowBufferListener();
        setupAutoscrollListener();
        setupBufferSizeListeners();
        setupAutoscrollSpeedListener();
    }

    private void setupTextScaleListener() {
        textScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress != viewModel.getTextScale().getValue()) {
                    viewModel.updateTextScale(progress);
                }
                textScaleValue.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupVoiceDetectionListener() {
        voiceDetectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != viewModel.getUseVoiceDetection().getValue()) {
                viewModel.updateUseVoiceDetection(isChecked);
                autoscrollSwitch.setChecked(false);
                updateDependentSwitches(isChecked, autoscrollSwitch.isChecked());
            }
        });
    }

    private void setupShowCurrentWordListener() {
        showCurrentWordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != viewModel.getShowCurrentWord().getValue()) {
                viewModel.updateShowCurrentWord(isChecked);
            }
        });
    }

    private void setupShowBufferListener() {
        showBufferSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != viewModel.getShowBuffer().getValue()) {
                viewModel.updateShowBuffer(isChecked);
            }
        });
    }

    private void setupAutoscrollListener() {
        autoscrollSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != viewModel.getUseAutoscroll().getValue()) {
                viewModel.updateUseAutoscroll(isChecked);
                updateDependentSwitches(voiceDetectionSwitch.isChecked(), isChecked);
            }
        });
    }

    private void setupAutoscrollSpeedListener() {
        autoscrollSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress != viewModel.getAutoscrollSpeed().getValue()) {
                    viewModel.updateAutoscrollSpeed(progress);
                }
                autoscrollSpeedValue.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupBufferSizeListeners() {
        beforeBufferSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress != viewModel.getBeforeBufferSize().getValue()) {
                    viewModel.updateBeforeBufferSize(progress);
                }
                beforeBufferSizeValue.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        afterBufferSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress != viewModel.getAfterBufferSize().getValue()) {
                    viewModel.updateAfterBufferSize(progress);
                }
                afterBufferSizeValue.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateDependentSwitches(boolean voiceEnabled, boolean autoscrollEnabled) {
        showCurrentWordSwitch.setEnabled(voiceEnabled);
        showBufferSwitch.setEnabled(voiceEnabled);
        beforeBufferSizeSeekBar.setEnabled(voiceEnabled);
        afterBufferSizeSeekBar.setEnabled(voiceEnabled);
        beforeBufferSizeLabel.setEnabled(voiceEnabled);
        afterBufferSizeLabel.setEnabled(voiceEnabled);
        beforeBufferSizeValue.setEnabled(voiceEnabled);
        afterBufferSizeValue.setEnabled(voiceEnabled);
        
        autoscrollSwitch.setEnabled(!voiceEnabled);
        autoscrollSpeedLabel.setEnabled(!voiceEnabled && autoscrollEnabled);
        autoscrollSpeedSeekBar.setEnabled(!voiceEnabled && autoscrollEnabled);
        autoscrollSpeedValue.setEnabled(!voiceEnabled && autoscrollEnabled);
    }
} 