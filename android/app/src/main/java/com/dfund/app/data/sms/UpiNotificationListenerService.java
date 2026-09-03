package com.dfund.app.data.sms;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.dfund.app.DFundApplication;
import com.dfund.app.data.local.TransactionEntity;
import java.util.concurrent.Executors;

public class UpiNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "DFund.NotificationSvc";

    private static final String[] UPI_PACKAGES = {
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                       // PhonePe
        "net.one97.paytm",                       // Paytm
        "in.org.npci.upiapp",                    // BHIM
        "com.whatsapp"                           // WhatsApp Pay
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;

        String packageName = sbn.getPackageName();
        boolean isUpiApp = false;
        for (String pkg : UPI_PACKAGES) {
            if (pkg.equalsIgnoreCase(packageName)) {
                isUpiApp = true;
                break;
            }
        }

        if (!isUpiApp) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        String fullMessage = (title != null ? title : "") + " " + 
                             (bigText != null ? bigText : (text != null ? text : ""));

        if (!fullMessage.trim().isEmpty()) {
            TransactionEntity entity = UpiSmsParser.parse(packageName, fullMessage, sbn.getPostTime());
            if (entity != null) {
                Log.d(TAG, "Captured UPI Notification from " + packageName + ": ₹" + entity.getAmount());
                Executors.newSingleThreadExecutor().execute(() -> {
                    DFundApplication.getInstance().getDatabase().transactionDao().insert(entity);
                });
            }
        }
    }
}
