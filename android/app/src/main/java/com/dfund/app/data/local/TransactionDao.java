package com.dfund.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionEntity transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransaction(TransactionEntity transaction);

    @Query("SELECT COUNT(*) FROM transactions")
    int getTransactionCount();

    @Query("SELECT COUNT(*) FROM transactions WHERE rawMessage = :rawMessage OR (timestamp = :timestamp AND amount = :amount)")
    int checkDuplicate(String rawMessage, long timestamp, double amount);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TransactionEntity> transactions);

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    LiveData<List<TransactionEntity>> getAllTransactionsLive();

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    List<TransactionEntity> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<TransactionEntity>> getTransactionsByCategory(String category);

    @Query("SELECT * FROM transactions WHERE isEmi = 1 OR category = 'EMI' ORDER BY timestamp DESC")
    LiveData<List<TransactionEntity>> getEmiTransactions();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT'")
    LiveData<Double> getTotalExpenses();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND (isEmi = 1 OR category = 'EMI')")
    LiveData<Double> getTotalEmi();

    @Query("DELETE FROM transactions")
    void deleteAll();
}
