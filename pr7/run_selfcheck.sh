#!/usr/bin/env bash
set -e
rm -rf out
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr04.SelfCheck
