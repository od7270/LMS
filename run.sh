#!/bin/bash
# Compiles all Java files into out/ and starts the LMS.
cd "$(dirname "$0")" || exit 1

rm -rf out
javac -d out $(find . -name "*.java" -not -path "./out/*") || exit 1
java -cp out LearningManagementSystem
