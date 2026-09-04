package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.dfund.app.ui.DashboardFragment;
import com.dfund.app.ui.MainActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class PersonalizedPlanFragment extends Fragment {

    private SecurityManager securityManager;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());
        currencyFormatter.setMaximumFractionDigits(0);

        TextView txtPlanIncome = view.findViewById(R.id.txtPlanIncome);
        TextView txtPlanEssentials = view.findViewById(R.id.txtPlanEssentials);
        TextView txtPlanEmergency = view.findViewById(R.id.txtPlanEmergency);
        TextView txtPlanSpending = view.findViewById(R.id.txtPlanSpending);
        TextView txtPlanInvest = view.findViewById(R.id.txtPlanInvest);

        double income = securityManager.getTypicalIncome();
        if (income <= 0) income = 25000;

        double essentials = securityManager.getMandatoryExpensesTotal();
        if (essentials <= 0) essentials = 16800;

        double desiredSavings = securityManager.getDesiredSavings();
        if (desiredSavings <= 0) desiredSavings = 3000;

        double desiredInvest = securityManager.getDesiredInvestment();

        // Safe spending limit is remaining income
        double safeSpending = Math.max(1000, income - essentials - desiredSavings - desiredInvest);

        txtPlanIncome.setText(currencyFormatter.format(income));
        txtPlanEssentials.setText(currencyFormatter.format(essentials));
        txtPlanEmergency.setText(currencyFormatter.format(desiredSavings));
        txtPlanSpending.setText(currencyFormatter.format(safeSpending));
        txtPlanInvest.setText(currencyFormatter.format(desiredInvest));

        view.findViewById(R.id.btnGoDashboard).setOnClickListener(v -> {
            securityManager.setOnboardingCompleted(true);

            if (getActivity() instanceof MainActivity) {
                MainActivity main = (MainActivity) getActivity();
                main.getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                main.setBottomNavVisibility(true);
                main.switchFragment(new DashboardFragment(), "DASHBOARD");
            }
        });
    }
}
