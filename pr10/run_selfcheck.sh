#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' > out/sources.txt
javac -encoding UTF-8 -d out @out/sources.txt
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr07.SelfCheck "$@"
