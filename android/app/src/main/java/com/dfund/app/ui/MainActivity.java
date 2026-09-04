package com.dfund.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.dfund.app.ui.onboarding.SplashFragment;
import com.dfund.app.ui.voice.VoiceActiveDialogFragment;
import com.dfund.app.ui.voice.VoiceInteractionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 101;
    private BottomNavigationView bottomNav;
    private View bottomNavContainer;
    private View fabVoiceAction;
    private MaterialButton btnLanguage;
    private MaterialButton btnThemeToggle;
    private VoiceInteractionManager voiceManager;
    private SecurityManager securityManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = "en";
        try {
            if (DFundApplication.getInstance() != null) {
                lang = DFundApplication.getInstance().getSecurityManager().getLanguage();
            }
        } catch (Exception ignored) {}
        if (lang == null || lang.isEmpty()) lang = "en";

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        securityManager = new SecurityManager(this);
        voiceManager = new VoiceInteractionManager(this);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNavContainer = findViewById(R.id.bottom_nav_container);
        fabVoiceAction = findViewById(R.id.fab_voice_action);
        btnLanguage = findViewById(R.id.btn_language_selector);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);
        MaterialButton btnTopProfile = findViewById(R.id.btn_top_profile);

        if (btnLanguage != null) {
            updateLanguageButtonLabel();
            btnLanguage.setOnClickListener(v -> showLanguagePickerDialog());
        }

        if (btnThemeToggle != null) {
            updateThemeButton();
            btnThemeToggle.setOnClickListener(v -> toggleTheme());
        }

        if (btnTopProfile != null) {
            btnTopProfile.setOnClickListener(v -> navigateToProfile());
        }

        // Setup Floating Action Button for Voice ("Tap to speak")
        if (fabVoiceAction != null) {
            fabVoiceAction.setOnClickListener(v -> {
                VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
                dialog.setVoiceInteractionManager(voiceManager);
                dialog.show(getSupportFragmentManager(), "VOICE_DIALOG");
            });
        }

        // Setup 3-Tab Bottom Navigation (Home, Savings, Profile)
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                switchFragment(new DashboardFragment(), "DASHBOARD");
                return true;
            } else if (id == R.id.nav_calculator) {
                switchFragment(SipCalculatorFragment.newInstance(1500.0, 3), "SIP");
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(ProfileFragment.newInstance(), "PROFILE");
                return true;
            }
            return false;
        });

        // Launch Onboarding / Splash or Dashboard
        if (savedInstanceState == null) {
            if (securityManager.isOnboardingCompleted()) {
                setBottomNavVisibility(true);
                switchFragment(new DashboardFragment(), "DASHBOARD");
            } else {
                setBottomNavVisibility(false);
                switchFragment(new SplashFragment(), "SPLASH");
            }
        }

        checkAndRequestPermissions();
    }

    public void setBottomNavVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (bottomNavContainer != null) bottomNavContainer.setVisibility(visibility);
        if (fabVoiceAction != null) fabVoiceAction.setVisibility(visibility);
    }

    public void navigateToProfile() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        } else {
            switchFragment(ProfileFragment.newInstance(), "PROFILE");
        }
    }

    public void switchFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commitAllowingStateLoss();
    }

    private void updateLanguageButtonLabel() {
        if (btnLanguage == null) return;
        String lang = securityManager.getLanguage();
        if ("ta".equals(lang)) btnLanguage.setText("தமிழ்");
        else if ("te".equals(lang)) btnLanguage.setText("తెలుగు");
        else if ("hi".equals(lang)) btnLanguage.setText("हिंदी");
        else btnLanguage.setText("English");
    }

    public void showLanguagePickerDialog() {
        String[] languages = {"English", "हिंदी (Hindi)", "தமிழ் (Tamil)", "తెలుగు (Telugu)"};
        String[] codes = {"en", "hi", "ta", "te"};

        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    String chosen = codes[which];
                    securityManager.setLanguage(chosen);
                    if (voiceManager != null) {
                        voiceManager.updateTtsLocale(chosen);
                    }
                    recreate();
                })
                .setNegativeButton(R.string.btn_close, null)
                .show();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECORD_AUDIO
        };

        boolean needed = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needed = true;
                break;
            }
        }

        if (needed) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE);
        } else {
            scanInboxAndSeedIfEmpty();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            scanInboxAndSeedIfEmpty();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            scanInboxAndSeedIfEmpty();
        }
    }

    public void scanInboxAndSeedIfEmpty() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            com.dfund.app.data.sms.SmsInboxScanner.scanInbox(this, 100, imported -> {
                runOnUiThread(() -> {
                    new Thread(() -> {
                        int count = com.dfund.app.data.local.AppDatabase.getInstance(this).transactionDao().getTransactionCount();
                        if (count == 0) {
                            com.dfund.app.data.mock.MockTransactionGenerator.seedMockData(this);
                        }
                    }).start();
                });
            });
        } else {
            new Thread(() -> {
                int count = com.dfund.app.data.local.AppDatabase.getInstance(this).transactionDao().getTransactionCount();
                if (count == 0) {
                    com.dfund.app.data.mock.MockTransactionGenerator.seedMockData(this);
                }
            }).start();
        }
    }

    private void updateThemeButton() {
        if (btnThemeToggle == null) return;
        boolean isDark = securityManager.isDarkMode();
        btnThemeToggle.setIconResource(isDark ? R.drawable.ic_mode_light : R.drawable.ic_mode_dark);
        btnThemeToggle.setContentDescription(isDark ? "Switch to White Mode" : "Switch to Dark Mode");
    }

    private void toggleTheme() {
        boolean currentlyDark = securityManager.isDarkMode();
        boolean newDark = !currentlyDark;
        securityManager.setThemeMode(newDark ? "dark" : "light");

        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                newDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
