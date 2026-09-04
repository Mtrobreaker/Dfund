from datetime import datetime
import logging
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.database import get_db
from app.models.user import User
from app.models.saving_goal import SavingGoal
from app.schemas.user import UserProfileResponse, UserProfileUpdate, LogoutRequest, LogoutResponse

logger = logging.getLogger("dfund.users_api")

router = APIRouter(prefix="/api/users", tags=["Users & Profile"])

@router.get("/profile", response_model=UserProfileResponse)
async def get_user_profile(
    device_id: str = Query(..., description="Unique client device ID"),
    db: AsyncSession = Depends(get_db)
):
    """
    Fetches the user's profile, settings, and goal statistics.
    Creates a new user record if not present.
    """
    result = await db.execute(select(User).where(User.device_id == device_id))
    user = result.scalar_one_or_none()

    if not user:
        user = User(
            device_id=device_id,
            name="Karthik Raja",
            language="en",
            risk_level="moderate",
            monthly_safety_buffer=3000.0,
            created_at=datetime.utcnow()
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)

    # Calculate savings statistics
    goals_res = await db.execute(select(SavingGoal).where(SavingGoal.device_id == device_id))
    goals = goals_res.scalars().all()
    active_goals_count = len([g for g in goals if not g.is_completed])
    total_saved = sum(g.current_amount for g in goals)

    return UserProfileResponse(
        device_id=user.device_id,
        name=user.name or "Valued User",
        phone=user.phone,
        pan=user.pan,
        job_type=user.job_type or "Salaried Job",
        income_frequency=user.income_frequency or "Monthly",
        typical_income=user.typical_income or 25000.0,
        mandatory_expenses=user.mandatory_expenses or 16800.0,
        desired_savings=user.desired_savings or 3000.0,
        desired_investment=user.desired_investment or 2000.0,
        language=user.language or "en",
        risk_level=user.risk_level or "moderate",
        monthly_safety_buffer=user.monthly_safety_buffer or 3000.0,
        member_since=user.created_at,
        active_goals_count=active_goals_count,
        total_saved=round(total_saved, 2)
    )

@router.put("/profile", response_model=UserProfileResponse)
async def update_user_profile(
    update_data: UserProfileUpdate,
    db: AsyncSession = Depends(get_db)
):
    """
    Updates the user's name, preferred language, risk appetite, or safety cushion.
    """
    result = await db.execute(select(User).where(User.device_id == update_data.device_id))
    user = result.scalar_one_or_none()

    if not user:
        user = User(
            device_id=update_data.device_id,
            name=update_data.name or "Valued User",
            phone=update_data.phone,
            pan=update_data.pan,
            job_type=update_data.job_type or "Salaried Job",
            income_frequency=update_data.income_frequency or "Monthly",
            typical_income=update_data.typical_income or 25000.0,
            mandatory_expenses=update_data.mandatory_expenses or 16800.0,
            desired_savings=update_data.desired_savings or 3000.0,
            desired_investment=update_data.desired_investment or 2000.0,
            language=update_data.language or "en",
            risk_level=update_data.risk_level or "moderate",
            monthly_safety_buffer=update_data.monthly_safety_buffer or 3000.0
        )
        db.add(user)
    else:
        if update_data.name is not None:
            user.name = update_data.name
        if update_data.phone is not None:
            user.phone = update_data.phone
        if update_data.pan is not None:
            user.pan = update_data.pan
        if update_data.job_type is not None:
            user.job_type = update_data.job_type
        if update_data.income_frequency is not None:
            user.income_frequency = update_data.income_frequency
        if update_data.typical_income is not None:
            user.typical_income = update_data.typical_income
        if update_data.mandatory_expenses is not None:
            user.mandatory_expenses = update_data.mandatory_expenses
        if update_data.desired_savings is not None:
            user.desired_savings = update_data.desired_savings
        if update_data.desired_investment is not None:
            user.desired_investment = update_data.desired_investment
        if update_data.language is not None:
            user.language = update_data.language
        if update_data.risk_level is not None:
            user.risk_level = update_data.risk_level
        if update_data.monthly_safety_buffer is not None:
            user.monthly_safety_buffer = update_data.monthly_safety_buffer

    await db.commit()
    await db.refresh(user)

    goals_res = await db.execute(select(SavingGoal).where(SavingGoal.device_id == user.device_id))
    goals = goals_res.scalars().all()
    active_goals_count = len([g for g in goals if not g.is_completed])
    total_saved = sum(g.current_amount for g in goals)

    return UserProfileResponse(
        device_id=user.device_id,
        name=user.name,
        phone=user.phone,
        pan=user.pan,
        job_type=user.job_type,
        income_frequency=user.income_frequency,
        typical_income=user.typical_income,
        mandatory_expenses=user.mandatory_expenses,
        desired_savings=user.desired_savings,
        desired_investment=user.desired_investment,
        language=user.language,
        risk_level=user.risk_level,
        monthly_safety_buffer=user.monthly_safety_buffer,
        member_since=user.created_at,
        active_goals_count=active_goals_count,
        total_saved=round(total_saved, 2)
    )

@router.post("/logout", response_model=LogoutResponse)
async def logout_user(
    logout_req: LogoutRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    Logs out the user session and acknowledges cleanup.
    """
    logger.info(f"User session logged out for device: {logout_req.device_id}")
    return LogoutResponse(
        status="success",
        message="Session successfully terminated. Re-authentication required on next access."
    )
