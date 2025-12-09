#This shell file is to automatically compile java files through maven! This is for UNIX file systems! If you are running
#Windows, then you can access these files through database_control_menu.bat instead!
#If you are on a UNIX system (not windows), then you may make this file executable with the command "chmod +x database_control_menu.sh".
#Then you can execute it with ./database_control_menu.sh

#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"   # Ensure script runs from project root

IMAGE_NAME="docker-java-server"
CONTAINER_NAME="wikirace_server"

while true; do
    echo ""
    echo "========= Wikipedia Race Menu ========="
    echo "1) Build & Run server in Docker"
    echo "2) Run App locally via Maven (no Docker)"
    echo "3) Print DuckDB contents (via DbInspector)"
    echo "4) Stop Docker server & Exit"
    echo "========================================"
    read -p "Enter choice: " choice

    case "$choice" in
        1)
            echo "🧱 Building JAR..."
            (cd project_internals && mvn -q -DskipTests package)

            echo "🐳 Building Docker image: ${IMAGE_NAME}..."
            docker build -t "${IMAGE_NAME}" project_internals

            echo "🚀 Running Docker container '${CONTAINER_NAME}' on port 8080..."
            docker run --rm -p 8080:8080 --name "${CONTAINER_NAME}" "${IMAGE_NAME}"
            ;;
        2)
            echo "🚀 Running com.example.App via Maven (host JVM)..."
            mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=com.example.App
            ;;
        3)
            echo "📄 Printing DuckDB contents with DbInspector..."
            java -cp "project_internals/target/classes:project_internals/target/dependency/*" com.example.DbInspector
            ;;
        4)
            echo "🛑 Stopping Docker container '${CONTAINER_NAME}' (if running)..."
            docker stop "${CONTAINER_NAME}" >/dev/null 2>&1 || true
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option."
            ;;
    esac
done