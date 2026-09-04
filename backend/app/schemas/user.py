from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

class UserProfileResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    device_id: str
    name: str = "Karthik Raja"
    language: str = "en"
    risk_level: str = "moderate"
    monthly_safety_buffer: float = 3000.0
    member_since: Optional[datetime] = None
    active_goals_count: int = 0
    total_saved: float = 0.0

class UserProfileUpdate(BaseModel):
    device_id: str
    name: Optional[str] = None
    language: Optional[str] = None
    risk_level: Optional[str] = None
    monthly_safety_buffer: Optional[float] = None

class LogoutRequest(BaseModel):
    device_id: str

class LogoutResponse(BaseModel):
    status: str
    message: str
