# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-books-1.0.0.jar microservice-books.jar

# Exponer el puerto en el contenedor (8088, según tu configuración)
EXPOSE 8088

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-books.jar"]
