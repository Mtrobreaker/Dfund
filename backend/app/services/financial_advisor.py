import math
from typing import Dict, Any, List
from app.schemas.savings import SurplusAllocationComparison

class FinancialAdvisor:
    @staticmethod
    def calculate_sip(monthly_amount: float, annual_rate_percent: float, tenure_years: int) -> Dict[str, float]:
        """
        Calculates SIP maturity value:
        FV = P * [((1 + i)^n - 1) / i] * (1 + i)
        where i = r / (12 * 100), n = tenure_years * 12
        """
        if monthly_amount <= 0 or tenure_years <= 0:
            return {"invested_amount": 0.0, "estimated_returns": 0.0, "total_value": 0.0}

        i = (annual_rate_percent / 100.0) / 12.0
        n = tenure_years * 12
        invested = monthly_amount * n

        if i > 0:
            future_value = monthly_amount * (((1 + i) ** n - 1) / i) * (1 + i)
        else:
            future_value = invested

        returns = max(0.0, future_value - invested)
        return {
            "invested_amount": round(invested, 2),
            "estimated_returns": round(returns, 2),
            "total_value": round(future_value, 2)
        }

    @staticmethod
    def compare_surplus_options(amount: float, tenure_years: int = 3) -> SurplusAllocationComparison:
        """
        Compares investing small surplus (one-time or recurring) across:
        1. Idle Bank Account (~3.0% p.a.)
        2. Recurring Deposit / Liquid Fund (~7.0% p.a.)
        3. Micro-SIP Equity Index (~12.5% p.a.)
        4. Digital Gold (~10.0% p.a.)
        """
        n = tenure_years
        idle_bank = amount * ((1 + 0.03) ** n)
        rd_val = amount * ((1 + 0.07) ** n)
        sip_val = amount * ((1 + 0.125) ** n)
        gold_val = amount * ((1 + 0.10) ** n)

        recommendation = "Micro-SIP Equity Index"
        reasoning = (
            f"If left idle in your bank savings account, ₹{int(amount)} grows to only ₹{int(idle_bank)} in {tenure_years} years. "
            f"Allocating it to a disciplined Micro-SIP could yield approx ₹{int(sip_val)} (+{int((sip_val/amount - 1)*100)}%), "
            f"or Digital Gold at ₹{int(gold_val)}."
        )

        return SurplusAllocationComparison(
            surplus_amount=round(amount, 2),
            tenure_years=tenure_years,
            idle_bank_value=round(idle_bank, 2),
            recurring_deposit_value=round(rd_val, 2),
            micro_sip_value=round(sip_val, 2),
            digital_gold_value=round(gold_val, 2),
            best_recommendation=recommendation,
            reasoning=reasoning
        )

financial_advisor = FinancialAdvisor()
