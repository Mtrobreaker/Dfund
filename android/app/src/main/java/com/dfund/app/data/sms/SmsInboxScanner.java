package com.dfund.app.data.sms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.dfund.app.data.local.AppDatabase;
import com.dfund.app.data.local.TransactionEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsInboxScanner {
    private static final String TAG = "DFund.SmsScanner";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ScanCallback {
        void onScanComplete(int transactionsImported);
    }

    public static void scanInbox(Context context, int maxMessages, ScanCallback callback) {
        executor.execute(() -> {
            int imported = 0;
            List<TransactionEntity> newTransactions = new ArrayList<>();

            try {
                ContentResolver contentResolver = context.getContentResolver();
                Uri inboxUri = Uri.parse("content://sms/inbox");
                
                String[] projection = new String[]{"_id", "address", "body", "date"};
                Cursor cursor = contentResolver.query(inboxUri, projection, null, null, "date DESC LIMIT " + maxMessages);

                if (cursor != null) {
                    AppDatabase db = AppDatabase.getInstance(context);
                    int addressIdx = cursor.getColumnIndex("address");
                    int bodyIdx = cursor.getColumnIndex("body");
                    int dateIdx = cursor.getColumnIndex("date");

                    while (cursor.moveToNext()) {
                        String address = addressIdx != -1 ? cursor.getString(addressIdx) : "";
                        String body = bodyIdx != -1 ? cursor.getString(bodyIdx) : "";
                        long date = dateIdx != -1 ? cursor.getLong(dateIdx) : System.currentTimeMillis();

                        if (body != null && !body.trim().isEmpty()) {
                            TransactionEntity tx = UpiSmsParser.parse(body, address, date);
                            if (tx != null) {
                                // Deduplication check
                                int existingCount = db.transactionDao().checkDuplicate(
                                    tx.getRawMessage(), 
                                    tx.getTimestamp(), 
                                    tx.getAmount()
                                );
                                if (existingCount == 0) {
                                    db.transactionDao().insertTransaction(tx);
                                    newTransactions.add(tx);
                                    imported++;
                                }
                            }
                        }
                    }
                    cursor.close();
                }
                Log.i(TAG, "Completed inbox scan. New transactions imported: " + imported);
            } catch (Exception e) {
                Log.e(TAG, "Error scanning SMS inbox", e);
            }

            final int finalImported = imported;
            if (callback != null) {
                callback.onScanComplete(finalImported);
            }
        });
    }
}
