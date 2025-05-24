package com.example.speechhint.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speechhint.R;
import com.example.speechhint.config.DefaultConfigs;
import com.example.speechhint.ui.adapters.WordAdapter;
import com.example.speechhint.ui.layouts.FlowLayoutManager;
import com.example.speechhint.ui.layouts.SmoothScrollingLayoutManager;
import com.example.speechhint.utils.LinkedText;
import com.example.speechhint.utils.SpeechRecognitionManager;
import com.example.speechhint.viewmodels.MainViewModel;

import java.util.Objects;

public class TextViewerFragment extends Fragment implements WordAdapter.OnWordClickListener, WordAdapter.OnWordLongClickListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String ARG_LINKED_TEXT = "linked_text";
    private static final String ARG_CURRENT_POSITION = "current_position";
    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private LinkedText linkedText;
    private SpeechRecognitionManager speechManager;
    private LinkedText.TextManager textManager;
    private MainViewModel viewModel;
    private int currentPosition = 0;
    private int bufferBeforeSize;
    private int bufferAfterSize;
    private int scrollSpeed = 100;
    private boolean isAutoScrolling = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrolling && recyclerView != null && recyclerView.getLayoutManager() != null) {
                if (currentPosition < linkedText.size() - 1) {
                    textManager.moveSteps(1);
                    scrollBySteps(1, true);
                    handler.postDelayed(this, calculateScrollDelay());
                } else {
                    stopAutoScroll();
                }
            }
        }
    };

    public static TextViewerFragment newInstance(LinkedText linkedText) {
        TextViewerFragment fragment = new TextViewerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LINKED_TEXT, linkedText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            linkedText = (LinkedText) savedInstanceState.getSerializable(ARG_LINKED_TEXT);
            currentPosition = savedInstanceState.getInt(ARG_CURRENT_POSITION, 0);
        } else if (getArguments() != null) {
            linkedText = (LinkedText) getArguments().getSerializable(ARG_LINKED_TEXT);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_LINKED_TEXT, linkedText);
        outState.putInt(ARG_CURRENT_POSITION, currentPosition);
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
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Initialize buffer sizes from ViewModel
        bufferBeforeSize = (viewModel.getBeforeBufferSize().getValue() != null) ?
                viewModel.getBeforeBufferSize().getValue() : DefaultConfigs.DEFAULT_BEFORE_BUFFER_SIZE;
        bufferAfterSize = (viewModel.getAfterBufferSize().getValue() != null) ?
                viewModel.getAfterBufferSize().getValue() : DefaultConfigs.DEFAULT_AFTER_BUFFER_SIZE;

        setupRecyclerView();
        observeViewModel();

        textManager = linkedText.new TextManager(bufferBeforeSize, bufferAfterSize);

        if (currentPosition != 0) {
            textManager.moveSteps(currentPosition);
            adapter.setCurrentPosition(currentPosition);
            ((SmoothScrollingLayoutManager) recyclerView.getLayoutManager()).timedScrollToPosition(recyclerView, currentPosition, 100);
        }

        if (viewModel.getUseVoiceDetection().getValue()) {
            setupSpeechRecognition();
        } else {
            stopSpeechRecognition();
        }
    }

    private void observeViewModel() {
        // Observe text scale changes
        viewModel.getTextScale().observe(getViewLifecycleOwner(), scale -> {
            adapter.notifyDataSetChanged();
        });

        // Observe voice detection changes
        viewModel.getUseVoiceDetection().observe(getViewLifecycleOwner(), useVoiceDetection -> {
            if (useVoiceDetection) {
                setupSpeechRecognition();
            } else {
                stopSpeechRecognition();
            }

            adapter.setShowCurrentWord(useVoiceDetection && viewModel.getShowCurrentWord().getValue());
            adapter.setShowBuffer(useVoiceDetection && viewModel.getShowBuffer().getValue());
            adapter.notifyItemRangeChanged(Math.max(0, currentPosition - bufferBeforeSize), Math.min(bufferAfterSize + 1 + bufferAfterSize, linkedText.size() - currentPosition + bufferBeforeSize));
        });

        // Observe show current word changes
        viewModel.getShowCurrentWord().observe(getViewLifecycleOwner(), showCurrentWord -> {
            adapter.setShowCurrentWord(showCurrentWord && viewModel.getUseVoiceDetection().getValue());
            adapter.notifyItemChanged(currentPosition);
        });

        // Observe show buffer changes
        viewModel.getShowBuffer().observe(getViewLifecycleOwner(), showBuffer -> {
            adapter.setShowBuffer(showBuffer && viewModel.getUseVoiceDetection().getValue());
            adapter.notifyItemRangeChanged(Math.max(0, currentPosition - bufferBeforeSize), Math.min(bufferAfterSize + 1 + bufferAfterSize, linkedText.size() - currentPosition + bufferBeforeSize));
        });

        // Observe buffer size changes
        viewModel.getBeforeBufferSize().observe(getViewLifecycleOwner(), newBufferBeforeSize -> {
            textManager.setBufferSizes(newBufferBeforeSize, bufferAfterSize);
            this.adapter.setBufferBeforeSize(newBufferBeforeSize);
            if (bufferBeforeSize >= newBufferBeforeSize) {
                this.adapter.notifyItemRangeChanged(Math.max(0, currentPosition - bufferBeforeSize), bufferBeforeSize);
                bufferBeforeSize = newBufferBeforeSize;
            } else {
                bufferBeforeSize = newBufferBeforeSize;
                this.adapter.notifyItemRangeChanged(Math.max(0, currentPosition - bufferBeforeSize), bufferBeforeSize);
            }
        });

        viewModel.getAfterBufferSize().observe(getViewLifecycleOwner(), newBufferAfterSize -> {
            textManager.setBufferSizes(bufferBeforeSize, newBufferAfterSize);
            this.adapter.setBufferAfterSize(newBufferAfterSize);
            if (bufferAfterSize >= newBufferAfterSize) {
                this.adapter.notifyItemRangeChanged(currentPosition, Math.min(bufferAfterSize + 1, linkedText.size() - currentPosition));
                bufferAfterSize = newBufferAfterSize;
            } else {
                bufferAfterSize = newBufferAfterSize;
                this.adapter.notifyItemRangeChanged(currentPosition, Math.min(bufferAfterSize + 1, linkedText.size() - currentPosition));
            }
        });

        viewModel.getUseAutoscroll().observe(getViewLifecycleOwner(), useAutoscroll -> {
            if (useAutoscroll && !viewModel.getUseVoiceDetection().getValue()) {
                startAutoScroll();
            } else {
                stopAutoScroll();
            }
        });

        viewModel.getAutoscrollSpeed().observe(getViewLifecycleOwner(), speed -> {
            this.scrollSpeed = speed;
        });
    }

    private void setupRecyclerView() {
        adapter = new WordAdapter(linkedText, this, this, viewModel, bufferBeforeSize, bufferAfterSize);
        recyclerView.setLayoutManager(new SmoothScrollingLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpeechRecognition() {
        if (speechManager == null) {
            speechManager = new SpeechRecognitionManager(requireContext());
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            speechManager.setWordDetectedListener(word -> {
                if (word != null && !word.isEmpty()) {
                    int steps = textManager.searchAndMoveToWord(word, LinkedText.Filters.containsCaseInsensitive(word), false);
                    if (steps != 0) {
                        scrollBySteps(steps, false);
                    }
                }
            });
            speechManager.initialize();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void stopSpeechRecognition() {
        if (speechManager != null) {
            speechManager.destroy();
            speechManager = null;
        }
    }

    @Override
    public void focusOnWord(int targetPosition) {
        textManager.moveSteps(targetPosition - currentPosition);
        adapter.setCurrentPosition(targetPosition);
        recyclerView.smoothScrollToPosition(targetPosition);
        currentPosition = targetPosition;
    }

    @Override
    public void onWordLongClick(String word, int position) {
        PopupMenu popup = new PopupMenu(requireContext(), recyclerView.findViewHolderForAdapterPosition(position).itemView);
        popup.getMenuInflater().inflate(R.menu.word_context_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_add_after) {
                showAddWordDialog(position + 1);
                return true;
            } else if (itemId == R.id.menu_add_before) {
                showAddWordDialog(position);
                return true;
            } else if (itemId == R.id.menu_edit) {
                showEditWordDialog(word, position);
                return true;
            } else if (itemId == R.id.menu_remove) {
                removeWord(position);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showAddWordDialog(int position) {
        EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.hint_enter_new_word));
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_add_word_title))
                .setView(input)
                .setPositiveButton(getString(R.string.dialog_add_word_positive), (dialog, which) -> {
                    String newWord = input.getText().toString().trim();
                    if (!newWord.isEmpty()) {
                        addWord(newWord, position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_add_word_negative), null)
                .show();
    }

    private void showEditWordDialog(String currentWord, int position) {
        EditText input = new EditText(requireContext());
        input.setText(currentWord);
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_edit_word_title))
                .setView(input)
                .setPositiveButton(getString(R.string.dialog_edit_word_positive), (dialog, which) -> {
                    String newWord = input.getText().toString().trim();
                    if (!newWord.isEmpty()) {
                        editWord(newWord, position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_edit_word_negative), null)
                .show();
    }

    private void addWord(String word, int position) {
        linkedText.addWordAt(word, position);
        adapter.notifyItemInserted(position);
        adapter.notifyItemRangeChanged(position, linkedText.size() - position);
    }

    private void editWord(String newWord, int position) {
        linkedText.removeWordAt(position);
        linkedText.addWordAt(newWord, position);
        adapter.notifyItemChanged(position);
    }

    private void removeWord(int position) {
        linkedText.removeWordAt(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, linkedText.size() - position);
    }

    public void scrollBySteps(int steps, boolean checkForSpecialSymbols) {
        if (adapter != null && steps != 0) {
            int targetPosition = currentPosition + steps;
            targetPosition = Math.max(0, Math.min(targetPosition, linkedText.size() - 1));

            if (!checkForSpecialSymbols || !Objects.equals(linkedText.getWord(targetPosition), "\n")) {
                recyclerView.smoothScrollToPosition(targetPosition);
            }
            currentPosition = targetPosition;
            adapter.setCurrentPosition(currentPosition);
        }
    }

    private void startAutoScroll() {
        if (!isAutoScrolling) {
            isAutoScrolling = true;
            handler.post(autoScrollRunnable);
        }
    }

    private void stopAutoScroll() {
        isAutoScrolling = false;
        handler.removeCallbacks(autoScrollRunnable);
    }

    private int calculateScrollDelay() {
        return 60000 / scrollSpeed;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
        stopSpeechRecognition();
    }
} 