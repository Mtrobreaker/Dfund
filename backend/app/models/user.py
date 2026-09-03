from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, DateTime
from app.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(128), unique=True, index=True, nullable=False)
    name = Column(String(100), nullable=True, default="Valued User")
    language = Column(String(10), default="en")  # 'en', 'ta', 'te', 'ml'
    risk_level = Column(String(20), default="moderate")  # 'conservative', 'moderate', 'aggressive'
    monthly_safety_buffer = Column(Float, default=0.0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
