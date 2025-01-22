# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-users-1.0.0.jar microservice-users.jar

# Exponer el puerto en el contenedor (8089, según tu configuración)
EXPOSE 8089

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-users.jar"]