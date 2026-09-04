package com.dfund.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.ui.voice.VoiceActiveDialogFragment;
import com.dfund.app.ui.voice.VoiceInteractionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 101;
    private BottomNavigationView bottomNav;
    private MaterialButton btnLanguage;
    private MaterialButton btnThemeToggle;
    private VoiceInteractionManager voiceManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply persisted regional language
        String lang = DFundApplication.getInstance() != null 
            ? DFundApplication.getInstance().getSecurityManager().getLanguage() 
            : "en";
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

        voiceManager = new VoiceInteractionManager(this);

        bottomNav = findViewById(R.id.bottom_navigation);
        btnLanguage = findViewById(R.id.btn_language_selector);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);
        MaterialButton btnTopProfile = findViewById(R.id.btn_top_profile);

        updateLanguageButtonLabel();
        btnLanguage.setOnClickListener(v -> showLanguagePickerDialog());

        updateThemeButton();
        btnThemeToggle.setOnClickListener(v -> toggleTheme());

        if (btnTopProfile != null) {
            btnTopProfile.setOnClickListener(v -> {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            });
        }

        // Setup Floating Action Button for Voice
        findViewById(R.id.fab_voice_action).setOnClickListener(v -> {
            VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
            dialog.setVoiceInteractionManager(voiceManager);
            dialog.show(getSupportFragmentManager(), "VOICE_DIALOG");
        });

        // Setup Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                switchFragment(new DashboardFragment(), "DASHBOARD");
                return true;
            } else if (id == R.id.nav_calculator) {
                switchFragment(SipCalculatorFragment.newInstance(500.0, 3), "SIP");
                return true;
            } else if (id == R.id.nav_transactions) {
                switchFragment(TransactionsFragment.newInstance(null), "TRANSACTIONS");
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(ProfileFragment.newInstance(), "PROFILE");
                return true;
            }
            return false;
        });

        // Default to Dashboard Fragment on launch
        if (savedInstanceState == null) {
            switchFragment(new DashboardFragment(), "DASHBOARD");
        }

        checkAndRequestPermissions();
    }

    private void switchFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit();
    }

    private void updateLanguageButtonLabel() {
        String lang = DFundApplication.getInstance().getSecurityManager().getLanguage();
        switch (lang) {
            case "ta":
                btnLanguage.setText("தமிழ்");
                break;
            case "te":
                btnLanguage.setText("తెలుగు");
                break;
            case "ml":
                btnLanguage.setText("മലയാളം");
                break;
            default:
                btnLanguage.setText("English");
                break;
        }
    }

    public void showLanguagePickerDialog() {
        String[] languages = {"English", "தமிழ் (Tamil)", "తెలుగు (Telugu)", "മലയാളം (Malayalam)"};
        String[] codes = {"en", "ta", "te", "ml"};

        new AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages, (dialog, which) -> {
                String chosen = codes[which];
                DFundApplication.getInstance().getSecurityManager().setLanguage(chosen);
                if (voiceManager != null) {
                    voiceManager.updateTtsLocale(chosen);
                }
                recreate(); // Reload activity with selected locale resources
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
        // Automatically scan SMS every time user opens or returns to the app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            scanInboxAndSeedIfEmpty();
        }
    }


    public void scanInboxAndSeedIfEmpty() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            com.dfund.app.data.sms.SmsInboxScanner.scanInbox(this, 100, imported -> {
                runOnUiThread(() -> {
                    if (imported > 0) {
                        android.widget.Toast.makeText(this, "Imported " + imported + " UPI transactions from SMS!", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        // If no bank SMS found in inbox, check if DB is empty and auto-seed sample transactions
                        new Thread(() -> {
                            int count = com.dfund.app.data.local.AppDatabase.getInstance(this).transactionDao().getTransactionCount();
                            if (count == 0) {
                                com.dfund.app.data.mock.MockTransactionGenerator.seedMockData(this);
                            }
                        }).start();
                    }
                });
            });
        } else {
            // If SMS permission not granted, ensure sample data is available for instant demo
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
        boolean isDark = DFundApplication.getInstance().getSecurityManager().isDarkMode();
        if (isDark) {
            btnThemeToggle.setIconResource(R.drawable.ic_mode_light);
            btnThemeToggle.setContentDescription("Switch to White Mode");
        } else {
            btnThemeToggle.setIconResource(R.drawable.ic_mode_dark);
            btnThemeToggle.setContentDescription("Switch to Dark Mode");
        }
    }

    private void toggleTheme() {
        boolean currentlyDark = DFundApplication.getInstance().getSecurityManager().isDarkMode();
        boolean newDark = !currentlyDark;
        DFundApplication.getInstance().getSecurityManager().setThemeMode(newDark ? "dark" : "light");

        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            newDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                    : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );

        android.widget.Toast.makeText(
            this, 
            newDark ? "🌙 Dark Mode Enabled" : "☀️ White Mode Enabled", 
            android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
