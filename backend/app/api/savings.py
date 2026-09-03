from typing import List
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.database import get_db
from app.models.saving_goal import SavingGoal
from app.schemas.savings import SavingGoalCreate, SavingGoalResponse, SurplusAllocationComparison
from app.services.financial_advisor import financial_advisor

router = APIRouter(prefix="/api/savings", tags=["Savings & Financial Planning"])

@router.post("/goals", response_model=SavingGoalResponse)
async def create_saving_goal(
    goal_in: SavingGoalCreate,
    db: AsyncSession = Depends(get_db)
):
    goal = SavingGoal(
        device_id=goal_in.device_id,
        title=goal_in.title,
        target_amount=goal_in.target_amount,
        saved_amount=goal_in.saved_amount,
        category=goal_in.category.upper(),
        target_date=goal_in.target_date
    )
    db.add(goal)
    await db.commit()
    await db.refresh(goal)
    return goal

@router.get("/goals", response_model=List[SavingGoalResponse])
async def list_saving_goals(
    device_id: str = Query(...),
    db: AsyncSession = Depends(get_db)
):
    result = await db.execute(
        select(SavingGoal).where(SavingGoal.device_id == device_id)
    )
    return result.scalars().all()

@router.get("/compare-surplus", response_model=SurplusAllocationComparison)
async def compare_surplus(
    amount: float = Query(..., gt=0, description="Surplus amount in INR"),
    tenure_years: int = Query(3, ge=1, le=30)
):
    return financial_advisor.compare_surplus_options(amount=amount, tenure_years=tenure_years)

@router.get("/calculate-sip")
async def calculate_sip(
    monthly_amount: float = Query(..., gt=0),
    annual_rate: float = Query(12.5, gt=0),
    tenure_years: int = Query(3, ge=1, le=40)
):
    return financial_advisor.calculate_sip(
        monthly_amount=monthly_amount,
        annual_rate_percent=annual_rate,
        tenure_years=tenure_years
    )
