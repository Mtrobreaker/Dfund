package com.dfund.app.data.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.dfund.app.DFundApplication;
import com.dfund.app.data.local.TransactionEntity;
import java.util.concurrent.Executors;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "DFund.SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");

        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                sms = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (sms != null) {
                String sender = sms.getOriginatingAddress();
                String messageBody = sms.getMessageBody();
                long timestamp = sms.getTimestampMillis();

                TransactionEntity entity = UpiSmsParser.parse(sender, messageBody, timestamp);
                if (entity != null) {
                    Log.d(TAG, "Parsed UPI SMS from " + sender + ": ₹" + entity.getAmount() + " (" + entity.getType() + ")");
                    Executors.newSingleThreadExecutor().execute(() -> {
                        DFundApplication.getInstance().getDatabase().transactionDao().insert(entity);
                    });
                }
            }
        }
    }
}
