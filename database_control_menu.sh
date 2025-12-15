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
    echo "2) Run GUI locally (JavaFX)"
    echo "3) Run server locally via Maven (no Docker)"
    echo "4) Print DuckDB contents (via DbInspector)"
    echo "5) Build WikiLink graph"
    echo "6) Stop Docker server & Exit"
    echo "========================================"
    read -p "Enter choice: " choice

  case "$choice" in
        1)
            chmod +x findport.sh
            port=$(./findport.sh)
            echo "found port: $port"

            echo "$port" > .wikirace_port
            echo "wrote port to $(pwd)/.wikirace_port"

            echo "🧱 Building JAR..."
            (cd project_internals && mvn -q -DskipTests package)

            echo "🐳 Building Docker image: ${IMAGE_NAME}..."
            docker build -t "${IMAGE_NAME}" project_internals

            echo "🚀 Running Docker container '${CONTAINER_NAME}' on port $port..."
            mkdir -p data
            docker run --rm \
              -p $port:8080 \
              -v "$(pwd)/data:/app/data" \
              --name "${CONTAINER_NAME}" \
              "${IMAGE_NAME}"
            ;;
        2)
        echo "🖥️  Run GUI + Docker server (like option 1, but automated)"

        # find a free host port and start the docker server in background
        chmod +x findport.sh
        port=$(./findport.sh)
        echo "found port: $port"

        echo "$port" > .wikirace_port
        echo "wrote port to $(pwd)/.wikirace_port"

        echo "🧱 Building JAR..."
        (cd project_internals && mvn -q -DskipTests package)

        echo "🐳 Building Docker image: ${IMAGE_NAME}..."
        docker build -t "${IMAGE_NAME}" project_internals

        echo "🚀 Starting Docker container '${CONTAINER_NAME}' on port $port (background)..."
        mkdir -p data
        docker run --rm -d \
            -p $port:8080 \
            -v "$(pwd)/data:/app/data" \
            --name "${CONTAINER_NAME}" \
            "${IMAGE_NAME}"

        # wait until container is actually running
        echo "⏳ Waiting for container to be up..."
        for i in {1..30}; do
            docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$" && break
            sleep 0.2
        done

        if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
            echo "❌ Server container failed to start."
            docker logs "${CONTAINER_NAME}" 2>/dev/null || true
            exit 1
        fi

        echo "✅ Server running. Launching GUI..."
        # IMPORTANT: your GUI must read .wikirace_port OR you hardcode ws://localhost:$port in GUI config
        mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=ui.ScreenTestApp || true

        echo "🛑 Stopping Docker container '${CONTAINER_NAME}'..."
        docker stop "${CONTAINER_NAME}" >/dev/null 2>&1 || true
        echo "Done."
        ;;
        3)
            echo "🚀 Running server (com.example.server.App) via Maven (host JVM)..."
            mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=com.example.server.App
            ;;
        4)
            echo "📄 Printing DuckDB contents with DbInspector..."
            java -cp "project_internals/target/classes:project_internals/target/dependency/*" \
              com.example.persistence.DbInspector
            ;;
        5)
            echo "🛠️ Building WikiLink Graph..."
            java -cp "project_internals/target/classes:project_internals/target/dependency/*" \
              com.example.persistence.BuildGameGraphTool 2 5 3 100
            ;;
        6)
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