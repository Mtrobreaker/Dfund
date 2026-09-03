from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field

class TransactionBase(BaseModel):
    amount: float = Field(..., gt=0, description="Transaction amount in INR")
    type: str = Field(..., description="DEBIT or CREDIT")
    bank_name: Optional[str] = None
    category: str = Field("OTHER", description="EMI, GROCERY, FUEL, BILL, SALARY, SURPLUS, etc.")
    vpa: Optional[str] = None
    utr: Optional[str] = None
    raw_message: Optional[str] = None
    is_recurring: bool = False
    is_emi: bool = False
    timestamp: Optional[datetime] = None

class TransactionCreate(TransactionBase):
    device_id: str

class TransactionResponse(TransactionBase):
    id: int
    device_id: str
    timestamp: datetime

    class Config:
        from_attributes = True

class FinancialSummary(BaseModel):
    total_income: float
    total_spending: float
    total_emi: float
    total_recurring: float
    available_surplus: float
    safety_shield_balance: float
    growth_pot_balance: float
    transaction_count: int
