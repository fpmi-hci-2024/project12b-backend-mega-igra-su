# Dockerfile
FROM openjdk:17-jdk-slim

# Установка рабочей директории
WORKDIR /app

# Копирование JAR-файла
COPY out/artifacts/app_jar/app.jar app.jar

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
