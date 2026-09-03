import logging
from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import declarative_base
from app.config import settings

logger = logging.getLogger("dfund.database")

Base = declarative_base()

sqlite_engine = create_async_engine(
    settings.SQLITE_FALLBACK_URL,
    echo=(settings.ENVIRONMENT == "development")
)

active_engine = sqlite_engine
postgres_engine = None

try:
    postgres_engine = create_async_engine(
        settings.DATABASE_URL,
        echo=(settings.ENVIRONMENT == "development"),
        pool_pre_ping=True
    )
    active_engine = postgres_engine
except Exception as e:
    logger.info(f"Using SQLite engine for local execution: {e}")
    active_engine = sqlite_engine

AsyncSessionLocal = async_sessionmaker(
    bind=active_engine,
    class_=AsyncSession,
    expire_on_commit=False
)

async def init_db():
    global active_engine, AsyncSessionLocal
    if postgres_engine is not None:
        try:
            # Try connecting to PostgreSQL
            async with postgres_engine.connect() as conn:
                logger.info("Connected successfully to PostgreSQL database.")
                active_engine = postgres_engine
        except Exception as e:
            logger.warning(f"PostgreSQL connection failed ({e}). Switching to SQLite fallback.")
            active_engine = sqlite_engine
            AsyncSessionLocal = async_sessionmaker(
                bind=active_engine,
                class_=AsyncSession,
                expire_on_commit=False
            )
    else:
        active_engine = sqlite_engine
        AsyncSessionLocal = async_sessionmaker(
            bind=active_engine,
            class_=AsyncSession,
            expire_on_commit=False
        )

    # Create tables
    async with active_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        logger.info(f"Database schema initialized on {'PostgreSQL' if active_engine == postgres_engine else 'SQLite'}.")

async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()
