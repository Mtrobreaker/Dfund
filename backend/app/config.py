import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    PROJECT_NAME: str = "DFund Backend"
    VERSION: str = "1.0.0"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    
    # Database
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL", 
        "postgresql+asyncpg://dfund_user:dfund_secure_password@localhost:5432/dfund_db"
    )
    SQLITE_FALLBACK_URL: str = os.getenv(
        "SQLITE_FALLBACK_URL", 
        "sqlite+aiosqlite:///./dfund_dev.db"
    )
    
    # Sarvam AI
    SARVAM_API_KEY: str = os.getenv("SARVAM_API_KEY", "")
    SARVAM_BASE_URL: str = os.getenv("SARVAM_BASE_URL", "https://api.sarvam.ai")

    # Ollama Cloud / Minimax-M3
    OLLAMA_API_KEY: str = os.getenv("OLLAMA_API_KEY", "")
    OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "https://ollama.com/api")
    OLLAMA_MODEL: str = os.getenv("OLLAMA_MODEL", "minimax-m3:cloud")

settings = Settings()
