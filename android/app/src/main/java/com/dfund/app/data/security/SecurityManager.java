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

    public SecurityManager(Context context) {
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
            .putBoolean(KEY_ONBOARDING_COMPLETED, false)
            .remove(KEY_MOBILE_NUMBER)
            .remove(KEY_PAN_NUMBER)
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

    // --- Onboarding & Financial Profile Storage ---
    private static final String KEY_ONBOARDING_COMPLETED = "key_onboarding_completed";
    private static final String KEY_MOBILE_NUMBER = "key_mobile_number";
    private static final String KEY_PAN_NUMBER = "key_pan_number";
    private static final String KEY_JOB_TYPE = "key_job_type";
    private static final String KEY_INCOME_FREQUENCY = "key_income_frequency";
    private static final String KEY_TYPICAL_INCOME = "key_typical_income";
    private static final String KEY_MANDATORY_EXPENSES_TOTAL = "key_mandatory_expenses_total";
    private static final String KEY_DESIRED_SAVINGS = "key_desired_savings";
    private static final String KEY_DESIRED_INVESTMENT = "key_desired_investment";
    private static final String KEY_FINANCIAL_PRIORITY = "key_financial_priority";
    private static final String KEY_VOICE_NAV_ENABLED = "key_voice_nav_enabled";
    private static final String KEY_SMS_PERMISSION_GRANTED = "key_sms_permission_granted";

    public boolean isOnboardingCompleted() {
        return securePrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        securePrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public String getMobileNumber() {
        return securePrefs.getString(KEY_MOBILE_NUMBER, "9876543210");
    }

    public void setMobileNumber(String mobile) {
        securePrefs.edit().putString(KEY_MOBILE_NUMBER, mobile).apply();
    }

    public String getPanNumber() {
        return securePrefs.getString(KEY_PAN_NUMBER, "");
    }

    public void setPanNumber(String pan) {
        securePrefs.edit().putString(KEY_PAN_NUMBER, pan).apply();
    }

    public String getJobType() {
        return securePrefs.getString(KEY_JOB_TYPE, "gig");
    }

    public void setJobType(String jobType) {
        securePrefs.edit().putString(KEY_JOB_TYPE, jobType).apply();
    }

    public String getIncomeFrequency() {
        return securePrefs.getString(KEY_INCOME_FREQUENCY, "monthly");
    }

    public void setIncomeFrequency(String frequency) {
        securePrefs.edit().putString(KEY_INCOME_FREQUENCY, frequency).apply();
    }

    public float getTypicalIncome() {
        return securePrefs.getFloat(KEY_TYPICAL_INCOME, 24000.0f);
    }

    public void setTypicalIncome(float income) {
        securePrefs.edit().putFloat(KEY_TYPICAL_INCOME, income).apply();
    }

    public void setTypicalIncome(double income) {
        setTypicalIncome((float) income);
    }

    public float getMandatoryExpensesTotal() {
        return securePrefs.getFloat(KEY_MANDATORY_EXPENSES_TOTAL, 14000.0f);
    }

    public void setMandatoryExpensesTotal(float total) {
        securePrefs.edit().putFloat(KEY_MANDATORY_EXPENSES_TOTAL, total).apply();
    }

    public void setMandatoryExpensesTotal(double total) {
        setMandatoryExpensesTotal((float) total);
    }

    public float getDesiredSavings() {
        return securePrefs.getFloat(KEY_DESIRED_SAVINGS, 3000.0f);
    }

    public void setDesiredSavings(float savings) {
        securePrefs.edit().putFloat(KEY_DESIRED_SAVINGS, savings).apply();
    }

    public void setDesiredSavings(double savings) {
        setDesiredSavings((float) savings);
    }

    public float getDesiredInvestment() {
        return securePrefs.getFloat(KEY_DESIRED_INVESTMENT, 1000.0f);
    }

    public void setDesiredInvestment(float investment) {
        securePrefs.edit().putFloat(KEY_DESIRED_INVESTMENT, investment).apply();
    }

    public void setDesiredInvestment(double investment) {
        setDesiredInvestment((float) investment);
    }

    public String getFinancialPriority() {
        return securePrefs.getString(KEY_FINANCIAL_PRIORITY, "manage_expenses");
    }

    public void setFinancialPriority(String priority) {
        securePrefs.edit().putString(KEY_FINANCIAL_PRIORITY, priority).apply();
    }

    public boolean isVoiceNavEnabled() {
        return securePrefs.getBoolean(KEY_VOICE_NAV_ENABLED, true);
    }

    public void setVoiceNavEnabled(boolean enabled) {
        securePrefs.edit().putBoolean(KEY_VOICE_NAV_ENABLED, enabled).apply();
    }

    public boolean isSmsAccessGranted() {
        return securePrefs.getBoolean(KEY_SMS_PERMISSION_GRANTED, true);
    }

    public void setSmsAccessGranted(boolean granted) {
        securePrefs.edit().putBoolean(KEY_SMS_PERMISSION_GRANTED, granted).apply();
    }
}
