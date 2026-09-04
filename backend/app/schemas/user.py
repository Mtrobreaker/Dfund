from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

class UserProfileResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    device_id: str
    name: str = "Valued User"
    phone: Optional[str] = None
    pan: Optional[str] = None
    job_type: str = "Salaried Job"
    income_frequency: str = "Monthly"
    typical_income: float = 25000.0
    mandatory_expenses: float = 16800.0
    desired_savings: float = 3000.0
    desired_investment: float = 2000.0
    language: str = "en"
    risk_level: str = "moderate"
    monthly_safety_buffer: float = 3000.0
    member_since: Optional[datetime] = None
    active_goals_count: int = 0
    total_saved: float = 0.0

class UserProfileUpdate(BaseModel):
    device_id: str
    name: Optional[str] = None
    phone: Optional[str] = None
    pan: Optional[str] = None
    job_type: Optional[str] = None
    income_frequency: Optional[str] = None
    typical_income: Optional[float] = None
    mandatory_expenses: Optional[float] = None
    desired_savings: Optional[float] = None
    desired_investment: Optional[float] = None
    language: Optional[str] = None
    risk_level: Optional[str] = None
    monthly_safety_buffer: Optional[float] = None

class LogoutRequest(BaseModel):
    device_id: str

class LogoutResponse(BaseModel):
    status: str
    message: str
