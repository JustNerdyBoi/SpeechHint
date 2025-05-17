package com.example.speechhint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class TextViewerFragment extends Fragment implements WordAdapter.OnWordClickListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private LinkedText linkedText;
    private SpeechRecognitionManager speechManager;
    private LinkedText.TextManager textManager;
    private int currentPosition = 0;
    private int bufferBeforeSize = 5;
    private int bufferAfterSize = 10;

    public static TextViewerFragment newInstance(LinkedText linkedText) {
        TextViewerFragment fragment = new TextViewerFragment();
        fragment.linkedText = linkedText;
        fragment.textManager = linkedText.new TextManager(fragment.bufferBeforeSize, fragment.bufferAfterSize);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();
        setupSpeechRecognition();
    }

    private void setupRecyclerView() {
        adapter = new WordAdapter(linkedText, this, bufferBeforeSize, bufferAfterSize);
        recyclerView.setLayoutManager(new SmoothScrollingLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpeechRecognition() {
        speechManager = new SpeechRecognitionManager(requireContext());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startSpeechRecognition() {
        speechManager.setWordDetectedListener(word -> {
            if (word != null && !word.isEmpty()) {
                int steps = textManager.searchAndMoveToWord(word, LinkedText.Filters.containsCaseInsensitive(word), false);
                if (steps != 0) {
                    scrollBySteps(steps);
                }
            }
        });
        speechManager.initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            Toast.makeText(requireContext(), "Microphone permission required", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWordClick(String word, int targetPosition) {
        textManager.moveSteps(targetPosition - currentPosition);
        recyclerView.smoothScrollToPosition(targetPosition);
        currentPosition = targetPosition;
        adapter.setCurrentPosition(currentPosition);
    }

    public void scrollBySteps(int steps) {
        if (adapter != null && steps != 0) {
            int targetPosition = currentPosition + steps;
            // Ensure target position is within bounds
            targetPosition = Math.max(0, Math.min(targetPosition, linkedText.size() - 1));

            recyclerView.smoothScrollToPosition(targetPosition);
            currentPosition = targetPosition;
            adapter.setCurrentPosition(currentPosition);
        }
    }

    public void setBufferAfterSize(int bufferAfterSize) {
        textManager.setBufferSizes(bufferBeforeSize, bufferAfterSize);
        this.bufferAfterSize = bufferAfterSize;
        this.adapter.setBufferAfterSize(bufferAfterSize);

    }

    public void setBufferBeforeSize(int bufferBeforeSize) {
        textManager.setBufferSizes(bufferBeforeSize, bufferBeforeSize);
        this.bufferBeforeSize = bufferBeforeSize;
        this.adapter.setBufferBeforeSize(bufferBeforeSize);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechManager != null) {
            speechManager.destroy();
        }
    }
} 