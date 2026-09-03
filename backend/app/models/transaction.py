from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, DateTime, Boolean, ForeignKey, Index
from app.database import Base

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(128), index=True, nullable=False)
    bank_name = Column(String(50), nullable=True)
    amount = Column(Float, nullable=False)
    type = Column(String(10), nullable=False)  # 'DEBIT' or 'CREDIT'
    category = Column(String(50), default="OTHER")  # 'EMI', 'GROCERY', 'FUEL', 'BILL', 'SALARY', 'SURPLUS', 'OTHER'
    vpa = Column(String(100), nullable=True)
    utr = Column(String(100), nullable=True)
    raw_message = Column(String(500), nullable=True)
    is_recurring = Column(Boolean, default=False)
    is_emi = Column(Boolean, default=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)

    __table_args__ = (
        Index("idx_device_timestamp", "device_id", "timestamp"),
    )
