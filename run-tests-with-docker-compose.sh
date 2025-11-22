#!/bin/bash

set -e  # остановка при любой ошибке

# Конфигурация
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}  # аргумент запуска: api или ui
TIMESTAMP=$(date +"%Y_%m-%d_%H_%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# Путь к docker-compose.yml
DOCKER_COMPOSE_FILE=./infra/docker_compose/docker-compose.yml

echo ">>> Поднимаем тестовое окружение через Docker Compose"

docker compose -f "$DOCKER_COMPOSE_FILE" up -d backend frontend selenoid selenoid-ui

# Ждём, чтобы сервисы успели подняться
echo ">>> Ждём 10 секунд, чтобы сервисы были готовы..."
sleep 10

# Создаем директории для результатов
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

echo ">>> Сборка Docker образа тестов"
docker build -t $IMAGE_NAME .

echo ">>> Запуск тестов"
docker run --rm \
  --network nbank-network \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://backend:4111 \
  -e UIBASEURL=http://frontend:80 \
  -e SELENOID_URL=http://selenoid:4444 \
  -e SELENOID_UI_URL=http://selenoid-ui:8080 \
  $IMAGE_NAME

echo ">>> Тесты завершены"
echo "Логи: $TEST_OUTPUT_DIR/logs"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Репорт: $TEST_OUTPUT_DIR/report"

echo ">>> Останавливаем тестовое окружение"
docker compose -f "$DOCKER_COMPOSE_FILE" down

echo ">>> Всё готово!"


# Скрипт запускает все API и UI тесты в тестовом окружении, используя Docker Compose