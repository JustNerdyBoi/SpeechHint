package ru.application.speechhint.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ru.application.speechhint.R;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

public class FileSelectFragment extends Fragment {

    private TeleprompterViewModel viewModel;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);
        setupFilePicker();

        Button loadLocalBtn = view.findViewById(R.id.button_load_local_file);
        Button loadGoogleBtn = view.findViewById(R.id.button_load_google_file);
        Button loadYandexBtn = view.findViewById(R.id.button_load_yandex_file);

        loadLocalBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.oasis.opendocument.text"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            filePickerLauncher.launch(intent);
        });

        loadGoogleBtn.setOnClickListener(v -> showInputLinkDialog(
                getString(R.string.enter_google_link),
                link -> viewModel.LoadGoogleDocument(link)
        ));

        loadYandexBtn.setOnClickListener(v -> showInputLinkDialog(
                getString(R.string.enter_yandex_link),
                link -> viewModel.LoadYandexDocument(link)
        ));
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                try {
                                    viewModel.LoadLocalDocument(uri);
                                } catch (Exception e) {
                                    Log.e("ERROR", e.toString());
                                    Toast.makeText(this.requireContext(), "Error loading document", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                }
        );
    }

    private void showInputLinkDialog(String title, OnLinkEnteredListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String link = input.getText().toString().trim();
            if (!link.isEmpty()) {
                listener.onLinkEntered(link);
            } else {
                Toast.makeText(requireContext(), R.string.link_empty, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private interface OnLinkEnteredListener {
        void onLinkEntered(String link);
    }

}