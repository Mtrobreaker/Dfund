package com.dfund.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.domain.FinancialMathEngine;
import com.dfund.app.ui.voice.VoiceActiveDialogFragment;
import com.dfund.app.ui.voice.VoiceInteractionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.text.NumberFormat;
import java.util.Locale;

public class SipCalculatorFragment extends Fragment {
    private static final String ARG_AMOUNT = "arg_amount";
    private static final String ARG_TENURE = "arg_tenure";

    private Slider sliderMonthlyAmount;
    private TextView tvMonthlyAmountVal;
    private TextView tvInvestedAmountVal;
    private TextView tvMaturityAmountVal;
    private ChipGroup chipGroupDuration;
    private VoiceInteractionManager voiceManager;

    private double monthlyAmount = 1500.0;
    private int tenureYears = 3;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static SipCalculatorFragment newInstance(double amount, int tenureYears) {
        SipCalculatorFragment fragment = new SipCalculatorFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_AMOUNT, amount);
        args.putInt(ARG_TENURE, tenureYears);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sip_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        voiceManager = new VoiceInteractionManager(requireContext());
        currencyFormatter.setMaximumFractionDigits(0);

        sliderMonthlyAmount = view.findViewById(R.id.slider_monthly_amount);
        tvMonthlyAmountVal = view.findViewById(R.id.tv_monthly_amount_val);
        tvInvestedAmountVal = view.findViewById(R.id.tv_invested_amount_val);
        tvMaturityAmountVal = view.findViewById(R.id.tv_maturity_amount_val);
        chipGroupDuration = view.findViewById(R.id.chip_group_duration);

        if (getArguments() != null) {
            monthlyAmount = getArguments().getDouble(ARG_AMOUNT, 1500.0);
            tenureYears = getArguments().getInt(ARG_TENURE, 3);
        }

        sliderMonthlyAmount.setValue((float) Math.min(Math.max(monthlyAmount, 500.0), 10000.0));

        sliderMonthlyAmount.addOnChangeListener((slider, value, fromUser) -> {
            monthlyAmount = value;
            recalculate();
        });

        chipGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_1yr) tenureYears = 1;
            else if (checkedId == R.id.chip_3yr) tenureYears = 3;
            else if (checkedId == R.id.chip_5yr) tenureYears = 5;
            else if (checkedId == R.id.chip_10yr) tenureYears = 10;
            recalculate();
        });

        // "Ask DFund" Suggestion Chips (Image 7)
        view.findViewById(R.id.chipAskSip).setOnClickListener(v -> openVoiceQuery("What is a Systematic Investment Plan (SIP) and how does it work?"));
        view.findViewById(R.id.chipAskAfford).setOnClickListener(v -> openVoiceQuery("Can I afford to invest 2000 rupees every month with my current budget?"));
        view.findViewById(R.id.chipAskGold).setOnClickListener(v -> openVoiceQuery("Is gold savings better or is a mutual fund SIP better for long-term growth?"));

        view.findViewById(R.id.btnAskDfundVoice).setOnClickListener(v -> {
            VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
            dialog.setVoiceInteractionManager(voiceManager);
            dialog.show(getParentFragmentManager(), "VOICE_DIALOG");
        });

        recalculate();
    }

    private void openVoiceQuery(String query) {
        VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
        dialog.setVoiceInteractionManager(voiceManager);
        dialog.show(getParentFragmentManager(), "VOICE_DIALOG");
    }

    private void recalculate() {
        if (tvMonthlyAmountVal == null) return;

        tvMonthlyAmountVal.setText(currencyFormatter.format(monthlyAmount));

        double totalInvested = monthlyAmount * tenureYears * 12;
        // Assume conservative equity index return of 12% p.a.
        double estimatedMaturity = FinancialMathEngine.calculateSipMaturity(monthlyAmount, tenureYears, 12.0);

        tvInvestedAmountVal.setText(currencyFormatter.format(totalInvested));
        tvMaturityAmountVal.setText(currencyFormatter.format(estimatedMaturity));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
