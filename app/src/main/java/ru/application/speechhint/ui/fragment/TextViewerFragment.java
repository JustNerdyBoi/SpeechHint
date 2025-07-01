package ru.application.speechhint.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.SttConfig;
import ru.application.domain.entity.UIConfig;
import ru.application.domain.entity.Word;
import ru.application.speechhint.R;
import ru.application.speechhint.ui.adapter.WordAdapter;
import ru.application.speechhint.ui.animator.AutoScroller;
import ru.application.speechhint.ui.layouts.WordWallLayoutManager;
import ru.application.speechhint.viewmodel.ServerViewModel;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.speechhint.viewmodel.SpeechRecognitionViewModel;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

public class TextViewerFragment extends Fragment {
    private TeleprompterViewModel teleprompterViewModel;
    private SettingsViewModel settingsViewModel;
    private SpeechRecognitionViewModel speechRecognitionViewModel;
    private ServerViewModel serverViewModel;
    private Document document;
    private int textScale;
    private boolean isScrollingToWord = false;
    private boolean highlighterFollow = false;

    RecyclerView recyclerView;
    AutoScroller scroller;
    View lineView;
    View pointerView;
    View lighterView;
    View currentHighlighter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        teleprompterViewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        speechRecognitionViewModel = new ViewModelProvider(requireActivity()).get(SpeechRecognitionViewModel.class);
        serverViewModel = new ViewModelProvider(requireActivity()).get(ServerViewModel.class);


        document = teleprompterViewModel.getDocumentLiveData().getValue();
        textScale = settingsViewModel.getSettingsLiveData().getValue().getUiConfig().getTextScale();

        recyclerView = view.findViewById(R.id.recyclerView);

        lineView = view.findViewById(R.id.lineView);
        pointerView = view.findViewById(R.id.pointerView);
        lighterView = view.findViewById(R.id.lighterView);

        scroller = new AutoScroller(recyclerView);

        setupListeners();
        setupRecyclerView(document, textScale);
        recyclerView.post(() -> setupHighlighter(settingsViewModel.getSettingsLiveData().getValue().getUiConfig()));
    }

    private void setupListeners() {
        teleprompterViewModel.getDocumentLiveData().observe(getViewLifecycleOwner(), newDocument -> {
            if (newDocument == null) {
                getParentFragmentManager().beginTransaction().remove(this).commit();
            } else {
                document = newDocument;
                setupRecyclerView(newDocument, textScale);
            }
        });

        settingsViewModel.getSettingsLiveData().observe(getViewLifecycleOwner(), settings -> {
            if (textScale != settings.getUiConfig().getTextScale()) {
                int newTextScale = settings.getUiConfig().getTextScale();
                setupRecyclerView(document, newTextScale);
                pointerView.setScaleY(newTextScale);
                lighterView.setScaleY(newTextScale);
            }
            textScale = settings.getUiConfig().getTextScale();

            setupHighlighter(settings.getUiConfig());

            if (settings.getUiConfig().isMirrorText()) {
                recyclerView.setScaleY(-1);
            } else {
                recyclerView.setScaleY(1);
            }

            if (settings.getScrollConfig().isEnableAutoScroll()) {
                if (settings.getSttConfig().isSttEnabled()) {
                    scroller.startScrolling(0);
                    speechRecognitionViewModel.startSpeechRecognition();
                    startScrollingToWord();
                } else {
                    speechRecognitionViewModel.stopSpeechRecognition();
                    stopScrollingToWord();
                    scroller.startScrolling(settings.getScrollConfig().getSpeed());
                }
            } else {
                speechRecognitionViewModel.stopSpeechRecognition();
                stopScrollingToWord();
                scroller.stopScrolling();
            }
        });

        speechRecognitionViewModel.getRecognizedWord().observe(getViewLifecycleOwner(), word -> {
            if (word == null) return;
            Document doc = teleprompterViewModel.getDocumentLiveData().getValue();
            SttConfig sttConfig = settingsViewModel.getSettingsLiveData().getValue().getSttConfig();
            teleprompterViewModel.onWordRecognized(word, doc, sttConfig);
        });

        serverViewModel.getReceivedScrollLiveData().observe(getViewLifecycleOwner(), scroll -> {
            if (scroll != null) {
                recyclerView.scrollBy(0, (int) (scroll * recyclerView.getHeight()));
                serverViewModel.clearReceivedScrollLiveData();
            }
        });
    }

    private void scrollToWordPosition() {
        if (recyclerView == null) return;
        Integer position = teleprompterViewModel.getCurrentPositionLiveData().getValue();
        if (position == null) return;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        View wordView = layoutManager.findViewByPosition(position);
        if (wordView == null) return;

        float targetScroll = (wordView.getTop() - (float) (recyclerView.getHeight() / 8)) / recyclerView.getResources().getDisplayMetrics().density;

        float speed = (float) (0.1 * Math.pow(Math.abs(targetScroll), 1.4));
        if (Math.abs(targetScroll) < 20) speed = 0;

        if (targetScroll < 0) speed = -speed;

        scroller.setSpeed(speed);
    }

    private void startScrollingToWord() {
        if (!isScrollingToWord) {
            isScrollingToWord = true;
            Choreographer.getInstance().postFrameCallback(scrollToWordCallback);
        }
    }

    private void startHighlighterFollow() {
        if (!highlighterFollow) {
            highlighterFollow = true;
            Choreographer.getInstance().postFrameCallback(highlighterFollowCallback);
        }
    }

    private void stopScrollingToWord() {
        if (isScrollingToWord) {
            isScrollingToWord = false;
            Choreographer.getInstance().removeFrameCallback(scrollToWordCallback);
            scroller.setSpeed(0f);
        }
    }

    private void stopHighlighterFollow() {
        if (highlighterFollow) {
            highlighterFollow = false;
            Choreographer.getInstance().removeFrameCallback(highlighterFollowCallback);
        }
    }

    private final Choreographer.FrameCallback scrollToWordCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            scrollToWordPosition();
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private final Choreographer.FrameCallback highlighterFollowCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            translateHighlighterToWord();
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private void setupRecyclerView(Document document, int textScale) {
        Log.i("TextViewerFragment", "Deploying new document with " + document.getWords().size() + " words.");
        WordAdapter wordAdapter = new WordAdapter(document, textScale, new WordAdapter.OnWordClickListener() {
            @Override
            public void onWordClick(Word word, int position) {
                teleprompterViewModel.setCurrentPosition(position);
            }

            @Override
            public void onWordLongClick(Word word, int position, View anchor) {
                PopupMenu popup = new PopupMenu(requireContext(), anchor);
                popup.getMenu().add(Menu.NONE, 1, 1, R.string.edit_word);
                popup.getMenu().add(Menu.NONE, 2, 2, R.string.delete_word);
                popup.getMenu().add(Menu.NONE, 3, 3, R.string.add_word_before);
                popup.getMenu().add(Menu.NONE, 4, 4, R.string.add_word_after);

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 1:
                            showInputDialog(requireContext().getString(R.string.edit_word), word.getText(), newText -> {
                                teleprompterViewModel.editWord(position, newText);
                            });
                            return true;
                        case 2:
                            teleprompterViewModel.removeWord(position);
                            return true;
                        case 3:
                            showInputDialog(requireContext().getString(R.string.add_word_before), "", newText -> {
                                teleprompterViewModel.addWord(position, newText);
                            });
                            return true;
                        case 4:
                            showInputDialog(requireContext().getString(R.string.add_word_after), "", newText -> {
                                teleprompterViewModel.addWord(position + 1, newText);
                            });
                            return true;
                    }
                    return false;
                });
                popup.show();
            }
        });
        recyclerView.setLayoutManager(new WordWallLayoutManager());
        recyclerView.setAdapter(wordAdapter);
    }

    private void translateHighlighterToWord() {
        if (recyclerView == null || currentHighlighter == null) return;
        Integer position = teleprompterViewModel.getCurrentPositionLiveData().getValue();
        if (position == null) return;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        View wordView = layoutManager.findViewByPosition(position);
        if (wordView == null) return;

        if (settingsViewModel.getSettingsLiveData().getValue().getUiConfig().isMirrorText()) {
            currentHighlighter.setY(recyclerView.getHeight() - (wordView.getY() + spToPx(textScale) + (currentHighlighter == lineView ? currentHighlighter.getHeight() * 2 : 0)));
        } else {
            currentHighlighter.setY(wordView.getY() + spToPx(textScale) + (currentHighlighter == lineView ? currentHighlighter.getHeight() : -currentHighlighter.getHeight() * currentHighlighter.getScaleY() * 0.1f));
        }
    }

    private void setupHighlighter(UIConfig uiConfig) {
        pointerView.setScaleY(uiConfig.getTextScale());
        lighterView.setScaleY(uiConfig.getTextScale());
        if (uiConfig.isCurrentStringHighlight()) {
            switch (uiConfig.getHighlightType()) {
                case LINE:
                    currentHighlighter = lineView;
                    break;
                case POINTER:
                    currentHighlighter = pointerView;
                    break;
                case LIGHT_ZONE:
                    currentHighlighter = lighterView;
                    break;
            }
        } else {
            currentHighlighter = null;
            stopHighlighterFollow();
        }

        lineView.setVisibility(View.GONE);
        pointerView.setVisibility(View.GONE);
        lighterView.setVisibility(View.GONE);

        if (currentHighlighter != null) {
            currentHighlighter.setVisibility(View.VISIBLE);
            if (uiConfig.isCurrentWordHighlightFollow()) {
                startHighlighterFollow();
            } else {
                stopHighlighterFollow();
                currentHighlighter.setY(uiConfig.isMirrorText() ? (
                        recyclerView.getHeight() - currentHighlighter.getHeight()) * (uiConfig.getHighlightHeight()) :
                        (recyclerView.getHeight() - currentHighlighter.getHeight()) * (1 - uiConfig.getHighlightHeight()));
                Log.i("SETTINGS", uiConfig.getHighlightHeight() + " " + currentHighlighter.getHeight());
            }
        }
    }

    private void showInputDialog(String title, String initialText, OnTextEnteredListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        final EditText input = new EditText(requireContext());
        input.setText(initialText);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newText = input.getText().toString();
            listener.onTextEntered(newText);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public interface OnTextEnteredListener {
        void onTextEntered(String text);
    }

    @Override
    public void onDestroy() {
        stopScrollingToWord();
        stopHighlighterFollow();
        super.onDestroy();
    }

    public int spToPx(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, recyclerView.getResources().getDisplayMetrics());
    }
}