package com.dfund.app.data.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.util.UUID;

public class SecurityManager {
    private static final String TAG = "DFund.SecurityManager";
    private static final String PREFS_NAME = "dfund_secure_prefs";
    private static final String KEY_DEVICE_ID = "key_device_id";
    private static final String KEY_LANGUAGE = "key_language";
    private static final String KEY_BIOMETRIC_ENABLED = "key_biometric_enabled";
    private static final String KEY_SERVER_URL = "key_server_url";
    private static final String KEY_THEME_MODE = "key_theme_mode";
    private static volatile SecurityManager instance;
    private SharedPreferences securePrefs;

    private SecurityManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

            securePrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, fallback to standard", e);
            securePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context.getApplicationContext());
        }
        return instance;
    }

    public String getDeviceId() {
        String id = securePrefs.getString(KEY_DEVICE_ID, null);
        if (id == null) {
            id = "dfund_" + UUID.randomUUID().toString().substring(0, 12);
            securePrefs.edit().putString(KEY_DEVICE_ID, id).apply();
        }
        return id;
    }

    public String getLanguage() {
        return securePrefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String language) {
        securePrefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public boolean isBiometricEnabled() {
        return securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        securePrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public String getServerUrl() {
        return securePrefs.getString(KEY_SERVER_URL, "http://10.0.2.2:8000/");
    }

    public void setServerUrl(String url) {
        if (url != null && !url.endsWith("/")) {
            url = url + "/";
        }
        securePrefs.edit().putString(KEY_SERVER_URL, url).apply();
    }

    public String getThemeMode() {
        return securePrefs.getString(KEY_THEME_MODE, "light");
    }

    public void setThemeMode(String mode) {
        securePrefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }

    public boolean isDarkMode() {
        return "dark".equalsIgnoreCase(getThemeMode());
    }
}
