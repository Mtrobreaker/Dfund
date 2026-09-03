import base64
import logging
from typing import Optional, Dict, Any
from fastapi import APIRouter, UploadFile, File, Form, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.database import get_db
from app.models.transaction import Transaction
from app.schemas.voice import VoiceInteractionResponse, VoiceActionPayload
from app.services.sarvam_service import sarvam_service
from app.services.ollama_service import ollama_service

logger = logging.getLogger("dfund.voice_api")

router = APIRouter(prefix="/api/voice", tags=["Voice AI & Action Dispatch"])

@router.post("/interact", response_model=VoiceInteractionResponse)
async def process_voice_interaction(
    audio_file: Optional[UploadFile] = File(None),
    text_prompt: Optional[str] = Form(None),
    device_id: str = Form(...),
    preferred_language: str = Form("en"),
    db: AsyncSession = Depends(get_db)
):
    """
    AI Architecture Pipeline:
    1. Sarvam Saaras STT hears the voice & detects the user's language.
    2. Sarvam Translate translates user request into Tamil (ta-IN).
    3. User's financial profile is queried from the database.
    4. Ollama Cloud LLM (minimax-m3:cloud) analyzes the user data & Tamil query to produce financial guidance and in-app action codes.
    5. Sarvam Bulbul v3 TTS synthesizes voice response in the user's language.
    6. Returns transcription, Tamil translation, Ollama suggestion, in-app action payload, and base64 audio.
    """
    transcript = ""
    detected_lang = preferred_language

    # Step 1: Hear voice / transcribe input
    if audio_file:
        audio_bytes = await audio_file.read()
        transcript, detected_lang = await sarvam_service.transcribe_audio(
            audio_bytes=audio_bytes, 
            filename=audio_file.filename or "recording.wav"
        )
    elif text_prompt:
        transcript = text_prompt.strip()
    else:
        raise HTTPException(status_code=400, detail="Either audio_file or text_prompt must be provided.")

    lowered = transcript.lower()

    # Fast-path: Language Switching Intent
    if any(w in lowered for w in ["tamil", "தமிழ்", "thamizh"]):
        return VoiceInteractionResponse(
            transcription=transcript,
            detected_language="ta",
            tamil_translation="மொழியைத் தமிழாக மாற்றவும்",
            financial_suggestion="மொழியைத் தமிழாக மாற்றியுள்ளேன். நான் உங்களுக்கு எப்படி உதவ முடியும்?",
            action=VoiceActionPayload(
                action_type="ACTION_CHANGE_LANGUAGE",
                parameters={"language": "ta"},
                spoken_response="மொழியைத் தமிழாக மாற்றியுள்ளேன். நான் உங்களுக்கு எப்படி உதவ முடியும்?",
                action_target_screen="SCREEN_DASHBOARD"
            ),
            audio_base64=await sarvam_service.synthesize_speech("மொழியைத் தமிழாக மாற்றியுள்ளேன்.", "ta-IN")
        )
    elif any(w in lowered for w in ["telugu", "తెలుగు"]):
        return VoiceInteractionResponse(
            transcription=transcript,
            detected_language="te",
            tamil_translation="மொழியைத் தெலுங்காக மாற்றவும்",
            financial_suggestion="భాషను తెలుగుకు మార్చాను. నేను మీకు ఎలా సహాయపడగలను?",
            action=VoiceActionPayload(
                action_type="ACTION_CHANGE_LANGUAGE",
                parameters={"language": "te"},
                spoken_response="భాషను తెలుగుకు మార్చాను. నేను మీకు ఎలా సహాయపడగలను?",
                action_target_screen="SCREEN_DASHBOARD"
            ),
            audio_base64=None
        )
    elif any(w in lowered for w in ["malayalam", "മലയാളം"]):
        return VoiceInteractionResponse(
            transcription=transcript,
            detected_language="ml",
            tamil_translation="மொழியை மலையாளமாக மாற்றவும்",
            financial_suggestion="ഭാഷ മലയാളത്തിലേക്ക് മാറ്റി. ഞാൻ എങ്ങനെ സഹായിക്കണം?",
            action=VoiceActionPayload(
                action_type="ACTION_CHANGE_LANGUAGE",
                parameters={"language": "ml"},
                spoken_response="ഭാഷ മലയാളത്തിലേക്ക് മാറ്റി. ഞാൻ എങ്ങനെ സഹായിക്കണം?",
                action_target_screen="SCREEN_DASHBOARD"
            ),
            audio_base64=None
        )
    elif any(w in lowered for w in ["english", "ஆங்கிலம்", "ఆంగ్లం"]):
        return VoiceInteractionResponse(
            transcription=transcript,
            detected_language="en",
            tamil_translation="ஆங்கில மொழிக்கு மாற்றவும்",
            financial_suggestion="Language changed to English. How can I help you today?",
            action=VoiceActionPayload(
                action_type="ACTION_CHANGE_LANGUAGE",
                parameters={"language": "en"},
                spoken_response="Language changed to English. How can I help you today?",
                action_target_screen="SCREEN_DASHBOARD"
            ),
            audio_base64=None
        )

    # Step 2: Translate to Tamil using Sarvam Translate API
    tamil_translation = await sarvam_service.translate_to_tamil(transcript, detected_lang)

    # Step 3: Fetch User Financial Profile from Database
    result = await db.execute(
        select(Transaction).where(Transaction.device_id == device_id)
    )
    transactions = result.scalars().all()

    income = sum(t.amount for t in transactions if t.type == "CREDIT")
    spending = sum(t.amount for t in transactions if t.type == "DEBIT")
    emi = sum(t.amount for t in transactions if t.type == "DEBIT" and (t.is_emi or t.category == "EMI"))
    surplus = max(0.0, income - spending)
    safety_shield = round(surplus * 0.5, 2)
    growth_pot = round(surplus * 0.5, 2)

    category_spending: Dict[str, float] = {}
    for t in transactions:
        if t.type == "DEBIT":
            cat = t.category or "OTHER"
            category_spending[cat] = category_spending.get(cat, 0.0) + t.amount

    # If new user with no transactions yet, provide realistic starter baseline
    if len(transactions) == 0:
        income = 25000.0
        spending = 16500.0
        emi = 3500.0
        surplus = 5000.0
        safety_shield = 2500.0
        growth_pot = 2500.0
        category_spending = {"FOOD": 4500.0, "GROCERY": 3800.0, "FUEL": 2200.0, "EMI": 3500.0}

    user_financial_profile = {
        "total_income": round(income, 2),
        "total_spending": round(spending, 2),
        "total_emi": round(emi, 2),
        "available_surplus": round(surplus, 2),
        "safety_shield_balance": safety_shield,
        "growth_pot_balance": growth_pot,
        "category_spending": {k: round(v, 2) for k, v in category_spending.items()},
        "transaction_count": len(transactions)
    }

    # Step 4: Analyze with Ollama Cloud LLM (Minimax-M3) using Tamil query & financial context
    analysis = await ollama_service.analyze_financial_data(
        user_data=user_financial_profile,
        query_tamil=tamil_translation,
        preferred_language=preferred_language
    )

    tamil_advice = analysis.get("tamil_suggestion", "")
    english_advice = analysis.get("english_suggestion", "")
    action_type = analysis.get("action_type", "ACTION_FINANCIAL_ADVICE")
    action_params = analysis.get("parameters", {})
    action_screen = analysis.get("action_target_screen", "SCREEN_DASHBOARD")

    # Step 5: Deliver Voice Assistant feedback in the user's preferred language
    user_lang = preferred_language.lower()
    spoken_feedback = ""

    if "ta" in user_lang:
        spoken_feedback = tamil_advice
    elif "en" in user_lang:
        spoken_feedback = english_advice if english_advice else tamil_advice
    else:
        # Translate Tamil advice to user's target language (e.g. te, ml, hi)
        spoken_feedback = await sarvam_service.translate_from_tamil(tamil_advice, user_lang)

    # Synthesize neural voice with Sarvam Bulbul v3 TTS
    audio_b64 = await sarvam_service.synthesize_speech(spoken_feedback, user_lang)

    return VoiceInteractionResponse(
        transcription=transcript,
        detected_language=detected_lang,
        tamil_translation=tamil_translation,
        financial_suggestion=spoken_feedback,
        action=VoiceActionPayload(
            action_type=action_type,
            parameters=action_params,
            spoken_response=spoken_feedback,
            action_target_screen=action_screen
        ),
        audio_base64=audio_b64
    )
