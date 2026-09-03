package com.dfund.app;

import com.dfund.app.domain.FinancialMathEngine;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class FinancialMathEngineTest {

    @Test
    public void testSipCalculationZeroInput() {
        FinancialMathEngine.SipResult result = FinancialMathEngine.calculateSip(0, 12, 3);
        assertEquals(0.0, result.investedAmount, 0.01);
        assertEquals(0.0, result.totalMaturityValue, 0.01);
    }

    @Test
    public void testSipCalculationPositiveReturn() {
        // ₹500/month for 3 years at 12% p.a.
        // Total invested = 500 * 36 = ₹18,000
        FinancialMathEngine.SipResult result = FinancialMathEngine.calculateSip(500.0, 12.0, 3);
        assertEquals(18000.0, result.investedAmount, 0.01);
        assertTrue("Maturity value should exceed invested amount", result.totalMaturityValue > 21000.0);
        assertTrue("Estimated returns should be positive", result.estimatedReturns > 3000.0);
    }

    @Test
    public void testSurplusComparisonGrowthAdvantage() {
        // ₹1,000 surplus over 3 years
        FinancialMathEngine.SurplusComparison comparison = FinancialMathEngine.compareSurplus(1000.0, 3);
        assertEquals(1000.0, comparison.surplusAmount, 0.01);
        assertEquals(3, comparison.tenureYears);

        // Micro-SIP (~12.5%) > Gold (~10%) > RD (~7%) > Idle Bank (~3%)
        assertTrue("Micro-SIP should yield higher than idle bank", comparison.microSipValue > comparison.idleBankValue);
        assertTrue("Micro-SIP should yield higher than RD", comparison.microSipValue > comparison.recurringDepositValue);
        assertTrue("RD should yield higher than idle bank", comparison.recurringDepositValue > comparison.idleBankValue);
    }

    @Test
    public void testSafetyShieldTargetForIrregularIncome() {
        // Average monthly expense = ₹10,000
        // Past 4 months gig incomes: ₹8,000, ₹15,000, ₹9,000, ₹18,000 (high volatility)
        List<Double> pastIncomes = Arrays.asList(8000.0, 15000.0, 9000.0, 18000.0);
        double target = FinancialMathEngine.calculateSafetyShieldTarget(pastIncomes, 10000.0);

        // Baseline 1.5 * 10,000 = 15,000 + volatility buffer > 15,000
        assertTrue("Safety shield should provide at least 1.5x monthly expense plus buffer", target > 15000.0);
    }
}
