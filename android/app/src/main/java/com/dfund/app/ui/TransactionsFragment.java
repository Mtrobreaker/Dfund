package com.dfund.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dfund.app.R;
import com.dfund.app.ui.adapters.TransactionAdapter;
import com.dfund.app.ui.viewmodels.MainViewModel;
import com.google.android.material.chip.ChipGroup;

public class TransactionsFragment extends Fragment {
    private static final String ARG_INITIAL_CATEGORY = "arg_initial_category";

    private MainViewModel viewModel;
    private TransactionAdapter adapter;
    private TextView tvEmptyState;
    private ChipGroup chipGroup;
    private String initialCategory = null;

    public static TransactionsFragment newInstance(String category) {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);

        tvEmptyState = view.findViewById(R.id.tv_empty_transactions);
        chipGroup = view.findViewById(R.id.chip_group_categories);

        if (getArguments() != null) {
            initialCategory = getArguments().getString(ARG_INITIAL_CATEGORY);
        }

        setupCategoryChips(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setItems(transactions);
                tvEmptyState.setVisibility(View.GONE);
            } else {
                adapter.setItems(null);
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });

        if (initialCategory != null && !initialCategory.isEmpty()) {
            selectCategoryChip(initialCategory);
            viewModel.setCategoryFilter(initialCategory);
        } else {
            viewModel.setCategoryFilter("ALL");
        }
    }

    private void setupCategoryChips(View view) {
        view.findViewById(R.id.chip_cat_all).setOnClickListener(v -> viewModel.setCategoryFilter("ALL"));
        view.findViewById(R.id.chip_cat_emi).setOnClickListener(v -> viewModel.setCategoryFilter("EMI"));
        view.findViewById(R.id.chip_cat_food).setOnClickListener(v -> viewModel.setCategoryFilter("FOOD"));
        view.findViewById(R.id.chip_cat_grocery).setOnClickListener(v -> viewModel.setCategoryFilter("GROCERY"));
        view.findViewById(R.id.chip_cat_fuel).setOnClickListener(v -> viewModel.setCategoryFilter("FUEL"));
        view.findViewById(R.id.chip_cat_bills).setOnClickListener(v -> viewModel.setCategoryFilter("BILL"));
        view.findViewById(R.id.chip_cat_surplus).setOnClickListener(v -> viewModel.setCategoryFilter("SURPLUS"));
    }

    private void selectCategoryChip(String cat) {
        if ("EMI".equalsIgnoreCase(cat)) {
            chipGroup.check(R.id.chip_cat_emi);
        } else if ("FOOD".equalsIgnoreCase(cat)) {
            chipGroup.check(R.id.chip_cat_food);
        } else if ("GROCERY".equalsIgnoreCase(cat)) {
            chipGroup.check(R.id.chip_cat_grocery);
        } else if ("FUEL".equalsIgnoreCase(cat)) {
            chipGroup.check(R.id.chip_cat_fuel);
        } else if ("BILL".equalsIgnoreCase(cat)) {
            chipGroup.check(R.id.chip_cat_bills);
        } else {
            chipGroup.check(R.id.chip_cat_all);
        }
    }
}
