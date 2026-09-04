package com.dfund.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dfund.app.R;
import com.dfund.app.data.local.TransactionEntity;
import com.dfund.app.data.security.SecurityManager;
import com.dfund.app.ui.viewmodels.MainViewModel;
import com.dfund.app.ui.voice.VoiceActiveDialogFragment;
import com.dfund.app.ui.voice.VoiceInteractionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private MainViewModel viewModel;
    private SecurityManager securityManager;
    private VoiceInteractionManager voiceManager;

    private TextView tvTotalSpent, tvSpendingLimit, tvSpendingPercent;
    private TextView tvRemainingLimit, tvSavedMonth;
    private ProgressBar progressSpending;
    private TextView tvAiSuggestion;
    private LinearLayout layoutRecentTransactions;
    private String currentAiAdviceText = "";

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());
        voiceManager = new VoiceInteractionManager(requireContext());
        currencyFormatter.setMaximumFractionDigits(0);

        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvSpendingLimit = view.findViewById(R.id.tv_spending_limit);
        tvSpendingPercent = view.findViewById(R.id.tv_spending_percent);
        tvRemainingLimit = view.findViewById(R.id.tv_remaining_limit);
        tvSavedMonth = view.findViewById(R.id.tv_saved_month);
        progressSpending = view.findViewById(R.id.progressSpending);
        tvAiSuggestion = view.findViewById(R.id.tv_dashboard_ai_suggestion);
        layoutRecentTransactions = view.findViewById(R.id.layoutRecentTransactions);

        // Top Header Actions
        view.findViewById(R.id.btnAvatarProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProfile();
            }
        });

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                Toast.makeText(getContext(), "🔔 No urgent financial alerts. You're in safe limits!", Toast.LENGTH_SHORT).show()
        );

        // Quick Actions
        view.findViewById(R.id.btnQuickAddIncome).setOnClickListener(v -> showAddTransactionDialog(true));
        view.findViewById(R.id.btnQuickAddExpense).setOnClickListener(v -> showAddTransactionDialog(false));

        view.findViewById(R.id.btnQuickEmergency).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SipCalculatorFragment.newInstance(1000.0, 1))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });

        view.findViewById(R.id.btnQuickSaveInvest).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SipCalculatorFragment.newInstance(2000.0, 3))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });

        view.findViewById(R.id.cardPromoBanner).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SipCalculatorFragment.newInstance(1500.0, 3))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });

        view.findViewById(R.id.btnSeeAllTransactions).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, TransactionsFragment.newInstance(null))
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });

        // AI Advisor buttons
        view.findViewById(R.id.btn_dashboard_listen_ai).setOnClickListener(v -> {
            if (!currentAiAdviceText.isEmpty()) {
                voiceManager.speak(currentAiAdviceText);
                Toast.makeText(getContext(), "🔊 Speaking DFund advice...", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_dashboard_ask_ai).setOnClickListener(v -> {
            VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
            dialog.setVoiceInteractionManager(voiceManager);
            dialog.show(getParentFragmentManager(), "VOICE_DIALOG");
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.autoInitializeDataIfEmpty();

        observeFinancialData();
    }

    private void observeFinancialData() {
        double income = securityManager.getTypicalIncome();
        if (income <= 0) income = 25000;

        double limit = income * 0.85; // 85% maximum safe spending limit
        tvSpendingLimit.setText("of " + currencyFormatter.format(limit) + " limit");

        final double finalLimit = limit;
        final double finalIncome = income;

        viewModel.getTotalExpenses().observe(getViewLifecycleOwner(), expenses -> {
            double spent = expenses != null ? expenses : 16800.0;
            tvTotalSpent.setText(currencyFormatter.format(spent));

            int pct = (int) Math.min(100, Math.round((spent / finalLimit) * 100));
            progressSpending.setProgress(pct);
            tvSpendingPercent.setText(pct + "% used");

            double remaining = Math.max(0, finalLimit - spent);
            tvRemainingLimit.setText(currencyFormatter.format(remaining) + " left");

            double saved = Math.max(0, finalIncome - spent);
            tvSavedMonth.setText(currencyFormatter.format(saved));

            currentAiAdviceText = String.format(Locale.getDefault(),
                    "You have spent %s this month, which is %d percent of your monthly limit. You still have %s safe to spend.",
                    currencyFormatter.format(spent), pct, currencyFormatter.format(remaining));
            tvAiSuggestion.setText(currentAiAdviceText);
        });

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), this::renderRecentTransactions);
    }

    private void renderRecentTransactions(List<TransactionEntity> transactions) {
        if (layoutRecentTransactions == null) return;
        layoutRecentTransactions.removeAllViews();

        if (transactions == null || transactions.isEmpty()) {
            TextView emptyTv = new TextView(getContext());
            emptyTv.setText("No recent transactions found.");
            emptyTv.setTextColor(getResources().getColor(R.color.text_muted));
            layoutRecentTransactions.addView(emptyTv);
            return;
        }

        int count = Math.min(4, transactions.size());
        for (int i = 0; i < count; i++) {
            TransactionEntity tx = transactions.get(i);
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, layoutRecentTransactions, false);

            TextView tvCategory = itemView.findViewById(R.id.tv_category_tag);
            TextView tvBank = itemView.findViewById(R.id.tv_bank_name);
            TextView tvAmount = itemView.findViewById(R.id.tv_amount);
            TextView tvDate = itemView.findViewById(R.id.tv_date);
            TextView tvDetails = itemView.findViewById(R.id.tv_details);

            if (tvCategory != null) {
                String cat = tx.getCategory() != null ? tx.getCategory().toUpperCase() : "GENERAL";
                tvCategory.setText(cat);
            }
            if (tvBank != null) {
                tvBank.setText(tx.getDescription());
            }
            if (tvDetails != null) {
                tvDetails.setText(tx.getMerchant());
            }
            if (tvDate != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault());
                tvDate.setText(sdf.format(new java.util.Date(tx.getTimestamp() > 0 ? tx.getTimestamp() : System.currentTimeMillis())));
            }

            if (tvAmount != null) {
                boolean isCredit = tx.isCredit();
                tvAmount.setText((isCredit ? "+ " : "- ") + currencyFormatter.format(tx.getAmount()));
                tvAmount.setTextColor(getResources().getColor(isCredit ? R.color.growth_green : R.color.text_dark));
            }

            layoutRecentTransactions.addView(itemView);
        }
    }

    private void showAddTransactionDialog(boolean isIncome) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(isIncome ? "Add Income" : "Add Expense");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final android.widget.EditText etDesc = new android.widget.EditText(getContext());
        etDesc.setHint(isIncome ? "Income source (e.g. Salary, Client)" : "Expense name (e.g. Groceries)");
        layout.addView(etDesc);

        final android.widget.EditText etAmt = new android.widget.EditText(getContext());
        etAmt.setHint("Amount in ₹");
        etAmt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmt);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String desc = etDesc.getText().toString().trim();
            String amtStr = etAmt.getText().toString().trim();
            if (!amtStr.isEmpty()) {
                try {
                    double amt = Double.parseDouble(amtStr);
                    if (desc.isEmpty()) desc = isIncome ? "Direct Income" : "Quick Expense";
                    viewModel.addManualTransaction(desc, amt, isIncome ? "Income" : "Expense", isIncome);
                    Toast.makeText(getContext(), "Transaction recorded!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException ignored) {}
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
