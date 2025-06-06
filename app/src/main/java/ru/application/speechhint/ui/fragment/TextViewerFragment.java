package ru.application.speechhint.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ru.application.domain.entity.Word;
import ru.application.speechhint.R;
import ru.application.speechhint.ui.adapter.WordAdapter;
import ru.application.speechhint.ui.layouts.WordWallLayoutManager;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

public class TextViewerFragment extends Fragment {
    private TeleprompterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        viewModel.getDocumentLiveData().observe(getViewLifecycleOwner(), document -> {
            WordAdapter wordAdapter = new WordAdapter(document, new WordAdapter.OnWordClickListener() {
                @Override
                public void onWordClick(Word word, int position) {
                    // TODO: Реагировать на клик
                }
                @Override
                public void onWordLongClick(Word word, int position) {
                    // TODO: Реагировать на долгий клик
                }
            });
            recyclerView.setLayoutManager(new WordWallLayoutManager());
            recyclerView.setAdapter(wordAdapter);
        });
    }
}