# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-auth-1.0.0.jar microservice-auth.jar

# Exponer el puerto en el contenedor (8083, según tu configuración)
EXPOSE 8083

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-auth.jar"]
