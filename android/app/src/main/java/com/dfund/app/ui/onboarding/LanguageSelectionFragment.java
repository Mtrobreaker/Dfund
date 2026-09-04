package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class LanguageSelectionFragment extends Fragment {

    private String selectedLanguage = "en";
    private MaterialCardView cardLangEn, cardLangHi, cardLangTa, cardLangTe;
    private SwitchMaterial switchVoiceNav;
    private SecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        cardLangEn = view.findViewById(R.id.cardLangEn);
        cardLangHi = view.findViewById(R.id.cardLangHi);
        cardLangTa = view.findViewById(R.id.cardLangTa);
        cardLangTe = view.findViewById(R.id.cardLangTe);
        switchVoiceNav = view.findViewById(R.id.switchVoiceNav);

        // Preload existing preference
        selectedLanguage = securityManager.getLanguage();
        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            selectedLanguage = "en";
        }
        switchVoiceNav.setChecked(securityManager.isVoiceNavEnabled());
        updateCardSelection();

        cardLangEn.setOnClickListener(v -> {
            selectedLanguage = "en";
            updateCardSelection();
        });

        cardLangHi.setOnClickListener(v -> {
            selectedLanguage = "hi";
            updateCardSelection();
        });

        cardLangTa.setOnClickListener(v -> {
            selectedLanguage = "ta";
            updateCardSelection();
        });

        cardLangTe.setOnClickListener(v -> {
            selectedLanguage = "te";
            updateCardSelection();
        });

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            securityManager.setLanguage(selectedLanguage);
            securityManager.setVoiceNavEnabled(switchVoiceNav.isChecked());

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AuthPhonePanFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    private void updateCardSelection() {
        int selectedStrokeColor = ContextCompat.getColor(requireContext(), R.color.primary_dark_green);
        int unselectedStrokeColor = ContextCompat.getColor(requireContext(), R.color.border_light_green);

        applyCardState(cardLangEn, "en".equals(selectedLanguage), selectedStrokeColor, unselectedStrokeColor);
        applyCardState(cardLangHi, "hi".equals(selectedLanguage), selectedStrokeColor, unselectedStrokeColor);
        applyCardState(cardLangTa, "ta".equals(selectedLanguage), selectedStrokeColor, unselectedStrokeColor);
        applyCardState(cardLangTe, "te".equals(selectedLanguage), selectedStrokeColor, unselectedStrokeColor);
    }

    private void applyCardState(MaterialCardView card, boolean isSelected, int selectedStrokeColor, int unselectedStrokeColor) {
        if (isSelected) {
            card.setStrokeColor(selectedStrokeColor);
            card.setStrokeWidth(dpToPx(2));
        } else {
            card.setStrokeColor(unselectedStrokeColor);
            card.setStrokeWidth(dpToPx(1));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
