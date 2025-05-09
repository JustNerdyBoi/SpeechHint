package com.example.speechhint;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    private TextView textView;
    private Button selectFileButton;
    private SpeechRecognitionManager speechManager;
    private LinkedText text = new LinkedText();
    private LinkedText.TextManager textManager = text.new TextManager(5, 10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textView = findViewById(R.id.textView);
        selectFileButton = findViewById(R.id.selectFileButton);

        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            try {
                                text = FileManager.loadDocument(this, fileUri);
                                textManager = text.new TextManager(5, 10);
                                textView.setText(textManager.getBufferString());
                                Toast.makeText(this, "File loaded successfully", Toast.LENGTH_SHORT).show();

                            } catch (IOException e) {
                                Toast.makeText(this, "Error loading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );

        speechManager = new SpeechRecognitionManager(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            filePickerLauncher.launch(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a file manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSpeechRecognition() {
        speechManager.setWordDetectedListener(word -> {
            if (word != null && !word.isEmpty()) {
                textManager.searchAndMoveToWord(word, LinkedText.Filters.contains(word), false);
                textView.setText(textManager.getBufferString());
            }
        });
        speechManager.initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechManager.destroy();
    }
}