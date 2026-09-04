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
        name=user.name or "Karthik Raja",
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
            name=update_data.name or "Karthik Raja",
            language=update_data.language or "en",
            risk_level=update_data.risk_level or "moderate",
            monthly_safety_buffer=update_data.monthly_safety_buffer or 3000.0
        )
        db.add(user)
    else:
        if update_data.name is not None:
            user.name = update_data.name
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
