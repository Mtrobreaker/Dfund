from typing import Optional, Dict, Any
from pydantic import BaseModel, Field

class VoiceActionPayload(BaseModel):
    action_type: str = Field(..., description="E.g., ACTION_OPEN_SIP_CALCULATOR, ACTION_SHOW_SPENDING, ACTION_ALLOCATE_SURPLUS, ACTION_CHECK_EMIS, ACTION_CHANGE_LANGUAGE, ACTION_FINANCIAL_ADVICE")
    parameters: Dict[str, Any] = Field(default_factory=dict, description="Extracted parameters e.g. amount, tenure, category, language")
    spoken_response: str = Field(..., description="Localized natural spoken response for TTS")
    action_target_screen: Optional[str] = Field(None, description="Target UI screen ID (e.g., SCREEN_DASHBOARD, SCREEN_SIP, SCREEN_TRANSACTIONS)")

class VoiceInteractionResponse(BaseModel):
    transcription: str
    detected_language: str
    tamil_translation: Optional[str] = None
    financial_suggestion: Optional[str] = None
    action: VoiceActionPayload
    audio_base64: Optional[str] = None
