# Usar una imagen base de OpenJDK 21 (puede ser slim, alpine, etc.)
FROM openjdk:21-jdk-slim

# Instalar curl
RUN apt-get update && apt-get install -y curl

# Crear directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR del microservicio. Ajusta el nombre del JAR según corresponda
COPY target/microservice-eureka-1.0.0.jar microservice-eureka.jar

# Exponer el puerto en el contenedor (8761, según tu configuración)
EXPOSE 8761

# Comando de arranque
ENTRYPOINT ["java", "-jar", "microservice-eureka.jar"]
