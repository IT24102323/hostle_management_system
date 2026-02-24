@echo off
echo ========================================
echo   Hostel Management System
echo   Starting Application...
echo ========================================
echo.

SET JAVA_HOME=C:\Users\USER\.jdks\ms-21.0.9
SET PATH=%JAVA_HOME%\bin;%PATH%

cd /d D:\Hostel_Management\hostel-management\hostel-management

echo [1/2] Building project...
call mvnw.cmd clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed! Check for errors.
    pause
    exit /b 1
)

echo.
echo [2/2] Starting server...
echo.
echo ========================================
echo   Application starting on:
echo   http://localhost:8080
echo.
echo   Open browser and go to:
echo   http://localhost:8080/login.html
echo ========================================
echo.
echo   Demo Credentials:
echo   Admin    - admin / admin123
echo   Resident - resident / resident123
echo ========================================
echo.
echo Press Ctrl+C to stop the server.
echo.

java -jar target\hostel-management-0.0.1-SNAPSHOT.jar

pause
