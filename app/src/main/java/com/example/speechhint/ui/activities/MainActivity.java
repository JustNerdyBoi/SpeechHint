package com.example.speechhint.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ProgressDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.speechhint.utils.FileManager;
import com.example.speechhint.R;
import com.example.speechhint.ui.fragments.SettingsFragment;
import com.example.speechhint.ui.fragments.TextViewerFragment;
import com.example.speechhint.utils.LinkedText;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Button selectFileButton;
    private LinkedText text = new LinkedText();
    private TextViewerFragment textViewerFragment;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup drawer toggle
        drawerToggle = new ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.settings_title,
            R.string.settings_title
        );
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings_container, settingsFragment)
                        .commit();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (settingsFragment != null) {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .remove(settingsFragment)
                        .commit();
                    settingsFragment = null;
                }
            }
        });
        drawerToggle.syncState();

        selectFileButton = findViewById(R.id.selectFileButton);
        selectFileButton.setOnClickListener(v -> openFilePicker());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            loadFile(fileUri);
                        }
                    }
                }
        );
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
            Toast.makeText(this, getString(R.string.toast_please_install_file_manager), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFile(Uri fileUri) {
        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_file));
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Load file in background
        new Thread(() -> {
            try {
                LinkedText loadedText = FileManager.loadDocument(this, fileUri);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    text = loadedText;
                    Toast.makeText(this, getString(R.string.toast_file_loaded_successfully), Toast.LENGTH_SHORT).show();

                    // Create and add TextViewerFragment
                    textViewerFragment = TextViewerFragment.newInstance(text);
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, textViewerFragment)
                        .commit();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, getString(R.string.toast_error_loading_file, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}