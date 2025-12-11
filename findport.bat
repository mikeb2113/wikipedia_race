@echo off
setlocal enabledelayedexpansion

REM Optional args:
REM   %1 = base port (default 8080)
REM   %2 = increment (default 1)

if "%~1"=="" (
    set "BASE_PORT=8080"
) else (
    set "BASE_PORT=%~1"
)

if "%~2"=="" (
    set "INCREMENT=1"
) else (
    set "INCREMENT=%~2"
)

set "port=%BASE_PORT%"

:check_port
REM Check if port is in use (LISTENING on TCP)
netstat -ano -p TCP | findstr /R /C:":%port% .*LISTENING" >nul 2>&1
if %errorlevel%==0 (
    REM Port is in use → bump and retry
    set /a port=%port%+%INCREMENT%
    goto :check_port
)

REM Print free port as plain number
echo %port%

endlocal
exit /b 0