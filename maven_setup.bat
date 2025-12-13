::This file is the access database module files. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running maven_setup.bat in the terminal)

@echo off
setlocal EnableExtensions

REM Go to project root (this file's directory)
cd /d "%~dp0" || goto :FAIL

echo.
echo Repo root: %cd%
echo.

REM Verify Maven is available
where mvn >nul 2>&1
if errorlevel 1 (
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "Add-Type -AssemblyName PresentationFramework; [System.Windows.MessageBox]::Show(\"Maven is not installed.`n`nInstall from:`nhttps://maven.apache.org/download.cgi\",\"Setup Error\",\"OK\",\"Error\") | Out-Null"
    exit /b 1
)

echo ✅ Maven detected:
mvn -v
echo.

echo 🔧 Building project internals (clean + package + copy dependencies)...
call mvn -f "project_internals\pom.xml" -DskipTests clean package dependency:copy-dependencies
if errorlevel 1 (
  echo ❌ Maven build failed with exit code %ERRORLEVEL%.
  goto :FAIL
)

echo ✅ Maven build complete.
goto :DONE

:FAIL
echo.
echo (Build did not complete.)
pause
exit /b 1

:DONE
pause
exit /b 0