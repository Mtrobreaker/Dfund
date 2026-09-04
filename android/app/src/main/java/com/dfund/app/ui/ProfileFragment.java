package com.dfund.app.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private TextView tvProfileName;
    private TextView tvProfileAccountId;
    private TextView tvBufferValue;
    private MaterialButton btnBufferMinus;
    private MaterialButton btnBufferPlus;
    private MaterialButton btnEditName;
    private MaterialButton btnProfileLang;
    private MaterialButton btnLogoutAction;
    private ChipGroup chipGroupRisk;
    private Chip chipRiskConservative;
    private Chip chipRiskModerate;
    private Chip chipRiskAggressive;
    private MaterialSwitch switchThemeMode;
    private MaterialSwitch switchBiometric;
    private SecurityManager securityManager;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = DFundApplication.getInstance().getSecurityManager();

        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileAccountId = view.findViewById(R.id.tv_profile_account_id);
        tvBufferValue = view.findViewById(R.id.tv_buffer_value);
        btnBufferMinus = view.findViewById(R.id.btn_buffer_minus);
        btnBufferPlus = view.findViewById(R.id.btn_buffer_plus);
        btnEditName = view.findViewById(R.id.btn_edit_profile_name);
        btnProfileLang = view.findViewById(R.id.btn_profile_lang);
        btnLogoutAction = view.findViewById(R.id.btn_logout_action);

        chipGroupRisk = view.findViewById(R.id.chip_group_risk);
        chipRiskConservative = view.findViewById(R.id.chip_risk_conservative);
        chipRiskModerate = view.findViewById(R.id.chip_risk_moderate);
        chipRiskAggressive = view.findViewById(R.id.chip_risk_aggressive);

        switchThemeMode = view.findViewById(R.id.switch_theme_mode);
        switchBiometric = view.findViewById(R.id.switch_biometric);

        loadProfileData(view);
        setupListeners(view);
    }

    private void loadProfileData(View view) {
        tvProfileName.setText(securityManager.getUserName());
        String devId = securityManager.getDeviceId();
        tvProfileAccountId.setText(getString(R.string.profile_account_id, devId));

        float buffer = securityManager.getMonthlySafetyBuffer();
        tvBufferValue.setText(String.format(Locale.getDefault(), "₹%,.2f", buffer));

        String risk = securityManager.getRiskLevel();
        if ("conservative".equalsIgnoreCase(risk)) {
            chipRiskConservative.setChecked(true);
        } else if ("aggressive".equalsIgnoreCase(risk)) {
            chipRiskAggressive.setChecked(true);
        } else {
            chipRiskModerate.setChecked(true);
        }

        switchThemeMode.setChecked(securityManager.isDarkMode());
        switchBiometric.setChecked(securityManager.isBiometricEnabled());        TextView tvFinSummary = view.findViewById(R.id.tv_profile_financial_summary);
        if (tvFinSummary != null) {
            String job = securityManager.getJobType();
            if (job == null || job.isEmpty()) job = "Salaried";
            double inc = securityManager.getTypicalIncome();
            if (inc <= 0) inc = 25000;
            double ess = securityManager.getMandatoryExpensesTotal();
            if (ess <= 0) ess = 16800;
            tvFinSummary.setText(String.format(Locale.getDefault(), "Work: %s · Income: ₹%,.0f · Essentials: ₹%,.0f", job, inc, ess));
        }

        String lang = securityManager.getLanguage();
        if ("ta".equals(lang)) {
            btnProfileLang.setText("தமிழ்");
        } else if ("te".equals(lang)) {
            btnProfileLang.setText("తెలుగు");
        } else if ("ml".equals(lang)) {
            btnProfileLang.setText("മലയാളം");
        } else {
            btnProfileLang.setText("English");
        }
    }

    private void setupListeners(View view) {
        // Edit Name Dialog
        btnEditName.setOnClickListener(v -> showEditNameDialog());

        // Safety Buffer adjustments
        btnBufferMinus.setOnClickListener(v -> {
            float current = securityManager.getMonthlySafetyBuffer();
            float updated = Math.max(500.0f, current - 500.0f);
            securityManager.setMonthlySafetyBuffer(updated);
            tvBufferValue.setText(String.format(Locale.getDefault(), "₹%,.2f", updated));
        });

        btnBufferPlus.setOnClickListener(v -> {
            float current = securityManager.getMonthlySafetyBuffer();
            float updated = current + 500.0f;
            securityManager.setMonthlySafetyBuffer(updated);
            tvBufferValue.setText(String.format(Locale.getDefault(), "₹%,.2f", updated));
        });

        // Risk Appetite chips
        chipGroupRisk.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_risk_conservative)) {
                securityManager.setRiskLevel("conservative");
            } else if (checkedIds.contains(R.id.chip_risk_aggressive)) {
                securityManager.setRiskLevel("aggressive");
            } else {
                securityManager.setRiskLevel("moderate");
            }
        });

        // Dark Theme Switch
        switchThemeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                String newMode = isChecked ? "dark" : "light";
                securityManager.setThemeMode(newMode);
                AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        });

        // Biometric Switch
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                securityManager.setBiometricEnabled(isChecked);
                Toast.makeText(requireContext(), 
                    isChecked ? "Biometric App Lock Enabled" : "Biometric App Lock Disabled", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Language Picker
        btnProfileLang.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLanguagePickerDialog();
            }
        });

        // Logout Action
        btnLogoutAction.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Retake Onboarding / Financial Setup
        View btnRetake = view.findViewById(R.id.btn_retake_onboarding);
        if (btnRetake != null) {
            btnRetake.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    MainActivity main = (MainActivity) getActivity();
                    main.setBottomNavVisibility(false);
                    main.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new com.dfund.app.ui.onboarding.LanguageSelectionFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
                }
            });
        }
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.btn_edit_name);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(securityManager.getUserName());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                securityManager.setUserName(newName);
                tvProfileName.setText(newName);
                Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_msg)
            .setIcon(R.drawable.ic_logout)
            .setPositiveButton(R.string.logout_confirm, (dialog, which) -> performLogout())
            .setNegativeButton(R.string.logout_cancel, (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void performLogout() {
        // Clear secure session state
        securityManager.logout();

        Toast.makeText(requireContext(), R.string.logout_toast, Toast.LENGTH_LONG).show();

        // Navigate back to Splash / Onboarding flow
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            main.setBottomNavVisibility(false);
            main.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new com.dfund.app.ui.onboarding.LanguageSelectionFragment())
                .commitAllowingStateLoss();
        }
    }
}
