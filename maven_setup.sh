#This shell file is to automatically compile java files through maven! This is for UNIX file systems! If you are running
#Windows, then you can access these files through maven_setup.bat instead!
#If you are on a UNIX system (not windows), then you may make this file executable with the command "chmod +x maven_setup.sh".
#Then you can execute it with ./maven_setup.sh

#!/usr/bin/env bash
set -e

# Go to project root (this file's directory)
cd "$(dirname "$0")"

echo "🔧 Building project internals (clean + package + copy dependencies)..."
mvn -f project_internals/pom.xml -DskipTests clean package dependency:copy-dependencies
echo "✅ Maven build complete."

#!/usr/bin/env bash
#set -e

# Go to project root (this file's directory)
#cd "$(dirname "$0")"

# Go into the Maven module that has your Java code
#cd project_internals

#echo "🔧 Building project internals..."
#mvn -e clean package

# If your main class is com.example.App:

#mvn -e exec:java -Dexec.mainClass=com.example.App
#echo "🚀 Running entry point..."
