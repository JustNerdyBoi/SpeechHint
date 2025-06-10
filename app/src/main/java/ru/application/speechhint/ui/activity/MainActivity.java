package ru.application.speechhint.ui.activity;

import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;

import ru.application.domain.entity.Settings;
import ru.application.speechhint.R;
import ru.application.speechhint.ui.fragment.FileSelectFragment;
import ru.application.speechhint.ui.fragment.SettingsFragment;
import ru.application.speechhint.ui.fragment.TextViewerFragment;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

import androidx.appcompat.app.AppCompatDelegate;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private TeleprompterViewModel teleprompterViewModel;
    private SettingsViewModel settingsViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teleprompterViewModel = new ViewModelProvider(this).get(TeleprompterViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        settingsViewModel.getSettingsLiveData().observe(this, settings -> {
            if (settings.getUiConfig().getTheme().equals("DARK")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(getResources().getColor(R.color.scrimColor));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsDrawerContainer, new SettingsFragment())
                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrameLayout, new FileSelectFragment())
                .commit();

        teleprompterViewModel.getDocumentLiveData().observe(this, document -> {
            if (document != null && !(getSupportFragmentManager().findFragmentById(R.id.mainFrameLayout) instanceof TextViewerFragment)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrameLayout, new TextViewerFragment())
                        .commit();
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }

    }
}
