from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, DateTime
from app.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(128), unique=True, index=True, nullable=False)
    name = Column(String(100), nullable=True, default="Valued User")
    phone = Column(String(20), nullable=True)
    pan = Column(String(20), nullable=True)
    job_type = Column(String(50), default="Salaried Job")
    income_frequency = Column(String(20), default="Monthly")
    typical_income = Column(Float, default=25000.0)
    mandatory_expenses = Column(Float, default=16800.0)
    desired_savings = Column(Float, default=3000.0)
    desired_investment = Column(Float, default=2000.0)
    language = Column(String(10), default="en")  # 'en', 'hi', 'ta', 'te'
    risk_level = Column(String(20), default="moderate")  # 'conservative', 'moderate', 'aggressive'
    monthly_safety_buffer = Column(Float, default=3000.0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
