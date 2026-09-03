package com.dfund.app.data.mock;

import com.dfund.app.data.local.TransactionEntity;
import java.util.ArrayList;
import java.util.List;

public class MockTransactionGenerator {

    public static void seedMockData(android.content.Context context) {
        if (context == null) return;
        com.dfund.app.data.local.AppDatabase db = com.dfund.app.data.local.AppDatabase.getInstance(context);
        db.transactionDao().insertAll(generateSampleTransactions());
    }

    public static List<TransactionEntity> generateSampleTransactions() {
        List<TransactionEntity> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        long day = 24 * 60 * 60 * 1000L;

        // 1. Irregular Gig Earnings / Incomes
        list.add(new TransactionEntity(
            "State Bank of India", 3200.0, "CREDIT", "SALARY",
            "swiggy.partner@sbi", "SBI90234812",
            "A/C *1234 Credited by INR 3,200.00 on 01-Sep. UPI: swiggy.partner@sbi. Ref: SBI90234812",
            false, false, now - (day * 1)
        ));

        list.add(new TransactionEntity(
            "State Bank of India", 2850.0, "CREDIT", "SALARY",
            "zomato.rider@icici", "ZOM87236109",
            "Your A/C *1234 is credited with Rs 2,850.00 via UPI from zomato.rider@icici Ref: ZOM87236109",
            false, false, now - (day * 4)
        ));

        // 2. Extra Surplus money (windfall/gift/extra tip)
        list.add(new TransactionEntity(
            "HDFC Bank", 1500.0, "CREDIT", "SURPLUS",
            "family.support@hdfcbank", "HDF76129845",
            "Rs 1,500.00 credited to HDFC A/C *5678 on 02-Sep by UPI. Extra surplus available.",
            false, false, now - (day * 2)
        ));

        // 3. Regular Expenses & Bills
        list.add(new TransactionEntity(
            "State Bank of India", 350.0, "DEBIT", "FUEL",
            "hpcl.bunk@icici", "HP76541289",
            "Rs 350.00 debited from A/C *1234 to HPCL PETROL PUMP on 02-Sep via UPI.",
            false, false, now - (day * 2)
        ));

        list.add(new TransactionEntity(
            "State Bank of India", 180.0, "DEBIT", "FOOD",
            "saravana.hotel@okaxis", "AXS12987345",
            "Paid Rs 180.00 from A/C *1234 to SARAVANA HOTEL UPI: saravana.hotel@okaxis",
            false, false, now - (day * 3)
        ));

        list.add(new TransactionEntity(
            "State Bank of India", 520.0, "DEBIT", "GROCERY",
            "zepto.groceries@hdfc", "ZEP98124567",
            "Rs 520.00 debited for Zepto groceries order Ref: ZEP98124567",
            false, false, now - (day * 5)
        ));

        list.add(new TransactionEntity(
            "State Bank of India", 499.0, "DEBIT", "BILL",
            "jio.prepaid@axis", "JIO34561289",
            "Rs 499.00 debited towards Jio Prepaid Mobile Recharge.",
            false, false, now - (day * 6)
        ));

        // 4. EMI / Loan Repayments
        list.add(new TransactionEntity(
            "State Bank of India", 1250.0, "DEBIT", "EMI",
            "bajaj.finance@hdfc", "BAJ65129843",
            "Rs 1,250.00 debited from A/C *1234 towards Two-Wheeler Loan EMI due for Sep-2026. Ref: BAJ65129843",
            true, true, now - (day * 8)
        ));

        list.add(new TransactionEntity(
            "HDFC Bank", 800.0, "DEBIT", "EMI",
            "tvscs.mandate@axis", "TVS98123456",
            "NACH auto-debit of Rs 800.00 for Phone EMI mandate TVS98123456",
            true, true, now - (day * 12)
        ));

        return list;
    }
}
