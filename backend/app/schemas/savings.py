from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field

class SavingGoalBase(BaseModel):
    title: str
    target_amount: float
    saved_amount: float = 0.0
    category: str = "GROWTH"  # 'SAFETY_SHIELD', 'GOLD', 'SIP', 'RD'
    target_date: Optional[datetime] = None

class SavingGoalCreate(SavingGoalBase):
    device_id: str

class SavingGoalResponse(SavingGoalBase):
    id: int
    device_id: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class SurplusAllocationComparison(BaseModel):
    surplus_amount: float
    tenure_years: int
    idle_bank_value: float
    recurring_deposit_value: float
    micro_sip_value: float
    digital_gold_value: float
    best_recommendation: str
    reasoning: str
