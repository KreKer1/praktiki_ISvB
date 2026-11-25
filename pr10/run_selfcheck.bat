@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if not exist out mkdir out
(
  for /r src %%f in (*.java) do (
    set "source=%%f"
    echo "!source:\=/!"
  )
) > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 exit /b 1
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr07.SelfCheck %*

