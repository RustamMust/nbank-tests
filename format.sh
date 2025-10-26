#!/usr/bin/env bash

# === Settings ===
# Path to the JAR (use the full path if it's not in the project root)
JAR_PATH="./google-java-format-1.23.0-all-deps.jar"

# === Check if the file exists ===
if [ ! -f "$JAR_PATH" ]; then
  echo "❌ File $JAR_PATH not found"
  echo "➡️  Download it from:"
  echo "https://github.com/google/google-java-format/releases/download/v1.23.0/google-java-format-1.23.0-all-deps.jar"
  exit 1
fi

# === Formatting ===
echo "🧼 Formatting Java files..."
find src -name "*.java" | xargs java -jar "$JAR_PATH" --replace

if [ $? -eq 0 ]; then
  echo "✅ Formatting completed successfully!"
else
  echo "⚠️ An error occurred during formatting."
fi
