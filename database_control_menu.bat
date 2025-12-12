::This file is  meant to give easy access to key database internals. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running database_control_menu.bat in the terminal)

@echo off
setlocal EnableDelayedExpansion

REM Ensure script runs from project root
cd /d "%~dp0"

set IMAGE_NAME=docker-java-server
set CONTAINER_NAME=wikirace_server

:MENU
echo.
echo ========= Wikipedia Race Menu =========
echo 1^) Build ^& Run server in Docker
echo 2^) Run App locally via Maven (no Docker)
echo 3^) Print DuckDB contents (via DbInspector)
echo 4^) Build WikiLink graph
echo 5^) Stop Docker server ^& Exit
echo =======================================
set /p choice=Enter choice: 

if "%choice%"=="1" goto DOCKER
if "%choice%"=="2" goto MAVEN
if "%choice%"=="3" goto INSPECT
if "%choice%"=="4" goto GRAPH
if "%choice%"=="5" goto EXIT

echo Invalid option.
goto MENU

:DOCKER
call findport.bat
set port=%FOUND_PORT%

echo Found port: %port%
echo 🧱 Building JAR...
pushd project_internals
mvn -q -DskipTests package
popd

echo 🐳 Building Docker image: %IMAGE_NAME%...
docker build -t %IMAGE_NAME% project_internals

echo 🚀 Running Docker container '%CONTAINER_NAME%' on port %port%...
docker run --rm -p %port%:8080 --name %CONTAINER_NAME% %IMAGE_NAME%
goto MENU

:MAVEN
echo 🚀 Running com.example.server.App via Maven (host JVM)...
mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=com.example.server.App
goto MENU

:INSPECT
echo 📄 Printing DuckDB contents with DbInspector...
java -cp "project_internals\target\classes;project_internals\target\dependency\*" ^
 com.example.persistence.DbInspector
goto MENU

:GRAPH
echo 🛠️ Building WikiLink Graph...
java -cp "project_internals\target\classes;project_internals\target\dependency\*" ^
 com.example.persistence.BuildGameGraphTool 2 5 3 100
goto MENU

:EXIT
echo 🛑 Stopping Docker container '%CONTAINER_NAME%' (if running)...
docker stop %CONTAINER_NAME% >nul 2>&1
echo Goodbye!
exit /b 0
