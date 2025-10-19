#!/usr/bin/env bash

# === Настройки ===
# Путь к JAR (укажи полный путь, если он не в корне проекта)
JAR_PATH="./google-java-format-1.23.0-all-deps.jar"

# === Проверка наличия файла ===
if [ ! -f "$JAR_PATH" ]; then
  echo "❌ Не найден файл $JAR_PATH"
  echo "➡️  Скачай его по ссылке:"
  echo "https://github.com/google/google-java-format/releases/download/v1.23.0/google-java-format-1.23.0-all-deps.jar"
  exit 1
fi

# === Форматирование ===
echo "🧼 Форматируем Java-файлы..."
find src -name "*.java" | xargs java -jar "$JAR_PATH" --replace

if [ $? -eq 0 ]; then
  echo "✅ Форматирование завершено успешно!"
else
  echo "⚠️ Произошла ошибка при форматировании."
fi