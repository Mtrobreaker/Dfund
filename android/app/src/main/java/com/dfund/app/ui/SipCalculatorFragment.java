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
import com.google.android.material.slider.Slider;
import java.util.Locale;

public class SipCalculatorFragment extends Fragment {
    private static final String ARG_AMOUNT = "arg_amount";
    private static final String ARG_TENURE = "arg_tenure";

    private Slider sliderMonthlyAmount;
    private Slider sliderTenure;
    private TextView tvMonthlyAmountVal;
    private TextView tvTenureYearsVal;
    private TextView tvTotalMaturityValue;
    private TextView tvTotalInvested;
    private TextView tvNetGain;

    private TextView tvCompareBankVal;
    private TextView tvCompareRdVal;
    private TextView tvCompareSipVal;
    private TextView tvCompareGoldVal;
    private TextView tvComparisonAdvice;

    private double monthlyAmount = 500.0;
    private int tenureYears = 3;

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
        View view = inflater.inflate(R.layout.fragment_sip_calculator, container, false);

        sliderMonthlyAmount = view.findViewById(R.id.slider_monthly_amount);
        sliderTenure = view.findViewById(R.id.slider_tenure);
        tvMonthlyAmountVal = view.findViewById(R.id.tv_monthly_amount_val);
        tvTenureYearsVal = view.findViewById(R.id.tv_tenure_years_val);
        tvTotalMaturityValue = view.findViewById(R.id.tv_total_maturity_value);
        tvTotalInvested = view.findViewById(R.id.tv_total_invested);
        tvNetGain = view.findViewById(R.id.tv_net_gain);

        tvCompareBankVal = view.findViewById(R.id.tv_compare_bank_val);
        tvCompareRdVal = view.findViewById(R.id.tv_compare_rd_val);
        tvCompareSipVal = view.findViewById(R.id.tv_compare_sip_val);
        tvCompareGoldVal = view.findViewById(R.id.tv_compare_gold_val);
        tvComparisonAdvice = view.findViewById(R.id.tv_comparison_advice);

        if (getArguments() != null) {
            monthlyAmount = getArguments().getDouble(ARG_AMOUNT, 500.0);
            tenureYears = getArguments().getInt(ARG_TENURE, 3);
        }

        sliderMonthlyAmount.setValue((float) Math.min(Math.max(monthlyAmount, 100.0), 10000.0));
        sliderTenure.setValue((float) Math.min(Math.max(tenureYears, 1), 15));

        setupListeners(view);
        recalculate();

        return view;
    }

    private void setupListeners(View view) {
        sliderMonthlyAmount.addOnChangeListener((slider, value, fromUser) -> {
            monthlyAmount = value;
            recalculate();
        });

        sliderTenure.addOnChangeListener((slider, value, fromUser) -> {
            tenureYears = (int) value;
            recalculate();
        });

        // Quick Preset Chips
        view.findViewById(R.id.chip_amt_200).setOnClickListener(v -> setAmountPreset(200.0));
        view.findViewById(R.id.chip_amt_500).setOnClickListener(v -> setAmountPreset(500.0));
        view.findViewById(R.id.chip_amt_1000).setOnClickListener(v -> setAmountPreset(1000.0));
        view.findViewById(R.id.chip_amt_2000).setOnClickListener(v -> setAmountPreset(2000.0));
    }

    private void setAmountPreset(double amt) {
        monthlyAmount = amt;
        sliderMonthlyAmount.setValue((float) amt);
        recalculate();
    }

    private void recalculate() {
        tvMonthlyAmountVal.setText(String.format(Locale.getDefault(), "₹%.0f / mo", monthlyAmount));
        tvTenureYearsVal.setText(String.format(Locale.getDefault(), "%d Years", tenureYears));

        // 1. Calculate Expected Micro-SIP Return (12.5% p.a.)
        FinancialMathEngine.SipResult sipResult = FinancialMathEngine.calculateSip(monthlyAmount, 12.5, tenureYears);
        tvTotalMaturityValue.setText(String.format(Locale.getDefault(), "₹%.2f", sipResult.totalMaturityValue));
        tvTotalInvested.setText(String.format(Locale.getDefault(), "₹%.2f", sipResult.investedAmount));
        tvNetGain.setText(String.format(Locale.getDefault(), "+ ₹%.2f", sipResult.estimatedReturns));

        // 2. 4-Way Comparison for the small surplus:
        FinancialMathEngine.SurplusComparison comp = FinancialMathEngine.compareSurplus(monthlyAmount * 12 * tenureYears, tenureYears);
        // Compare annualized options:
        double totalPrincipal = monthlyAmount * 12 * tenureYears;
        double idleBank = totalPrincipal * Math.pow(1.0 + 0.03, tenureYears);
        double rd = totalPrincipal * Math.pow(1.0 + 0.07, tenureYears);
        double gold = totalPrincipal * Math.pow(1.0 + 0.10, tenureYears);

        tvCompareBankVal.setText(String.format(Locale.getDefault(), "₹%.0f", idleBank));
        tvCompareRdVal.setText(String.format(Locale.getDefault(), "₹%.0f", rd));
        tvCompareSipVal.setText(String.format(Locale.getDefault(), "₹%.0f", sipResult.totalMaturityValue));
        tvCompareGoldVal.setText(String.format(Locale.getDefault(), "₹%.0f", gold));

        tvComparisonAdvice.setText(String.format(Locale.getDefault(),
            "💡 Notice: Keeping ₹%.0f in bank yields only ₹%.0f, while a disciplined Micro-SIP reaches approx ₹%.0f (+₹%.0f extra wealth)!",
            totalPrincipal, idleBank, sipResult.totalMaturityValue, (sipResult.totalMaturityValue - idleBank)));
    }
}
