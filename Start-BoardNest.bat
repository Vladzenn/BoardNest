@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-BoardNest.ps1"
if errorlevel 1 (
  echo.
  echo BoardNest could not start. Read the message above, fix the issue, then try again.
  pause
)
endlocal
