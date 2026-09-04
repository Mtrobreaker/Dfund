package com.dfund.app.ui.onboarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dfund.app.R;
import com.dfund.app.data.security.SecurityManager;

import java.text.NumberFormat;
import java.util.Locale;

public class MandatoryExpensesFragment extends Fragment {

    private EditText etRent, etFood, etTransport, etUtilities, etLoan, etOther;
    private TextView txtTotalEssentials;
    private SecurityManager securityManager;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = new SecurityManager(requireContext());
        currencyFormatter.setMaximumFractionDigits(0);

        etRent = view.findViewById(R.id.etRent);
        etFood = view.findViewById(R.id.etFood);
        etTransport = view.findViewById(R.id.etTransport);
        etUtilities = view.findViewById(R.id.etUtilities);
        etLoan = view.findViewById(R.id.etLoan);
        etOther = view.findViewById(R.id.etOther);
        txtTotalEssentials = view.findViewById(R.id.txtTotalEssentials);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etRent.addTextChangedListener(watcher);
        etFood.addTextChangedListener(watcher);
        etTransport.addTextChangedListener(watcher);
        etUtilities.addTextChangedListener(watcher);
        etLoan.addTextChangedListener(watcher);
        etOther.addTextChangedListener(watcher);

        calculateTotal();

        view.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            double total = calculateTotal();
            securityManager.setMandatoryExpensesTotal(total);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SavingsPreferencesFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    private double calculateTotal() {
        double rent = parseAmount(etRent, 7000);
        double food = parseAmount(etFood, 5000);
        double transport = parseAmount(etTransport, 2000);
        double utilities = parseAmount(etUtilities, 1500);
        double loan = parseAmount(etLoan, 0);
        double other = parseAmount(etOther, 1300);

        double total = rent + food + transport + utilities + loan + other;
        if (txtTotalEssentials != null) {
            txtTotalEssentials.setText(currencyFormatter.format(total) + " / month");
        }
        return total;
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
