#This shell file is to automatically access database module files! This is for UNIX file systems! If you are running
#Windows, then you can access these files through run.bat instead!
#If you are on a UNIX system (not windows), then you may make this file executable with the command "chmod +x run.sh".
#Then you can execute it with ./run.sh

#!/usr/bin/env bash
set -e

echo "🔧 Building project external..."
mvn -e clean package

# Go to project root (this file's directory)
cd "$(dirname "$0")"

# Go into the Maven module that has your Java code
cd project_internals

echo "🔧 Building project internals..."
mvn -e clean package

# If your main class is com.example.App:
mvn -q exec:java -Dexec.mainClass=com.example.App

cd ..

#echo "🚀 Running entry point..."