package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;
import com.dfund.app.ui.DashboardFragment;
import com.dfund.app.ui.MainActivity;

public class SplashFragment extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());

        // Hide bottom nav while on splash
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavVisibility(false);
        }

        // Tap to skip or proceed after 1200ms
        view.setOnClickListener(v -> proceed());
        handler.postDelayed(this::proceed, 1200);
    }

    private void proceed() {
        if (!isAdded()) return;
        handler.removeCallbacksAndMessages(null);

        if (securityManager.isOnboardingCompleted()) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setBottomNavVisibility(true);
            }
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commitAllowingStateLoss();
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LanguageSelectionFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
