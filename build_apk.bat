@echo off
cd /d "%~dp0android"
echo ==========================================
echo   Building DFund Debug APK...
echo ==========================================
call gradlew.bat assembleDebug
if %ERRORLEVEL% equ 0 (
    copy /y "app\build\outputs\apk\debug\app-debug.apk" "..\DFund-app-debug.apk" >nul
    echo.
    echo ==========================================
    echo   BUILD SUCCESSFUL!
    echo   Updated APK: %~dp0DFund-app-debug.apk
    echo ==========================================
) else (
    echo.
    echo [ERROR] Build failed.
)
pause
