# Базовый докер образ
FROM maven:3.9.9-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# Работаем из папки /app
WORKDIR /app

# Копируем pom.xml
COPY pom.xml .

# Загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# Копируем весь проект
COPY . .

# Теперь внутри есть зависимости, весь проект и мы готовы запускать тесты

USER root

# mvn test -P api
# mvn -DskipTests=true surfire-report:report
# лог выводился не в консоль, а в файл
# bash file
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
   } > /app/logs/run.log 2>&1"