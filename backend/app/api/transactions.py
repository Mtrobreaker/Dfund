from typing import List, Optional
from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, desc

from app.database import get_db
from app.models.transaction import Transaction
from app.schemas.transaction import TransactionCreate, TransactionResponse, FinancialSummary

router = APIRouter(prefix="/api/transactions", tags=["Transactions"])

@router.post("/", response_model=TransactionResponse)
async def create_transaction(
    tx_in: TransactionCreate,
    db: AsyncSession = Depends(get_db)
):
    tx = Transaction(
        device_id=tx_in.device_id,
        bank_name=tx_in.bank_name,
        amount=tx_in.amount,
        type=tx_in.type.upper(),
        category=tx_in.category.upper(),
        vpa=tx_in.vpa,
        utr=tx_in.utr,
        raw_message=tx_in.raw_message,
        is_recurring=tx_in.is_recurring,
        is_emi=tx_in.is_emi,
        timestamp=tx_in.timestamp or datetime.utcnow()
    )
    db.add(tx)
    await db.commit()
    await db.refresh(tx)
    return tx

@router.get("/", response_model=List[TransactionResponse])
async def list_transactions(
    device_id: str = Query(..., description="Device identifier"),
    category: Optional[str] = None,
    limit: int = 50,
    db: AsyncSession = Depends(get_db)
):
    query = select(Transaction).where(Transaction.device_id == device_id)
    if category:
        query = query.where(Transaction.category == category.upper())
    query = query.order_by(desc(Transaction.timestamp)).limit(limit)
    
    result = await db.execute(query)
    return result.scalars().all()

@router.get("/summary", response_model=FinancialSummary)
async def get_financial_summary(
    device_id: str = Query(..., description="Device identifier"),
    db: AsyncSession = Depends(get_db)
):
    # Fetch all transactions for this device
    result = await db.execute(
        select(Transaction).where(Transaction.device_id == device_id)
    )
    transactions = result.scalars().all()

    income = sum(t.amount for t in transactions if t.type == "CREDIT")
    spending = sum(t.amount for t in transactions if t.type == "DEBIT")
    emi = sum(t.amount for t in transactions if t.type == "DEBIT" and (t.is_emi or t.category == "EMI"))
    recurring = sum(t.amount for t in transactions if t.is_recurring)

    # Calculate surplus
    surplus = max(0.0, income - spending)
    # Calibrated 50% safety shield buffer, 50% growth pot
    safety_cushion = round(surplus * 0.5, 2)
    growth_pot = round(surplus * 0.5, 2)

    return FinancialSummary(
        total_income=round(income, 2),
        total_spending=round(spending, 2),
        total_emi=round(emi, 2),
        total_recurring=round(recurring, 2),
        available_surplus=round(surplus, 2),
        safety_shield_balance=safety_cushion,
        growth_pot_balance=growth_pot,
        transaction_count=len(transactions)
    )

@router.post("/sync")
async def sync_batch_transactions(
    transactions_in: List[TransactionCreate],
    db: AsyncSession = Depends(get_db)
):
    imported = 0
    for tx_in in transactions_in:
        # Check if already exists for this device by raw_message or timestamp
        query = select(Transaction).where(
            Transaction.device_id == tx_in.device_id,
            Transaction.raw_message == tx_in.raw_message
        )
        existing = await db.execute(query)
        if existing.scalar_one_or_none() is None:
            tx = Transaction(
                device_id=tx_in.device_id,
                bank_name=tx_in.bank_name,
                amount=tx_in.amount,
                type=tx_in.type.upper(),
                category=tx_in.category.upper(),
                vpa=tx_in.vpa,
                utr=tx_in.utr,
                raw_message=tx_in.raw_message,
                is_recurring=tx_in.is_recurring,
                is_emi=tx_in.is_emi,
                timestamp=tx_in.timestamp or datetime.utcnow()
            )
            db.add(tx)
            imported += 1

    if imported > 0:
        await db.commit()

    return {"status": "success", "imported": imported}

