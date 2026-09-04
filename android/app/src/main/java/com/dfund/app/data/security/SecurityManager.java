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

    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_RISK_LEVEL = "key_risk_level";
    private static final String KEY_MONTHLY_BUFFER = "key_monthly_buffer";

    public String getUserName() {
        return securePrefs.getString(KEY_USER_NAME, "Karthik Raja");
    }

    public void setUserName(String name) {
        securePrefs.edit().putString(KEY_USER_NAME, name != null ? name.trim() : "Valued User").apply();
    }

    public String getRiskLevel() {
        return securePrefs.getString(KEY_RISK_LEVEL, "moderate");
    }

    public void setRiskLevel(String riskLevel) {
        securePrefs.edit().putString(KEY_RISK_LEVEL, riskLevel).apply();
    }

    public float getMonthlySafetyBuffer() {
        return securePrefs.getFloat(KEY_MONTHLY_BUFFER, 3000.0f);
    }

    public void setMonthlySafetyBuffer(float buffer) {
        securePrefs.edit().putFloat(KEY_MONTHLY_BUFFER, buffer).apply();
    }

    public void logout() {
        // Clear user session data securely while preserving theme and language
        String currentLang = getLanguage();
        String currentTheme = getThemeMode();
        String serverUrl = getServerUrl();

        securePrefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_RISK_LEVEL)
            .remove(KEY_MONTHLY_BUFFER)
            .remove(KEY_BIOMETRIC_ENABLED)
            .putString(KEY_DEVICE_ID, "dfund_" + UUID.randomUUID().toString().substring(0, 12))
            .apply();

        setLanguage(currentLang);
        setThemeMode(currentTheme);
        setServerUrl(serverUrl);
        Log.i(TAG, "User logged out successfully. Session reset.");
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
