@echo off
cd /d "%~dp0android\platform-tools"
adb.exe logcat -c
echo Monitoring WiFi logs... Open the app now.
echo.
adb.exe logcat | findstr /i "WiFi Timber"
