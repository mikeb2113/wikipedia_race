::This file is the access database module files. This is made for windows file systems.
::If you are running windows, you can execute this code by simply double clicking it (or running run.bat in the terminal)

@echo off
cd /d "%~dp0"
cd project_internals

echo Building project...
mvn -q clean package

echo Running entry point...
mvn -q exec:java -Dexec.mainClass=com.example.EntryPoint