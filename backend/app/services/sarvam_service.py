import re
import logging
import httpx
from typing import Dict, Any, Optional, Tuple
from app.config import settings
from app.schemas.voice import VoiceActionPayload

logger = logging.getLogger("dfund.sarvam")

class SarvamService:
    def __init__(self):
        self.api_key = settings.SARVAM_API_KEY
        self.base_url = settings.SARVAM_BASE_URL.rstrip("/")

    async def transcribe_audio(self, audio_bytes: bytes, filename: str = "audio.wav") -> Tuple[str, str]:
        """
        Transcribes audio using Sarvam Saaras STT API if key is available.
        Returns (transcript, detected_language_code).
        """
        if self.api_key and self.api_key != "mock_key":
            try:
                async with httpx.AsyncClient(timeout=15.0) as client:
                    files = {"file": (filename, audio_bytes, "audio/wav")}
                    headers = {"api-subscription-key": self.api_key}
                    response = await client.post(
                        f"{self.base_url}/speech-to-text",
                        files=files,
                        headers=headers
                    )
                    if response.status_code == 200:
                        data = response.json()
                        transcript = data.get("transcript", "")
                        language = data.get("language_code", "en-IN")
                        return transcript, language
            except Exception as e:
                logger.error(f"Sarvam STT API error: {e}. Falling back to default transcript.")

        return "Show my spending on food this month", "en-IN"

    async def translate_text(
        self,
        text: str,
        source_language_code: str = "auto",
        target_language_code: str = "ta-IN"
    ) -> str:
        """
        Translates text using Sarvam Translation API (sarvam-translate:v1).
        """
        if not text or not text.strip():
            return ""

        if source_language_code == target_language_code:
            return text

        if self.api_key and self.api_key != "mock_key":
            try:
                payload = {
                    "input": text,
                    "source_language_code": source_language_code,
                    "target_language_code": target_language_code,
                    "model": "sarvam-translate:v1"
                }
                headers = {
                    "Content-Type": "application/json",
                    "api-subscription-key": self.api_key
                }
                async with httpx.AsyncClient(timeout=15.0) as client:
                    response = await client.post(
                        f"{self.base_url}/translate",
                        json=payload,
                        headers=headers
                    )
                    if response.status_code == 200:
                        data = response.json()
                        translated = data.get("translated_text", "")
                        if translated:
                            return translated
            except Exception as e:
                logger.error(f"Sarvam Translation API error: {e}")

        # Fallback if offline or API unreachable
        return text

    async def translate_to_tamil(self, text: str, source_language_code: str = "auto") -> str:
        """
        Translates user prompt into Tamil.
        If already in Tamil (checked via Tamil Unicode range 0x0B80-0x0BFF), returns as is.
        """
        # Quick check if text is already primarily Tamil
        tamil_chars = sum(1 for c in text if 0x0B80 <= ord(c) <= 0x0BFF)
        if tamil_chars > 3 and tamil_chars > len(text) * 0.3:
            return text

        src = "auto"
        if "te" in source_language_code:
            src = "te-IN"
        elif "ml" in source_language_code:
            src = "ml-IN"
        elif "en" in source_language_code:
            src = "en-IN"
        elif "hi" in source_language_code:
            src = "hi-IN"

        return await self.translate_text(text, source_language_code=src, target_language_code="ta-IN")

    async def translate_from_tamil(self, tamil_text: str, target_lang: str) -> str:
        """
        Translates Tamil financial suggestions into the user's preferred language (en, te, ml, etc.)
        """
        if target_lang in ["ta", "ta-IN"]:
            return tamil_text

        target_code = "en-IN"
        if target_lang in ["te", "te-IN"]:
            target_code = "te-IN"
        elif target_lang in ["ml", "ml-IN"]:
            target_code = "ml-IN"
        elif target_lang in ["hi", "hi-IN"]:
            target_code = "hi-IN"

        return await self.translate_text(tamil_text, source_language_code="ta-IN", target_language_code=target_code)

    async def synthesize_speech(
        self,
        text: str,
        target_language_code: str = "ta-IN",
        speaker: Optional[str] = None
    ) -> Optional[str]:
        """
        Synthesizes speech into base64 audio using Sarvam Bulbul v3 TTS API.
        Valid speakers include: kavitha, priya, aditya, rahul, shreya, amit.
        """
        if not text or not self.api_key or self.api_key == "mock_key":
            return None

        # Choose appropriate speaker based on language
        chosen_speaker = speaker or "kavitha"
        if "en" in target_language_code:
            chosen_speaker = speaker or "priya"
        elif "te" in target_language_code:
            chosen_speaker = speaker or "kavitha"
        elif "ml" in target_language_code:
            chosen_speaker = speaker or "kavitha"

        lang_code = target_language_code
        if len(lang_code) == 2:
            lang_code = f"{lang_code}-IN"

        payload = {
            "inputs": [text[:500]],  # Bulbul supports up to 2500 chars, limit to 500 for fast UI response
            "target_language_code": lang_code,
            "speaker": chosen_speaker,
            "pitch": 0,
            "pace": 1.0,
            "loudness": 1.5,
            "speech_sample_rate": 22050,
            "enable_preprocessing": True,
            "model": "bulbul:v3"
        }

        headers = {
            "Content-Type": "application/json",
            "api-subscription-key": self.api_key
        }

        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                response = await client.post(
                    f"{self.base_url}/text-to-speech",
                    json=payload,
                    headers=headers
                )
                if response.status_code == 200:
                    data = response.json()
                    audios = data.get("audios", [])
                    if audios:
                        return audios[0]
                else:
                    logger.warning(f"Sarvam TTS API status {response.status_code}: {response.text}")
        except Exception as e:
            logger.error(f"Sarvam TTS API error: {e}")

        return None

sarvam_service = SarvamService()
