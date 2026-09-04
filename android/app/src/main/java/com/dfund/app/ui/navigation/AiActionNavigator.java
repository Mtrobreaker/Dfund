package com.dfund.app.ui.navigation;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.data.remote.VoiceAction;
import com.dfund.app.ui.DashboardFragment;
import com.dfund.app.ui.SipCalculatorFragment;
import com.dfund.app.ui.TransactionsFragment;
import java.util.Locale;
import java.util.Map;

public class AiActionNavigator {
    private static final String TAG = "DFund.AiNavigator";

    public interface OnLocaleChangeListener {
        void onLocaleChanged(String newLanguageCode);
    }

    public static void executeAction(FragmentActivity activity, VoiceAction action, OnLocaleChangeListener localeListener) {
        if (activity == null || action == null) return;

        String actionType = action.getActionType();
        Map<String, Object> params = action.getParameters();

        Log.d(TAG, "Executing AI Action: " + actionType);

        switch (actionType != null ? actionType : "") {
            case "ACTION_OPEN_SIP_CALCULATOR":
            case "ACTION_ALLOCATE_SURPLUS":
                double amount = 500.0;
                int tenure = 3;
                if (params != null) {
                    if (params.containsKey("amount")) {
                        amount = ((Number) params.get("amount")).doubleValue();
                    } else if (params.containsKey("monthly_amount")) {
                        amount = ((Number) params.get("monthly_amount")).doubleValue();
                    }
                    if (params.containsKey("tenure_years")) {
                        tenure = ((Number) params.get("tenure_years")).intValue();
                    }
                }
                SipCalculatorFragment sipFrag = SipCalculatorFragment.newInstance(amount, tenure);
                navigateTo(activity, sipFrag, "SIP");
                break;

            case "ACTION_SHOW_SPENDING":
                String category = "OTHER";
                if (params != null && params.containsKey("category")) {
                    category = String.valueOf(params.get("category"));
                }
                TransactionsFragment txFrag = TransactionsFragment.newInstance(category);
                navigateTo(activity, txFrag, "TX");
                break;

            case "ACTION_CHECK_EMIS":
                TransactionsFragment emiFrag = TransactionsFragment.newInstance("EMI");
                navigateTo(activity, emiFrag, "TX");
                break;

            case "ACTION_OPEN_PROFILE":
                navigateTo(activity, com.dfund.app.ui.ProfileFragment.newInstance(), "PROFILE");
                break;

            case "ACTION_LOGOUT":
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.logout_dialog_title)
                    .setMessage(R.string.logout_dialog_msg)
                    .setIcon(R.drawable.ic_logout)
                    .setPositiveButton(R.string.logout_confirm, (d, w) -> {
                        DFundApplication.getInstance().getSecurityManager().logout();
                        android.widget.Toast.makeText(activity, R.string.logout_toast, android.widget.Toast.LENGTH_LONG).show();
                        navigateTo(activity, new DashboardFragment(), "DASHBOARD");
                    })
                    .setNegativeButton(R.string.logout_cancel, null)
                    .show();
                break;

            case "ACTION_CHANGE_LANGUAGE":
                if (params != null && params.containsKey("language")) {
                    String lang = String.valueOf(params.get("language"));
                    DFundApplication.getInstance().getSecurityManager().setLanguage(lang);
                    if (localeListener != null) {
                        localeListener.onLocaleChanged(lang);
                    }
                }
                break;

            default:
                // Return to dashboard
                navigateTo(activity, new DashboardFragment(), "DASHBOARD");
                break;
        }
    }

    private static void navigateTo(FragmentActivity activity, Fragment fragment, String tag) {
        activity.getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit();

        com.google.android.material.bottomnavigation.BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
        if (nav != null) {
            try {
                if ("SIP".equals(tag)) {
                    android.view.MenuItem item = nav.getMenu().findItem(R.id.nav_calculator);
                    if (item != null) item.setChecked(true);
                } else if ("TX".equals(tag)) {
                    android.view.MenuItem item = nav.getMenu().findItem(R.id.nav_dashboard);
                    if (item != null) item.setChecked(true);
                } else if ("PROFILE".equals(tag)) {
                    android.view.MenuItem item = nav.getMenu().findItem(R.id.nav_profile);
                    if (item != null) item.setChecked(true);
                } else if ("DASHBOARD".equals(tag)) {
                    android.view.MenuItem item = nav.getMenu().findItem(R.id.nav_dashboard);
                    if (item != null) item.setChecked(true);
                }
            } catch (Exception ignored) {}
        }
    }
}
