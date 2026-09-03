package com.dfund.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.ui.viewmodels.MainViewModel;
import com.dfund.app.ui.voice.VoiceActiveDialogFragment;
import com.dfund.app.ui.voice.VoiceInteractionManager;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private MainViewModel viewModel;
    private TextView tvTotalIncome;
    private TextView tvTotalSpent;
    private TextView tvSafetyShieldAmount;
    private TextView tvUpcomingEmiAmount;
    private TextView tvUpcomingEmiDesc;
    private TextView tvGrowthPotAmount;
    private TextView tvSurplusHint;
    private TextView tvAiSuggestion;
    private double currentSurplus = 0.0;
    private VoiceInteractionManager voiceManager;
    private String currentAiAdviceText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvSafetyShieldAmount = view.findViewById(R.id.tv_safety_shield_amount);
        tvUpcomingEmiAmount = view.findViewById(R.id.tv_upcoming_emi_amount);
        tvUpcomingEmiDesc = view.findViewById(R.id.tv_upcoming_emi_desc);
        tvGrowthPotAmount = view.findViewById(R.id.tv_growth_pot_amount);
        tvSurplusHint = view.findViewById(R.id.tv_surplus_hint);
        tvAiSuggestion = view.findViewById(R.id.tv_dashboard_ai_suggestion);

        if (getContext() != null) {
            voiceManager = new VoiceInteractionManager(getContext());
        }

        view.findViewById(R.id.btn_dashboard_listen_ai).setOnClickListener(v -> {
            if (voiceManager != null && !currentAiAdviceText.isEmpty()) {
                voiceManager.speak(currentAiAdviceText);
                Toast.makeText(getContext(), "🔊 Speaking financial advice...", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_dashboard_ask_ai).setOnClickListener(v -> {
            if (getActivity() != null) {
                VoiceActiveDialogFragment dialog = VoiceActiveDialogFragment.newInstance();
                dialog.setVoiceInteractionManager(voiceManager);
                dialog.show(getActivity().getSupportFragmentManager(), "VOICE_DIALOG");
            }
        });

        view.findViewById(R.id.btn_explore_growth).setOnClickListener(v -> {
            double initialAmt = currentSurplus > 0 ? Math.min(currentSurplus, 2000.0) : 500.0;
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, SipCalculatorFragment.newInstance(initialAmt, 3))
                    .addToBackStack(null)
                    .commit();
            }
        });

        view.findViewById(R.id.btn_scan_sms).setOnClickListener(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Scanning SMS messages...", Toast.LENGTH_SHORT).show();
                com.dfund.app.data.sms.SmsInboxScanner.scanInbox(getContext(), 100, count -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (count > 0) {
                                Toast.makeText(getContext(), "Found & imported " + count + " new UPI transactions!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "SMS up-to-date! No new transactions found.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });

        view.findViewById(R.id.btn_load_mock_data).setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.loadSampleData();
                Toast.makeText(getContext(), R.string.sample_data_loaded, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private double latestIncome = 0.0;
    private double latestExpenses = 0.0;
    private double latestEmi = 0.0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.autoInitializeDataIfEmpty();

        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            latestIncome = income != null ? income : 0.0;
            tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%.2f", latestIncome));
            recomputeBuckets();
        });

        viewModel.getTotalExpenses().observe(getViewLifecycleOwner(), spent -> {
            latestExpenses = spent != null ? spent : 0.0;
            tvTotalSpent.setText(String.format(Locale.getDefault(), "₹%.2f", latestExpenses));
            recomputeBuckets();
        });

        viewModel.getTotalEmi().observe(getViewLifecycleOwner(), emi -> {
            latestEmi = emi != null ? emi : 0.0;
            tvUpcomingEmiAmount.setText(String.format(Locale.getDefault(), "₹%.2f", latestEmi));
            if (latestEmi > 0) {
                tvUpcomingEmiDesc.setText(R.string.bucket_dues_desc);
            } else {
                tvUpcomingEmiDesc.setText(R.string.label_no_emis);
            }
            recomputeBuckets();
        });
    }

    private void recomputeBuckets() {
        double netBalance = Math.max(0.0, latestIncome - latestExpenses);
        
        // 1. Safety Shield: Emergency cushion for irregular income
        double safetyShield = netBalance > 0 ? Math.min(netBalance * 0.5, 3000.0) : 0.0;
        
        // 2. Growth Pot: Extra small surplus ready to grow
        currentSurplus = Math.max(0.0, netBalance - safetyShield);

        if (tvSafetyShieldAmount != null) {
            tvSafetyShieldAmount.setText(String.format(Locale.getDefault(), "₹%.2f", safetyShield));
        }
        if (tvGrowthPotAmount != null) {
            tvGrowthPotAmount.setText(String.format(Locale.getDefault(), "₹%.2f", currentSurplus));
        }

        if (tvSurplusHint != null) {
            if (currentSurplus > 0) {
                tvSurplusHint.setText(String.format(getString(R.string.surplus_detected_msg), String.format(Locale.getDefault(), "%.0f", currentSurplus)));
            } else {
                tvSurplusHint.setText(R.string.bucket_growth_desc);
            }
        }

        // 3. Update Ollama Minimax-M3 Advisor recommendation
        String lang = DFundApplication.getInstance() != null 
            ? DFundApplication.getInstance().getSecurityManager().getLanguage() 
            : "en";

        if ("ta".equals(lang)) {
            if (currentSurplus > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "உங்களிடம் ₹%.0f உபரி உள்ளது! மாதம் ₹500 மைக்ரோ-SIP-ல் முதலீடு செய்தால் 3 ஆண்டுகளில் ₹22,000க்கு மேல் வளர்ச்சி பெறலாம்.", 
                    currentSurplus);
            } else if (latestEmi > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "உங்கள் வரவிருக்கும் EMI தவணைகள் ₹%.0f. அபராதங்களைத் தவிர்க்க பாதுகாப்பு நிதியை தயாராக வையுங்கள்.", 
                    latestEmi);
            } else {
                currentAiAdviceText = "உங்கள் வருமானம் மற்றும் செலவுகள் சீராக கண்காணிக்கப்படுகின்றன. தொடர்ந்து சேமியுங்கள்!";
            }
        } else if ("te".equals(lang)) {
            if (currentSurplus > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "మీ వద్ద ₹%.0f మిగులు ఉంది! మైక్రో-SIP లో పెట్టుబడి పెట్టడం ద్వారా మంచి లాభం పొందవచ్చు.", 
                    currentSurplus);
            } else {
                currentAiAdviceText = "మీ ఖర్చులు క్రమబద్ధంగా ఉన్నాయి. పొదుపును కొనసాగించండి!";
            }
        } else if ("ml".equals(lang)) {
            if (currentSurplus > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "നിങ്ങൾക്ക് ₹%.0f മിച്ചമുണ്ട്! മൈക്രോ-SIP-ൽ നിക്ഷേപിക്കുന്നത് മികച്ച വളർച്ച നൽകും.", 
                    currentSurplus);
            } else {
                currentAiAdviceText = "നിങ്ങളുടെ ചെലവുകൾ കൃത്യമായി ട്രാക്ക് ചെയ്യുന്നു!";
            }
        } else {
            if (currentSurplus > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "You have ₹%.0f surplus! Investing ₹500/mo in a Micro-SIP can compound to over ₹22,000 in 3 years with 12%% returns.", 
                    currentSurplus);
            } else if (latestEmi > 0) {
                currentAiAdviceText = String.format(Locale.getDefault(), 
                    "You have ₹%.0f in upcoming EMIs. Keep your safety buffer ready to avoid late fees.", 
                    latestEmi);
            } else {
                currentAiAdviceText = "Your cashflow is balanced. Track your daily expenses and build your safety shield.";
            }
        }

        if (tvAiSuggestion != null) {
            tvAiSuggestion.setText(currentAiAdviceText);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}
