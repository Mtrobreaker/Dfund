package com.dfund.app.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinancialMathEngine {

    public static class SipResult {
        public final double investedAmount;
        public final double estimatedReturns;
        public final double totalMaturityValue;

        public SipResult(double investedAmount, double estimatedReturns, double totalMaturityValue) {
            this.investedAmount = Math.round(investedAmount * 100.0) / 100.0;
            this.estimatedReturns = Math.round(estimatedReturns * 100.0) / 100.0;
            this.totalMaturityValue = Math.round(totalMaturityValue * 100.0) / 100.0;
        }
    }

    public static class SurplusComparison {
        public final double surplusAmount;
        public final int tenureYears;
        public final double idleBankValue;
        public final double recurringDepositValue;
        public final double microSipValue;
        public final double digitalGoldValue;
        public final String recommendation;

        public SurplusComparison(double surplusAmount, int tenureYears, double idleBankValue, 
                                 double recurringDepositValue, double microSipValue, 
                                 double digitalGoldValue, String recommendation) {
            this.surplusAmount = surplusAmount;
            this.tenureYears = tenureYears;
            this.idleBankValue = Math.round(idleBankValue * 100.0) / 100.0;
            this.recurringDepositValue = Math.round(recurringDepositValue * 100.0) / 100.0;
            this.microSipValue = Math.round(microSipValue * 100.0) / 100.0;
            this.digitalGoldValue = Math.round(digitalGoldValue * 100.0) / 100.0;
            this.recommendation = recommendation;
        }
    }

    /**
     * Calculates SIP Return:
     * FV = P * [((1 + i)^n - 1) / i] * (1 + i)
     */
    public static SipResult calculateSip(double monthlyInvestment, double annualRatePercent, int tenureYears) {
        if (monthlyInvestment <= 0 || tenureYears <= 0) {
            return new SipResult(0, 0, 0);
        }

        double i = (annualRatePercent / 100.0) / 12.0;
        int n = tenureYears * 12;
        double invested = monthlyInvestment * n;

        double fv;
        if (i > 0) {
            fv = monthlyInvestment * ((Math.pow(1.0 + i, n) - 1.0) / i) * (1.0 + i);
        } else {
            fv = invested;
        }

        double returns = Math.max(0.0, fv - invested);
        return new SipResult(invested, returns, fv);
    }

    public static double calculateSipMaturity(double monthlyInvestment, int tenureYears, double annualRatePercent) {
        return calculateSip(monthlyInvestment, annualRatePercent, tenureYears).totalMaturityValue;
    }

    /**
     * Compares 3-Way Surplus Allocation for parents & irregular income earners:
     * 1. Idle Bank Account (3% p.a.)
     * 2. Recurring Deposit / Liquid Fund (7% p.a.)
     * 3. Micro-SIP Index Fund (12.5% p.a.)
     * 4. Digital Gold (10% p.a.)
     */
    public static SurplusComparison compareSurplus(double amount, int tenureYears) {
        if (amount <= 0) amount = 500.0;
        if (tenureYears <= 0) tenureYears = 3;

        double idleBank = amount * Math.pow(1.0 + 0.03, tenureYears);
        double rd = amount * Math.pow(1.0 + 0.07, tenureYears);
        double sip = amount * Math.pow(1.0 + 0.125, tenureYears);
        double gold = amount * Math.pow(1.0 + 0.10, tenureYears);

        String recommendation = String.format(
            "Putting ₹%.0f in a Micro-SIP can grow to ₹%.0f vs only ₹%.0f idle in bank.",
            amount, sip, idleBank
        );

        return new SurplusComparison(amount, tenureYears, idleBank, rd, sip, gold, recommendation);
    }

    /**
     * Calibrates Safety Shield for irregular gig worker income.
     * Computes volatility buffer based on rolling income standard deviation.
     */
    public static double calculateSafetyShieldTarget(List<Double> pastMonthlyIncomes, double avgMonthlyExpense) {
        if (pastMonthlyIncomes == null || pastMonthlyIncomes.isEmpty()) {
            return Math.max(5000.0, avgMonthlyExpense * 1.5);
        }

        // Calculate Mean
        double sum = 0;
        for (double val : pastMonthlyIncomes) sum += val;
        double mean = sum / pastMonthlyIncomes.size();

        // Calculate Variance & StdDev (Income Volatility)
        double variance = 0;
        for (double val : pastMonthlyIncomes) {
            variance += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(variance / pastMonthlyIncomes.size());

        // Target Cushion = Baseline 1.5x Expense + 50% of Volatility StdDev
        double target = (avgMonthlyExpense * 1.5) + (stdDev * 0.5);
        return Math.round(target * 100.0) / 100.0;
    }
}
