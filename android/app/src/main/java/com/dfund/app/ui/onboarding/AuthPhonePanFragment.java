package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;

public class AuthPhonePanFragment extends Fragment {

    private EditText etPhone;
    private EditText etPan;
    private SecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        etPhone = view.findViewById(R.id.etPhone);
        etPan = view.findViewById(R.id.etPan);

        String existingPhone = securityManager.getMobileNumber();
        if (existingPhone != null && !existingPhone.isEmpty()) {
            etPhone.setText(existingPhone);
        }
        String existingPan = securityManager.getPanNumber();
        if (existingPan != null && !existingPan.isEmpty()) {
            etPan.setText(existingPan);
        }

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String pan = etPan.getText().toString().trim().toUpperCase();

            if (!TextUtils.isEmpty(phone) && phone.length() < 10) {
                Toast.makeText(requireContext(), "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save details
            if (TextUtils.isEmpty(phone)) {
                phone = "9876543210"; // default demo user
            }
            securityManager.setMobileNumber(phone);
            securityManager.setPanNumber(pan);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SmsPermissionFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }
}
