::This file is the access database module files. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running maven_setup.bat in the terminal)

::This file is the access database module files. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running maven_setup.bat in the terminal)

@echo off
setlocal EnableExtensions

title Maven Setup - Wikipedia Race

REM Log to a file in the same folder as the bat
set "LOG=%~dp0maven_setup.log"
echo ===== RUN %date% %time% ===== > "%LOG%"

echo.
echo [START] maven_setup.bat running...
echo Script path: %~f0
echo.
echo [START] maven_setup.bat running...>>"%LOG%"
echo Script path: %~f0>>"%LOG%"

REM Go to project root (this file's directory)
cd /d "%~dp0"
echo cd exit=%errorlevel%>>"%LOG%"
if errorlevel 1 goto :FAIL

echo Repo root: %cd%
echo Repo root: %cd%>>"%LOG%"

REM Verify Maven is available
where mvn >nul 2>&1
echo where mvn exit=%errorlevel%>>"%LOG%"
if errorlevel 1 goto :MAVEN_MISSING

echo Maven detected.>>"%LOG%"
call mvn -v
echo call mvn -v exit=%errorlevel%>>"%LOG%"
if errorlevel 1 goto :FAIL

echo.
echo Building project internals...
echo Building project internals...>>"%LOG%"

call mvn -f "project_internals\pom.xml" -DskipTests clean package dependency:copy-dependencies
echo mvn build exit=%errorlevel%>>"%LOG%"
if errorlevel 1 goto :FAIL

echo.
echo Build complete.
echo Build complete.>>"%LOG%"
goto :DONE

:MAVEN_MISSING
echo.
echo [ERROR] Maven not found on PATH.
echo [ERROR] Maven not found on PATH.>>"%LOG%"
pause
goto :DONE

:FAIL
echo.
echo (Build did not complete.)
echo (Build did not complete.)>>"%LOG%"
echo See log: "%LOG%"
pause
goto :DONE

:DONE
echo.
echo Done. Log: "%LOG%"
pause
endlocal
exit /b 0