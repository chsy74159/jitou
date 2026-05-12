@echo off
setlocal

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0deploy-debug.ps1"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo Deploy failed. Press any key to close this window.
    pause >nul
)

exit /b %EXIT_CODE%
