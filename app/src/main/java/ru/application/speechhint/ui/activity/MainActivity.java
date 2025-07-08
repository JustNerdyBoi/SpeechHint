package ru.application.speechhint.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Theme;
import ru.application.speechhint.R;
import ru.application.speechhint.ui.dialog.SubscriptionDialog;
import ru.application.speechhint.ui.fragment.FileSelectFragment;
import ru.application.speechhint.ui.fragment.ServerFragment;
import ru.application.speechhint.ui.fragment.SettingsFragment;
import ru.application.speechhint.ui.fragment.TextViewerFragment;
import ru.application.speechhint.viewmodel.BillingViewModel;
import ru.application.speechhint.viewmodel.ServerViewModel;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private TeleprompterViewModel teleprompterViewModel;
    private SettingsViewModel settingsViewModel;
    private ServerViewModel serverViewModel;
    private BillingViewModel billingViewModel;
    @Inject
    RuStoreBillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            billingClient.onNewIntent(getIntent());
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teleprompterViewModel = new ViewModelProvider(this).get(TeleprompterViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        serverViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
        billingViewModel = new ViewModelProvider(this).get(BillingViewModel.class);

        setupListeners();

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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.serverDrawerContainer, new ServerFragment())
                .commit();
    }

    private void setupListeners() {
        settingsViewModel.getSettingsLiveData().observe(this, settings -> {
            if (settings.getSttConfig().isSttEnabled()) {
                switch (billingViewModel.checkSubscription("speech_recognition_purchase")) {
                    case NOT_ACTIVE:
                        settings.getSttConfig().setSttEnabled(false);
                        SubscriptionDialog.show(this, () -> billingViewModel.purchaseSubscription("speech_recognition_purchase"));
                        break;

                    case NO_DATA:
                        settings.getSttConfig().setSttEnabled(false);
                        Toast.makeText(this, R.string.paid_feature_update_toast, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            serverViewModel.setServerCurrentSettings(settings);
            if (settings.getUiConfig().getTheme() == Theme.DARK) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        teleprompterViewModel.getDocumentLiveData().observe(this, document -> {
            serverViewModel.setServerCurrentDocument(document);
            if (document != null && !(getSupportFragmentManager().findFragmentById(R.id.mainFrameLayout) instanceof TextViewerFragment)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrameLayout, new TextViewerFragment())
                        .commit();
            }
        });

        teleprompterViewModel.getCurrentPositionLiveData().observe(this, position -> {
            serverViewModel.setServerCurrentPosition(position);
        });

        serverViewModel.getReceivedDocumentLiveData().observe(this, document -> {
            if (document != null) {
                teleprompterViewModel.setDocument(document);
                serverViewModel.clearReceivedDocumentLiveData();
            }
        });

        serverViewModel.getReceivedSettingsLiveData().observe(this, settings -> {
            if (settings != null) {
                settingsViewModel.saveSettings(settings);
                serverViewModel.clearReceivedSettingsLiveData();
            }
        });

        serverViewModel.getReceivedPositionLiveData().observe(this, position -> {
            if (position != null) {
                Document document = teleprompterViewModel.getDocumentLiveData().getValue();
                if (document != null && 0 <= position && position < document.getWords().size())
                    teleprompterViewModel.setCurrentPosition(position);
                serverViewModel.clearReceivedPositionLiveData();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30 and above
                WindowInsetsController insetsController = getWindow().getInsetsController();
                if (insetsController != null) {
                    insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    insetsController.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    );
                }
            } else {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        billingClient.onNewIntent(intent);
    }
}
