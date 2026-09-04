package com.dfund.app.ui.onboarding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;

public class SmsPermissionFragment extends Fragment {

    private SecurityManager securityManager;

    private final ActivityResultLauncher<String[]> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_SMS));
                securityManager.setSmsAccessGranted(granted);
                navigateToWork();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_sms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        view.findViewById(R.id.btnAllowSms).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                securityManager.setSmsAccessGranted(true);
                navigateToWork();
            } else {
                smsPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS
                });
            }
        });

        view.findViewById(R.id.btnManualEntry).setOnClickListener(v -> {
            securityManager.setSmsAccessGranted(false);
            navigateToWork();
        });
    }

    private void navigateToWork() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new JobIncomeFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
