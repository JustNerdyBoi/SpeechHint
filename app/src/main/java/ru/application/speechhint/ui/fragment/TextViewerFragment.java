package ru.application.speechhint.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import ru.application.domain.entity.Word;
import ru.application.speechhint.R;
import ru.application.speechhint.ui.adapter.WordAdapter;
import ru.application.speechhint.ui.animator.AutoScroller;
import ru.application.speechhint.ui.layouts.WordWallLayoutManager;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

public class TextViewerFragment extends Fragment {
    private TeleprompterViewModel viewModel;

    RecyclerView recyclerView;
    AutoScroller scroller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        scroller = new AutoScroller(recyclerView);

        viewModel.getDocumentLiveData().observe(getViewLifecycleOwner(), document -> {
            if (document == null) {
                getParentFragmentManager()
                        .beginTransaction()
                        .remove(this)
                        .commit();

            } else {
                Log.i("TextViewerFragment", "Deploying document with " + document.getWords().size() + " words.");
                setupRecyclerView(document);
            }
        });
    }

    private void setupRecyclerView(Document document) {
        WordAdapter wordAdapter = new WordAdapter(document, new WordAdapter.OnWordClickListener() {
            @Override
            public void onWordClick(Word word, int position) {
                // TODO: Реагировать на клик
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
                                viewModel.editWord(position, newText);
                            });
                            return true;
                        case 2:
                            viewModel.removeWord(position);
                            return true;
                        case 3:
                            showInputDialog(requireContext().getString(R.string.add_word_before), "", newText -> {
                                viewModel.addWord(position, newText);
                            });
                            return true;
                        case 4:
                            showInputDialog(requireContext().getString(R.string.add_word_after), "", newText -> {
                                viewModel.addWord(position + 1, newText);
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
    public void onDestroy(){
        super.onDestroy();
    }

}