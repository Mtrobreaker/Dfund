from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, DateTime
from app.database import Base

class SavingGoal(Base):
    __tablename__ = "saving_goals"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(128), index=True, nullable=False)
    title = Column(String(100), nullable=False)
    target_amount = Column(Float, nullable=False)
    saved_amount = Column(Float, default=0.0)
    category = Column(String(50), default="GROWTH")  # 'SAFETY_SHIELD', 'GOLD', 'SIP', 'RD', 'EMERGENCY'
    target_date = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
