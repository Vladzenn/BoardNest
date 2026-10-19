@echo off
setlocal
cd /d "%~dp0"
echo.
echo =====================================
echo      BoardNest First-Time Setup
echo =====================================
echo.
where java >nul 2>nul
if errorlevel 1 (
  echo [MISSING] Java. Install JDK 21 and add it to PATH.
) else (
  echo [OK] Java command found. Start-BoardNest checks that it is Java 21.
)
if not exist "%~dp0mvnw.cmd" echo [MISSING] Maven Wrapper. Re-download and extract the complete project ZIP.
if not exist "%~dp0pom.xml" echo [MISSING] pom.xml. Extract the complete project ZIP first.
where mysql >nul 2>nul
if errorlevel 1 (
  echo [NOTE] mysql.exe is not on PATH. Start-BoardNest also searches common Laragon/MySQL locations.
)
echo.
echo FIRST RUN:
echo 1. Make sure Java 21 and MySQL or Laragon are installed.
echo 2. Double-click Start-BoardNest.bat.
echo 3. Enter an application database username and password.
echo 4. If the database/account is missing, the launcher asks for a MySQL administrator login,
echo    creates boarding_house_db and the application account, then creates/checks the tables.
echo 5. Existing databases are not dropped. Missing monthly_rent is added using the migration.
echo.
echo The launcher never saves passwords into the project files.
echo It cannot install Java or MySQL for you. MySQL administrator permission is needed for first-time database creation.
echo.
pause
endlocal
