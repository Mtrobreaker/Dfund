import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import init_db
from app.api.transactions import router as transactions_router
from app.api.voice import router as voice_router
from app.api.savings import router as savings_router
from app.api.users import router as users_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("dfund.main")

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting DFund Backend Server...")
    await init_db()
    yield
    logger.info("Shutting down DFund Backend Server...")

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    lifespan=lifespan,
    description="DFund AI-Powered Automated Personal Finance & Financial Awareness Service"
)

# Enable CORS for local Android emulator (10.0.2.2) and physical devices
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include Routers
app.include_router(transactions_router)
app.include_router(voice_router)
app.include_router(savings_router)
app.include_router(users_router)

@app.get("/", tags=["Root"])
async def root():
    return {
        "status": "online",
        "service": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "documentation": "/docs",
        "health_check": "/api/health",
        "message": "DFund AI Personal Finance Backend is running!"
    }

@app.get("/health", tags=["Health"])
@app.get("/api/health", tags=["Health"])
async def health_check():
    return {
        "status": "healthy",
        "service": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "environment": settings.ENVIRONMENT
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
