@echo off
echo ==========================================
echo   DFund 1-Click Phone Installer and Launcher
echo ==========================================
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist "%ADB%" (
    set "ADB=adb"
)

echo [1/4] Checking connected physical device...
"%ADB%" -d get-serialno

echo [2/4] Setting up reverse port mapping (8000)...
"%ADB%" -d reverse tcp:8000 tcp:8000

echo [3/4] Installing DFund APK onto your phone...
if exist "%~dp0DFund-app-debug.apk" (
    "%ADB%" -d install -r "%~dp0DFund-app-debug.apk"
) else (
    "%ADB%" -d install -r "%~dp0android\app\build\outputs\apk\debug\app-debug.apk"
)

echo [4/4] Launching DFund on your phone screen...
"%ADB%" -d shell am start -n com.dfund.app/.ui.MainActivity

echo.
echo ==========================================
echo   DONE! DFund is now running on your phone.
echo ==========================================
pause
