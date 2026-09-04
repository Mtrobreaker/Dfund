package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

public class SavingsPreferencesFragment extends Fragment {

    private EditText etSavings, etInvest;
    private MaterialCardView cardInvestInput;
    private MaterialCheckBox cbNotReadyToInvest;
    private SecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_savings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        etSavings = view.findViewById(R.id.etSavings);
        etInvest = view.findViewById(R.id.etInvest);
        cardInvestInput = view.findViewById(R.id.cardInvestInput);
        cbNotReadyToInvest = view.findViewById(R.id.cbNotReadyToInvest);

        // Preload defaults
        etSavings.setText("5000");
        etInvest.setText("2000");

        view.findViewById(R.id.chipSave500).setOnClickListener(v -> etSavings.setText("500"));
        view.findViewById(R.id.chipSave1000).setOnClickListener(v -> etSavings.setText("1000"));
        view.findViewById(R.id.chipSave2000).setOnClickListener(v -> etSavings.setText("2000"));
        view.findViewById(R.id.chipSave5000).setOnClickListener(v -> etSavings.setText("5000"));

        cbNotReadyToInvest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etInvest.setText("0");
                etInvest.setEnabled(false);
                cardInvestInput.setAlpha(0.5f);
            } else {
                etInvest.setText("2000");
                etInvest.setEnabled(true);
                cardInvestInput.setAlpha(1.0f);
            }
        });

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            double savings = parseAmount(etSavings, 5000);
            double invest = cbNotReadyToInvest.isChecked() ? 0 : parseAmount(etInvest, 2000);

            securityManager.setDesiredSavings(savings);
            securityManager.setDesiredInvestment(invest);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PersonalizedPlanFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    private double parseAmount(EditText et, double fallback) {
        if (et == null) return fallback;
        String s = et.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return fallback;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
