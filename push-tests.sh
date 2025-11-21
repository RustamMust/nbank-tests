#!/bin/bash

# -----------------------
# Настройки
# -----------------------
IMAGE_NAME=nbank-tests
TAG=latest

# Docker Hub логин (нужно указать свой)
DOCKERHUB_USERNAME=docker-hub-username

# Токен можно хранить в переменной окружения DOCKERHUB_TOKEN
# Например: export DOCKERHUB_TOKEN=ваш_токен
if [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "Ошибка: переменная окружения DOCKERHUB_TOKEN не установлена!"
  exit 1
fi

# -----------------------
# Логин в Docker Hub
# -----------------------
echo ">>> Логин в Docker Hub"
echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
if [ $? -ne 0 ]; then
  echo "Ошибка: не удалось залогиниться в Docker Hub"
  exit 1
fi

# -----------------------
# Тегирование образа
# -----------------------
echo ">>> Тегирование образа"
docker tag "$IMAGE_NAME" "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
if [ $? -ne 0 ]; then
  echo "Ошибка: не удалось тегировать образ"
  exit 1
fi

# -----------------------
# Отправка образа в Docker Hub
# -----------------------
echo ">>> Отправка образа в Docker Hub"
docker push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
if [ $? -ne 0 ]; then
  echo "Ошибка: не удалось отправить образ в Docker Hub"
  exit 1
fi

echo ">>> Готово! Образ доступен как: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"


# Выполните скрипт
# chmod +x push-tests.sh
# ./push-tests.sh

# Проверьте, что образ появился в вашем аккаунте На hub.docker.com → ваш профиль → Repositories → nbank-tests.
