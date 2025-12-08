::This file is  meant to give easy access to key database internals. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running database_control_menu.bat in the terminal)

@echo off
setlocal enabledelayedexpansion

REM Ensure we are in the script directory (project root)
cd /d "%~dp0"

:menu
echo.
echo ========= Wikipedia Race Menu =========
echo 1^) Run App (via Maven)
echo 2^) Print DuckDB contents (via DbInspector)
echo 3^) Exit
echo ========================================
set /p choice=Enter choice: 

if "%choice%"=="1" goto run_app
if "%choice%"=="2" goto print_db
if "%choice%"=="3" goto end
echo Invalid option.
goto menu

:run_app
echo 🚀 Running com.example.App via Maven...
mvn -f project_internals\pom.xml -q exec:java -Dexec.mainClass=com.example.App
goto menu

:print_db
echo 📄 Printing DuckDB contents with DbInspector...

REM NOTE: Windows uses semicolon ";" not colon ":" for classpath separation
java -cp "project_internals\target\classes;project_internals\target\dependency\*" com.example.DbInspector

goto menu

:end
echo Goodbye!
endlocal
exit /b 0