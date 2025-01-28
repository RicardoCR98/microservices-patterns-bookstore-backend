# Microservices Patterns Bookstore - Backend

Este repositorio contiene el código de un proyecto de arquitectura de microservicios para la gestión de una librería en línea, desarrollado en **Java** con **Spring Boot** y organizado mediante **Maven**. El proyecto aplica principios y patrones arquitectónicos clave (por ejemplo, **API Gateway**, **Circuit Breaker**, **Externalized Configuration**, **Access Token**, **Remote Procedure Invocation** y **Transactional Outbox**) para demostrar la construcción de sistemas distribuidos, escalables y resilientes.

---

## Estructura de los Microservicios

La solución se ha dividido en varios directorios, cada uno representando un microservicio independiente:

1. **microservice-auth**  
   - Responsable de la autenticación y la autorización mediante tokens JWT (Access Token Pattern).  
   - Emite y valida credenciales de usuario, gestionando diferentes roles (`USER`, `ADMIN`).

2. **microservice-users**  
   - Centraliza la información de usuarios: perfiles, direcciones y otros datos relevantes.  
   - Expone endpoints protegidos y consultas para la administración.

3. **microservice-books**  
   - Ofrece la lógica para gestionar el catálogo de libros (registro, búsqueda, filtrado, portadas).  
   - Asegura la separación de funcionalidades relativas a productos.

4. **microservice-orders**  
   - Maneja la creación, actualización y consulta de órdenes de compra.  
   - Implementa **Circuit Breaker** y **RPI** para comunicarse con el servicio de pagos, y **Transactional Outbox** para garantizar la consistencia de eventos y datos.

5. **microservice-payments**  
   - Procesa pagos (por ejemplo, con PayPal).  
   - Responde a solicitudes del servicio de órdenes y está protegido con **Circuit Breaker** en caso de fallos recurrentes.

6. **microservice-notifications**  
   - Suscrito a la cola de eventos generada por el _Outbox_ del servicio de órdenes.  
   - Envía correos electrónicos de confirmación y notificaciones a los usuarios.

7. **microservice-config**  
   - Permite la externalización de la configuración (_Externalized Configuration Pattern_), de modo que cada servicio recupere parámetros sin necesidad de recompilarlos.  
   - Simplifica el mantenimiento y la adaptabilidad a entornos distintos (desarrollo, pruebas, producción).

8. **microservice-eureka**  
   - Actúa como servidor de descubrimiento de servicios (`Eureka Server`).  
   - Facilita el registro y la localización dinámica de cada microservicio.

9. **microservice-gateway**  
   - Implementa el **API Gateway Pattern**, ruteando peticiones desde el cliente hacia los servicios apropiados.  
   - Integra características como autenticación, autorización y posibles configuraciones de _Circuit Breaker_ global.

---

## Principales Patrones Arquitectónicos

- **Access Token Pattern**  
  Empleado en el microservicio de autenticación para la emisión de tokens JWT y la autorización de peticiones.

- **API Gateway Pattern**  
  Se concentra en `microservice-gateway`, ofreciendo un punto de acceso único al ecosistema de microservicios.

- **Circuit Breaker**  
  Asegura la resiliencia ante fallas en servicios externos (principalmente en la integración con pagos).

- **Externalized Configuration**  
  Centraliza la configuración en `microservice-config`, minimizando la necesidad de recompilación ante cambios en propiedades.

- **Remote Procedure Invocation (RPI)**  
  Implementado con _Feign_ para llamadas entre microservicios (p. ej., comunicación entre `orders` y `payments`).

- **Transactional Outbox**  
  Garantiza la consistencia de datos y la publicación confiable de eventos; `microservice-orders` registra órdenes y publica notificaciones en RabbitMQ, consumidas por `microservice-notifications`.

---

## Requisitos y Configuración

1. **Java 21** o superior (se recomienda revisar el `pom.xml` padre para saber la versión exacta).
2. **Maven** para la gestión de dependencias y el ciclo de construcción.
3. **Spring Boot** como _framework_ base de cada microservicio.
4. **RabbitMQ** para la mensajería (eventos _Outbox_).
5. **Eureka** como servidor de descubrimiento (`microservice-eureka`).
6. **Config Server** (`microservice-config`) para manejar la configuración externalizada.
7. (Opcional) **Docker** para contenerizar cada servicio (puede configurarse a conveniencia).
8. **PostgreSQL 17** o superior 

La configuración de puertos, credenciales, URLs y otras propiedades específicas se gestiona a través de `application.yml`, los cuales se unifican en el servicio de configuración (`microservice-config`).

---

## Ejecución y Uso

1. **Iniciar el Config Server**: (`microservice-config`), asegurándose de que posea acceso a la configuración requerida.
2. **Iniciar Eureka Server**: (`microservice-eureka`) para permitir el registro dinámico de servicios.
3. **Levantar cada microservicio**: (`auth`, `users`, `books`, `orders`, `payments`, `notifications`, `gateway`) en orden flexible, según dependa la topología.
4. **Verificar registro**: Revisar la consola de Eureka para confirmar que todos los microservicios aparecen como _UP_.
5. **Probar Endpoints**: Utilizar herramientas como _Postman_ o _cURL_ para consumir la API (normalmente expuesta por `microservice-gateway` en un puerto unificado).

Para mayor detalle sobre rutas y funcionalidades, cada microservicio incluye su propio `/swagger-ui.html` (si se activó) o documentación en `README.md`/`docs` internos.

---

## Características Destacadas

- **Seguridad y Roles**: Los servicios diferencian credenciales y roles (`USER` y `ADMIN`) gracias al **Access Token Pattern**.
- **Escalabilidad y Resiliencia**: El desacoplamiento de servicios, junto con **Circuit Breaker** y **API Gateway**, mejora la resiliencia ante sobrecargas y fallos.
- **Facilidad de Configuración**: **Externalized Configuration** evita la recompilación de cada microservicio con cambios de parámetros o entornos.
- **Procesamiento de Mensajería**: **Transactional Outbox** + **RabbitMQ** aseguran la entrega confiable de eventos y minimizan problemas de consistencia.
- **Comunicación Simplificada**: **RPI** (Feign) reduce la complejidad de llamadas REST entre microservicios, inyectando automáticamente credenciales y manejando fallbacks.

---

## Contribución y Mantenimiento

El proyecto está planteado para fines académicos e ilustrativos, demostrando la adopción de patrones en microservicios. Se recomienda:

- **Mantener el control de versiones** en cada microservicio, favoreciendo una estructura clara de `branches` o `tags`.
- **Documentar** cualquier configuración adicional en la carpeta `docs` o en el archivo `README.md` local de cada microservicio.
- **Realizar pruebas continuas** (funcionales, de carga y de integración) para asegurar la estabilidad al agregar nuevas funcionalidades.

---


