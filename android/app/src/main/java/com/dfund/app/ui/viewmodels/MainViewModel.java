package com.dfund.app.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.dfund.app.DFundApplication;
import com.dfund.app.data.local.AppDatabase;
import com.dfund.app.data.local.TransactionDao;
import com.dfund.app.data.local.TransactionEntity;
import com.dfund.app.data.mock.MockTransactionGenerator;
import com.dfund.app.domain.FinancialMathEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final TransactionDao transactionDao;
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>(null);
    private final LiveData<List<TransactionEntity>> filteredTransactions;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = ((DFundApplication) application).getDatabase();
        transactionDao = db.transactionDao();

        filteredTransactions = Transformations.switchMap(categoryFilter, category -> {
            if (category == null || category.isEmpty() || "ALL".equalsIgnoreCase(category)) {
                return transactionDao.getAllTransactionsLive();
            } else if ("EMI".equalsIgnoreCase(category)) {
                return transactionDao.getEmiTransactions();
            } else {
                return transactionDao.getTransactionsByCategory(category);
            }
        });
    }

    public LiveData<List<TransactionEntity>> getTransactions() {
        return filteredTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpenses() {
        return transactionDao.getTotalExpenses();
    }

    public LiveData<Double> getTotalEmi() {
        return transactionDao.getTotalEmi();
    }

    public void setCategoryFilter(String category) {
        categoryFilter.setValue(category);
    }

    public void loadSampleData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<TransactionEntity> samples = MockTransactionGenerator.generateSampleTransactions();
            transactionDao.insertAll(samples);
        });
    }

    public void autoInitializeDataIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = transactionDao.getTransactionCount();
            if (count == 0) {
                // Try scanning SMS inbox first if permission granted
                if (androidx.core.content.ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.READ_SMS) 
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    com.dfund.app.data.sms.SmsInboxScanner.scanInbox(getApplication(), 100, imported -> {
                        if (imported == 0) {
                            loadSampleData();
                        }
                    });
                } else {
                    loadSampleData();
                }
            }
        });
    }

    public void insertTransaction(TransactionEntity tx) {
        Executors.newSingleThreadExecutor().execute(() -> {
            transactionDao.insert(tx);
        });
    }

    public void clearAllTransactions() {
        Executors.newSingleThreadExecutor().execute(transactionDao::deleteAll);
    }
}
