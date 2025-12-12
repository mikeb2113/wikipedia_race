::This file is the access database module files. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running maven_setup.bat in the terminal)

@echo off
setlocal

REM Go to project root (this file's directory)
cd /d "%~dp0" || exit /b 1

echo 🔧 Building project internals (clean + package + copy dependencies)...
call mvn -f "project_internals\pom.xml" -DskipTests clean package dependency:copy-dependencies
if errorlevel 1 (
    echo ❌ Maven build failed.
    exit /b %ERRORLEVEL%
)

echo ✅ Maven build complete.
exit /b 0