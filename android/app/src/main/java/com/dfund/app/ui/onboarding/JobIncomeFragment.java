package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

public class JobIncomeFragment extends Fragment {

    private String selectedJob = "Salaried Job";
    private String selectedFrequency = "Monthly";

    private MaterialCardView cardSalaried, cardSelf, cardDaily, cardGig, cardHome, cardOther;
    private TextView txtIncomePrompt;
    private EditText etIncome;
    private SecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_work, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        cardSalaried = view.findViewById(R.id.cardWorkSalaried);
        cardSelf = view.findViewById(R.id.cardWorkSelf);
        cardDaily = view.findViewById(R.id.cardWorkDaily);
        cardGig = view.findViewById(R.id.cardWorkGig);
        cardHome = view.findViewById(R.id.cardWorkHome);
        cardOther = view.findViewById(R.id.cardWorkOther);

        txtIncomePrompt = view.findViewById(R.id.txtIncomePrompt);
        etIncome = view.findViewById(R.id.etIncome);
        ChipGroup chipGroupFrequency = view.findViewById(R.id.chipGroupFrequency);

        // Preload if available
        String savedJob = securityManager.getJobType();
        if (savedJob != null && !savedJob.isEmpty()) selectedJob = savedJob;
        String savedFreq = securityManager.getIncomeFrequency();
        if (savedFreq != null && !savedFreq.isEmpty()) selectedFrequency = savedFreq;
        double savedInc = securityManager.getTypicalIncome();
        if (savedInc > 0) etIncome.setText(String.valueOf((long) savedInc));

        updateJobCardSelection();

        cardSalaried.setOnClickListener(v -> { selectedJob = "Salaried Job"; updateJobCardSelection(); });
        cardSelf.setOnClickListener(v -> { selectedJob = "Self-employed"; updateJobCardSelection(); });
        cardDaily.setOnClickListener(v -> { selectedJob = "Daily-wage Worker"; updateJobCardSelection(); });
        cardGig.setOnClickListener(v -> { selectedJob = "Gig / Part-time"; updateJobCardSelection(); });
        cardHome.setOnClickListener(v -> { selectedJob = "Homemaker"; updateJobCardSelection(); });
        cardOther.setOnClickListener(v -> { selectedJob = "Other"; updateJobCardSelection(); });

        chipGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipMonthly) {
                selectedFrequency = "Monthly";
                txtIncomePrompt.setText(R.string.typical_monthly_income);
                etIncome.setHint("25,000");
            } else if (checkedId == R.id.chipWeekly) {
                selectedFrequency = "Weekly";
                txtIncomePrompt.setText(R.string.typical_weekly_income);
                etIncome.setHint("6,000");
            } else if (checkedId == R.id.chipDaily) {
                selectedFrequency = "Daily";
                txtIncomePrompt.setText(R.string.typical_daily_income);
                etIncome.setHint("900");
            } else if (checkedId == R.id.chipVaries) {
                selectedFrequency = "It varies";
                txtIncomePrompt.setText(R.string.typical_varies_income);
                etIncome.setHint("20,000");
            }
        });

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String incStr = etIncome.getText().toString().trim();
            double income = 25000; // sensible default
            if (!TextUtils.isEmpty(incStr)) {
                try {
                    income = Double.parseDouble(incStr);
                } catch (NumberFormatException ignored) {}
            }

            securityManager.setJobType(selectedJob);
            securityManager.setIncomeFrequency(selectedFrequency);
            securityManager.setTypicalIncome(income);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MandatoryExpensesFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    private void updateJobCardSelection() {
        int selectedStroke = ContextCompat.getColor(requireContext(), R.color.primary_dark_green);
        int unselectedStroke = ContextCompat.getColor(requireContext(), R.color.border_light_green);

        applyCardState(cardSalaried, "Salaried Job".equals(selectedJob), selectedStroke, unselectedStroke);
        applyCardState(cardSelf, "Self-employed".equals(selectedJob), selectedStroke, unselectedStroke);
        applyCardState(cardDaily, "Daily-wage Worker".equals(selectedJob), selectedStroke, unselectedStroke);
        applyCardState(cardGig, "Gig / Part-time".equals(selectedJob), selectedStroke, unselectedStroke);
        applyCardState(cardHome, "Homemaker".equals(selectedJob), selectedStroke, unselectedStroke);
        applyCardState(cardOther, "Other".equals(selectedJob), selectedStroke, unselectedStroke);
    }

    private void applyCardState(MaterialCardView card, boolean isSelected, int selectedStroke, int unselectedStroke) {
        if (isSelected) {
            card.setStrokeColor(selectedStroke);
            card.setStrokeWidth(dpToPx(2));
        } else {
            card.setStrokeColor(unselectedStroke);
            card.setStrokeWidth(dpToPx(1));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
