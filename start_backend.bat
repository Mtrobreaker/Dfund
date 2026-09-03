@echo off
cd /d "%~dp0backend"
echo ==========================================
echo   Starting DFund FastAPI Backend...
echo ==========================================
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
