package com.dfund.app.data.sms;

import com.dfund.app.data.local.TransactionEntity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpiSmsParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    );

    private static final Pattern VPA_PATTERN = Pattern.compile(
        "(?i)(?:to|from|vpa|at|info)\\s+([a-zA-Z0-9.\\-_]+@[a-zA-Z0-9]+)"
    );

    private static final Pattern UTR_PATTERN = Pattern.compile(
        "(?i)(?:ref|utr|txn|rrn|reference)\\s*(?:no\\.?)?\\s*[:\\-]?\\s*([0-9a-zA-Z]{8,22})"
    );

    private static final String[] DEBIT_KEYWORDS = {
        "debited", "paid", "sent", "transferred", "deducted", "dr", "withdrawn", "spent"
    };

    private static final String[] CREDIT_KEYWORDS = {
        "credited", "received", "added", "deposited", "cr", "refund", "cashback", "payout"
    };

    private static final String[] EMI_KEYWORDS = {
        "emi", "loan", "mandate", "autopay", "nach", "installment", "auto-debit", "repayment"
    };

    public static TransactionEntity parse(String sender, String message, long timestamp) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        String lower = message.toLowerCase();

        // 1. Determine Debit vs Credit
        String type = null;
        for (String kw : DEBIT_KEYWORDS) {
            if (lower.contains(kw)) {
                type = "DEBIT";
                break;
            }
        }
        for (String kw : CREDIT_KEYWORDS) {
            if (lower.contains(kw)) {
                type = "CREDIT";
                break;
            }
        }

        // If no financial indicator, ignore non-banking SMS
        if (type == null && !lower.contains("upi") && !lower.contains("bank")) {
            return null;
        }
        if (type == null) {
            type = "DEBIT"; // default if UPI found
        }

        // 2. Extract Amount
        double amount = 0.0;
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(message);
        if (amountMatcher.find()) {
            try {
                String amtStr = amountMatcher.group(1).replace(",", "");
                amount = Double.parseDouble(amtStr);
            } catch (NumberFormatException ignored) {}
        }

        if (amount <= 0.0) {
            return null; // Ignore non-monetary SMS
        }

        // 3. Extract VPA
        String vpa = null;
        Matcher vpaMatcher = VPA_PATTERN.matcher(message);
        if (vpaMatcher.find()) {
            vpa = vpaMatcher.group(1);
        }

        // 4. Extract UTR
        String utr = null;
        Matcher utrMatcher = UTR_PATTERN.matcher(message);
        if (utrMatcher.find()) {
            utr = utrMatcher.group(1);
        }

        // 5. Detect Bank from Sender or Body
        String bankName = extractBankName(sender, lower);

        // 6. Detect EMI or Recurring
        boolean isEmi = false;
        for (String emiKw : EMI_KEYWORDS) {
            if (lower.contains(emiKw)) {
                isEmi = true;
                break;
            }
        }

        // 7. Categorize
        String category = categorize(lower, vpa, isEmi, type);

        return new TransactionEntity(
            bankName,
            amount,
            type,
            category,
            vpa,
            utr,
            message,
            isEmi, // isRecurring flag
            isEmi,
            timestamp > 0 ? timestamp : System.currentTimeMillis()
        );
    }

    private static String extractBankName(String sender, String lowerMsg) {
        String s = (sender != null ? sender.toLowerCase() : "") + " " + lowerMsg;
        if (s.contains("hdfc")) return "HDFC Bank";
        if (s.contains("sbi") || s.contains("sbin")) return "State Bank of India";
        if (s.contains("icici")) return "ICICI Bank";
        if (s.contains("axis")) return "Axis Bank";
        if (s.contains("kotak")) return "Kotak Mahindra";
        if (s.contains("pnb")) return "Punjab National Bank";
        if (s.contains("bob") || s.contains("baroda")) return "Bank of Baroda";
        if (s.contains("paytm")) return "Paytm Payments Bank";
        if (s.contains("phonepe")) return "PhonePe UPI";
        if (s.contains("gpay")) return "Google Pay";
        return "Bank Account";
    }

    private static String categorize(String msg, String vpa, boolean isEmi, String type) {
        if (isEmi) return "EMI";
        if ("CREDIT".equals(type)) {
            if (msg.contains("salary") || msg.contains("payout")) return "SALARY";
            return "SURPLUS";
        }

        String searchScope = msg + " " + (vpa != null ? vpa.toLowerCase() : "");

        if (searchScope.contains("swiggy") || searchScope.contains("zomato") || 
            searchScope.contains("restaurant") || searchScope.contains("food") || 
            searchScope.contains("tea") || searchScope.contains("bhojanalaya")) {
            return "FOOD";
        }
        if (searchScope.contains("blinkit") || searchScope.contains("zepto") || 
            searchScope.contains("instamart") || searchScope.contains("grocer") || 
            searchScope.contains("supermarket") || searchScope.contains("provision")) {
            return "GROCERY";
        }
        if (searchScope.contains("petrol") || searchScope.contains("diesel") || 
            searchScope.contains("fuel") || searchScope.contains("hpcl") || 
            searchScope.contains("bpcl") || searchScope.contains("iocl")) {
            return "FUEL";
        }
        if (searchScope.contains("bill") || searchScope.contains("electricity") || 
            searchScope.contains("gas") || searchScope.contains("recharge") || 
            searchScope.contains("airtel") || searchScope.contains("jio") || 
            searchScope.contains("bescom") || searchScope.contains("tneb")) {
            return "BILL";
        }

        return "OTHER";
    }
}
