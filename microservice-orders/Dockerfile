# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-orders-1.0.0.jar microservice-orders.jar

# Exponer el puerto en el contenedor (8090, según tu configuración)
EXPOSE 8090

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-orders.jar"]