package com.dfund.test;

import com.dfund.app.domain.FinancialMathEngine;
import com.dfund.app.data.sms.UpiSmsParser;
import com.dfund.app.data.local.TransactionEntity;
import java.util.Arrays;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  RUNNING DFUND CORE ENGINE VERIFICATION TESTS");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: SIP Calculation
        try {
            FinancialMathEngine.SipResult sip = FinancialMathEngine.calculateSip(500.0, 12.0, 3);
            if (sip.investedAmount == 18000.0 && sip.totalMaturityValue > 21000.0 && sip.estimatedReturns > 3000.0) {
                System.out.println("✅ [PASS] SIP Calculation Test (₹500/mo, 12%, 3 yrs -> Invested: ₹" + sip.investedAmount + ", Value: ₹" + sip.totalMaturityValue + ")");
                passed++;
            } else {
                System.err.println("❌ [FAIL] SIP Calculation Test: Unexpected values");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] SIP Calculation Test Exception: " + e);
            failed++;
        }

        // Test 2: Small Surplus 3-Way Comparator
        try {
            FinancialMathEngine.SurplusComparison comp = FinancialMathEngine.compareSurplus(1000.0, 3);
            if (comp.microSipValue > comp.idleBankValue && comp.microSipValue > comp.recurringDepositValue) {
                System.out.println("✅ [PASS] Small Surplus 3-Way Comparison Test (Idle Bank: ₹" + comp.idleBankValue + " vs SIP: ₹" + comp.microSipValue + ")");
                passed++;
            } else {
                System.err.println("❌ [FAIL] Small Surplus Comparison Test");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] Surplus Comparison Test Exception: " + e);
            failed++;
        }

        // Test 3: Irregular Income Volatility Buffer
        try {
            List<Double> pastIncomes = Arrays.asList(8000.0, 15000.0, 9000.0, 18000.0);
            double target = FinancialMathEngine.calculateSafetyShieldTarget(pastIncomes, 10000.0);
            if (target > 15000.0) {
                System.out.println("✅ [PASS] Irregular Income Volatility Buffer Test (Target Safety Shield: ₹" + target + ")");
                passed++;
            } else {
                System.err.println("❌ [FAIL] Volatility Buffer Test: Target ₹" + target + " <= 15000");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] Volatility Buffer Test Exception: " + e);
            failed++;
        }

        // Test 4: UPI SMS Parser - SBI Credit
        try {
            String msg = "Your A/C *1234 is credited with Rs 3,200.00 on 01-Sep via UPI from swiggy.partner@sbi Ref: SBI90234812.";
            TransactionEntity tx = UpiSmsParser.parse("SBIINB", msg, System.currentTimeMillis());
            if (tx != null && "CREDIT".equals(tx.getType()) && tx.getAmount() == 3200.0 && "swiggy.partner@sbi".equals(tx.getVpa())) {
                System.out.println("✅ [PASS] UPI SMS Parser (SBI Credit ₹3,200 from Swiggy)");
                passed++;
            } else {
                System.err.println("❌ [FAIL] UPI SMS Parser SBI Credit");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] UPI SMS Parser SBI Credit Exception: " + e);
            failed++;
        }

        // Test 5: UPI SMS Parser - EMI Detection
        try {
            String msg = "Auto-debit for Two-Wheeler Loan EMI of Rs 1,250.00 debited from A/C *9876 on 03-Sep. Mandate Ref: AXS98123456";
            TransactionEntity tx = UpiSmsParser.parse("AXISBK", msg, System.currentTimeMillis());
            if (tx != null && "DEBIT".equals(tx.getType()) && tx.getAmount() == 1250.0 && tx.isEmi() && "EMI".equals(tx.getCategory())) {
                System.out.println("✅ [PASS] UPI SMS Parser (Two-Wheeler Loan EMI Detection ₹1,250)");
                passed++;
            } else {
                System.err.println("❌ [FAIL] UPI SMS Parser EMI Detection");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] UPI SMS Parser EMI Exception: " + e);
            failed++;
        }

        // Test 6: Ignore Non-monetary SMS
        try {
            String msg = "Get 50% discount on your next ride with code SAVE50. Valid till midnight!";
            TransactionEntity tx = UpiSmsParser.parse("DM-PROMO", msg, System.currentTimeMillis());
            if (tx == null) {
                System.out.println("✅ [PASS] UPI SMS Parser (Correctly Ignored Non-Monetary Promotional SMS)");
                passed++;
            } else {
                System.err.println("❌ [FAIL] Non-monetary SMS was incorrectly parsed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] Non-monetary SMS Exception: " + e);
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("  SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
