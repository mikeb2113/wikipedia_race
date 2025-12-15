@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM HARD DEBUG STOP
echo.
echo [DEBUG] Batch file opened successfully.
echo [DEBUG] Press any key to continue execution...
pause

chcp 65001 >nul
set "FAIL=0"

echo [DEBUG] Started: %~f0
echo [DEBUG] Script dir: %~dp0
echo [DEBUG] CWD before cd: %cd%

REM Ensure script runs from repo root
cd /d "%~dp0"
if errorlevel 1 (
  echo [FATAL] Failed to cd into script directory: "%~dp0"
  set "FAIL=1"
  goto END
)

echo [DEBUG] Repo root: %cd%

REM Verify required files
if not exist "%~dp0findport.bat" (
  echo [FATAL] Missing findport.bat at "%~dp0findport.bat"
  set "FAIL=1"
  goto END
)

if not exist "project_internals\pom.xml" (
  echo [FATAL] Missing project_internals\pom.xml - are you in the repo root?
  set "FAIL=1"
  goto END
)

REM Verify toolchain
where mvn >nul 2>&1
if errorlevel 1 (
  echo [FATAL] mvn not found on PATH
  set "FAIL=1"
  goto END
)

where java >nul 2>&1
if errorlevel 1 (
  echo [FATAL] java not found on PATH
  set "FAIL=1"
  goto END
)

where docker >nul 2>&1
if errorlevel 1 (
  echo [FATAL] docker not found on PATH
  set "FAIL=1"
  goto END
)

docker info >nul 2>&1
if errorlevel 1 (
  echo [FATAL] Docker not usable. Start Docker Desktop and re-run.
  set "FAIL=1"
  goto END
)

echo [DEBUG] Toolchain OK.

set "IMAGE_NAME=docker-java-server"
set "CONTAINER_NAME=wikirace_server"

:MENU
echo.
echo ========= Wikipedia Race Menu =========
echo 1^) Build ^& Run server in Docker
echo 2^) Run GUI locally (JavaFX) + start Docker server
echo 3^) Run App locally via Maven (no Docker)
echo 4^) Print DuckDB contents (DbInspector)
echo 5^) Build WikiLink graph
echo 6^) Stop Docker server ^& Exit
echo =======================================
set /p choice=Enter choice:

if "%choice%"=="1" goto DOCKER
if "%choice%"=="2" goto GUI_DOCKER
if "%choice%"=="3" goto MAVEN
if "%choice%"=="4" goto INSPECT
if "%choice%"=="5" goto GRAPH
if "%choice%"=="6" goto EXIT

echo Invalid option.
goto MENU

:DOCKER
echo [DEBUG] Finding free port...
call "%~dp0findport.bat" 8080 1
if errorlevel 1 (
  echo [ERROR] findport.bat failed
  set "FAIL=1"
  goto END
)

echo [DEBUG] FOUND_PORT="%FOUND_PORT%"
if not defined FOUND_PORT (
  echo [ERROR] FOUND_PORT was empty
  set "FAIL=1"
  goto END
)

set "PORT=%FOUND_PORT%"
echo [INFO] Using port %PORT%

echo [BUILD] Building project (package + dependencies)...
pushd project_internals >nul
if errorlevel 1 (
  echo [ERROR] Missing project_internals folder
  set "FAIL=1"
  goto END
)

call mvn -U -DskipTests package dependency:copy-dependencies -e -X
if errorlevel 1 (
  echo [ERROR] Maven build failed
  popd >nul
  set "FAIL=1"
  goto END
)
popd >nul

echo [DOCKER] Building image %IMAGE_NAME%...
docker build -t "%IMAGE_NAME%" project_internals
if errorlevel 1 (
  echo [ERROR] Docker build failed
  set "FAIL=1"
  goto END
)

echo [DOCKER] Running container "%CONTAINER_NAME%" on port %PORT%...
docker run --rm ^
  -p %port%:8080 ^
  -v "%cd%\data:/app/data" ^
  --name "%CONTAINER_NAME%" ^
  "%IMAGE_NAME%"echo [DEBUG] docker run exited (errorlevel=%errorlevel%)
goto MENU

:GUI_DOCKER
echo [GUI+DOCKER] Starting Docker server (detached) then launching GUI...

REM --- Find free port (same as DOCKER) ---
echo [DEBUG] Finding free port...
call "%~dp0findport.bat" 8080 1
if errorlevel 1 (
  echo [ERROR] findport.bat failed
  set "FAIL=1"
  goto END
)

if not defined FOUND_PORT (
  echo [ERROR] FOUND_PORT was empty
  set "FAIL=1"
  goto END
)

set "PORT=%FOUND_PORT%"
echo [INFO] Using port %PORT%

REM Write port for GUI to read
echo %PORT%> .wikirace_port
echo [INFO] Wrote port to %cd%\.wikirace_port

REM Ensure data directory exists
if not exist "data" mkdir data

REM --- Build jar ---
echo [BUILD] Building project (package + dependencies)...
pushd project_internals >nul
call mvn -q -DskipTests package dependency:copy-dependencies
if errorlevel 1 (
  echo [ERROR] Maven build failed
  popd >nul
  set "FAIL=1"
  goto END
)
popd >nul

REM --- Build docker image ---
echo [DOCKER] Building image %IMAGE_NAME%...
docker build -t "%IMAGE_NAME%" project_internals
if errorlevel 1 (
  echo [ERROR] Docker build failed
  set "FAIL=1"
  goto END
)

REM --- Start docker container detached ---
echo [DOCKER] Starting container "%CONTAINER_NAME%" on port %PORT% (detached)...
docker rm -f "%CONTAINER_NAME%" >nul 2>&1

docker run --rm -d ^
  -p %PORT%:8080 ^
  -v "%cd%\data:/app/data" ^
  --name "%CONTAINER_NAME%" ^
  "%IMAGE_NAME%"

if errorlevel 1 (
  echo [ERROR] Docker run failed
  set "FAIL=1"
  goto END
)

REM Small wait so WS is ready (deadline-simple)
timeout /t 1 /nobreak >nul

REM --- Run GUI ---
echo [GUI] Running ui.ScreenTestApp via Maven...
call mvn -f project_internals\pom.xml -q exec:java -Dexec.mainClass=ui.ScreenTestApp

echo [GUI] GUI exited. Stopping Docker container...
docker stop "%CONTAINER_NAME%" >nul 2>&1

goto MENU

:MAVEN
echo [RUN] Running com.example.server.App via Maven...
call mvn -f project_internals\pom.xml exec:java -Dexec.mainClass=com.example.server.App
if errorlevel 1 (
  echo [ERROR] Maven exec:java failed
  set "FAIL=1"
  goto END
)
goto MENU

:INSPECT
echo [DB] Running DbInspector...

if not exist "project_internals\target\dependency" (
  echo [INFO] Dependencies missing - building them now...
  call mvn -f project_internals\pom.xml -DskipTests package dependency:copy-dependencies
)

call java -cp "project_internals\target\classes;project_internals\target\dependency\*" com.example.persistence.DbInspector
if errorlevel 1 (
  echo [ERROR] DbInspector failed
  set "FAIL=1"
  goto END
)
goto MENU

:GRAPH
echo [DB] Building WikiLink Graph...

if not exist "project_internals\target\dependency" (
  echo [INFO] Dependencies missing - building them now...
  call mvn -f project_internals\pom.xml -DskipTests package dependency:copy-dependencies
)

call java -cp "project_internals\target\classes;project_internals\target\dependency\*" com.example.persistence.BuildGameGraphTool 2 5 3 100
if errorlevel 1 (
  echo [ERROR] BuildGameGraphTool failed
  set "FAIL=1"
  goto END
)
goto MENU

:EXIT
echo [STOP] Stopping Docker container (if running)...
docker stop "%CONTAINER_NAME%" >nul 2>&1
echo Goodbye!
goto END

:END
echo.
echo ===== SCRIPT EXIT =====
echo FAIL=%FAIL%
echo ERRORLEVEL=%ERRORLEVEL%
echo Press any key to close...
pause
endlocal
exit /b %FAIL%