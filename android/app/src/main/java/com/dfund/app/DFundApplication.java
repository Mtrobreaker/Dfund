package com.dfund.app;

import android.app.Application;
import com.dfund.app.data.local.AppDatabase;
import com.dfund.app.data.security.SecurityManager;

public class DFundApplication extends Application {
    private static DFundApplication instance;
    private AppDatabase database;
    private SecurityManager securityManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
        securityManager = SecurityManager.getInstance(this);
        if (securityManager.isDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static DFundApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
