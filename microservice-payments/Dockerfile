# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-payments-1.0.0.jar microservice-payments.jar

# Exponer el puerto en el contenedor (8091, según tu configuración)
EXPOSE 8091

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-payments.jar"]