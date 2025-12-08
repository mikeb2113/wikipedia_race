#This shell file is to automatically compile java files through maven! This is for UNIX file systems! If you are running
#Windows, then you can access these files through database_control_menu.bat instead!
#If you are on a UNIX system (not windows), then you may make this file executable with the command "chmod +x database_control_menu.sh".
#Then you can execute it with ./database_control_menu.sh

#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"   # Ensure script runs from project root

while true; do
    echo ""
    echo "========= Wikipedia Race Menu ========="
    echo "1) Run App (via Maven)"
    echo "2) Print DuckDB contents (via DbInspector)"
    echo "3) Exit"
    echo "========================================"
    read -p "Enter choice: " choice

    case "$choice" in
        1)
            echo "🚀 Running com.example.App via Maven..."
            mvn -f project_internals/pom.xml -q exec:java -Dexec.mainClass=com.example.App
            ;;
        2)
            echo "📄 Printing DuckDB contents with DbInspector..."
            # Assumes you've already run:
            # mvn -f project_internals/pom.xml -DskipTests package dependency:copy-dependencies
            java -cp "project_internals/target/classes:project_internals/target/dependency/*" com.example.DbInspector
            ;;
        3)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option."
            ;;
    esac
done