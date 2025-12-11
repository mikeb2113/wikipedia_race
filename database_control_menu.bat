::This file is  meant to give easy access to key database internals. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running database_control_menu.bat in the terminal)

@echo off
setlocal enabledelayedexpansion

REM Ensure script runs from its own directory
cd /d "%~dp0"

set IMAGE_NAME=docker-java-server
set CONTAINER_NAME=wikirace_server

:menu
echo.
echo ========= Wikipedia Race Menu =========
echo 1^) Build ^& Run server in Docker
echo 2^) Run App locally via Maven ^(no Docker^)
echo 3^) Print DuckDB contents ^(via DbInspector^)
echo 4^) Stop Docker server ^& Exit
echo ========================================
choice /c 1234 /n /m "Enter choice: "

if errorlevel 4 goto stop_docker
if errorlevel 3 goto db_print
if errorlevel 2 goto run_maven
if errorlevel 1 goto docker_run

goto menu

:docker_run
echo.
echo Finding free port starting at 8080...

REM Call findport.bat and capture its output into PORT
for /f "usebackq delims=" %%P in (`call "%~dp0findport.bat"`) do set "PORT=%%P"

echo Found port: %PORT%

echo 🧱 Building JAR...
pushd project_internals
call mvn -q -DskipTests package dependency:copy-dependencies
popd

echo 🐳 Building Docker image: %IMAGE_NAME% ...
docker build -t %IMAGE_NAME% project_internals

echo 🚀 Running Docker container '%CONTAINER_NAME%' on port %PORT%...
docker run --rm -p %PORT%:8080 --name %CONTAINER_NAME% %IMAGE_NAME%

goto menu

:run_maven
echo.
echo 🚀 Running com.example.App via Maven...
call mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=com.example.App
goto menu

:db_print
echo.
echo 📄 Printing DuckDB contents using DbInspector...
java -cp "project_internals/target/classes;project_internals/target/dependency/*" com.example.DbInspector
goto menu

:stop_docker
echo.
echo 🛑 Stopping Docker container '%CONTAINER_NAME%' (if running)...
docker stop %CONTAINER_NAME% >nul 2>&1
echo Goodbye!
exit /b 0