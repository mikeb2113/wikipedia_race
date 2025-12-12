@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Args: base_port increment
set "BASE_PORT=%~1"
if not defined BASE_PORT set "BASE_PORT=8080"

set "INCREMENT=%~2"
if not defined INCREMENT set "INCREMENT=1"

set "PORT=%BASE_PORT%"

where netstat >nul 2>&1 || (
  endlocal & exit /b 1
)

:CHECK_PORT
netstat -ano | findstr /I /C:":%PORT% " | findstr /I "LISTENING" >nul
if not errorlevel 1 (
  set /a PORT+=INCREMENT
  goto CHECK_PORT
)

endlocal & set "FOUND_PORT=%PORT%"
exit /b 0