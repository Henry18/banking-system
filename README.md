# Banking System - Microservicios (Personas/Clientes y Cuentas/Movimientos)

## Descripción
Proyecto de arquitectura basada en microservicios que implementa un sistema bancario simplificado.
Incluye dos microservicios:
1. **Personas-Clientes** → gestiona información personal y de clientes.
2. **Cuentas-Movimientos** → administra cuentas bancarias, operaciones y reportes financieros.

---

## Estructura del proyecto

```
/services
├── personas-clientes/
│ ├── Dockerfile
│ ├── src/main/java/com/bank/personasclientes/
│ └── ...
├── cuentas-movimientos/
│ ├── Dockerfile
│ ├── src/main/java/com/bank/cuentasmovimientos/
│ └── ...
├── docker
│ └── docker-compose.yml
└── postman
  └── collection.json
```

---

## Tecnologías
- Java 17 + Spring Boot 3.3.4
- PostgreSQL 15
- Docker / Docker Compose
- JPA + Hibernate
- Swagger / OpenAPI
- JUnit 5 + MockMvc

---

## Ejecución del proyecto

### Clonar el repositorio
```bash
git clone https://github.com/tuusuario/banking-system.git
cd banking-system
```

### Construir y levantar contenedores
```bash
docker compose -f services/docker/docker-compose.yml up --build -d
```

Esto levantará:

```
| Servicio | Puerto | Descripción |
|-----------|---------|--------------|
| personas-clientes | 8080 | Gestión de clientes y personas |
| cuentas-movimientos | 8081 | Gestión de cuentas, movimientos y reportes |
| postgres-personas | 5433 | Base de datos personas |
| postgres-cuentas | 5434 | Base de datos cuentas |
```
### Verificación
Swagger disponible en:
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## Pruebas

### Unitarias
```bash
mvn test
```

### Integración
Ejemplo de test de integración (`ReporteEstadoCuentaIntegrationTest`):
- Valida el flujo completo: creación de movimientos → consolidación → generación de reporte de estado de cuenta.

---

## Colección Postman

- Archivo: `banking-system.postman_collection.json`
- Incluye ejemplos de:
    - `POST /clientes`
    - `GET /clientes/{id}`
    - `POST /cuentas`
    - `POST /movimientos`
    - `GET /reportes/estado-cuenta?clienteId=...`

Importar en Postman → “Import” → seleccionar archivo JSON.

---

## Despliegue en contenedores

Cada microservicio tiene su propio Dockerfile:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

El `docker-compose.yml` centraliza el orquestamiento:

```yaml
version: "3.9"
services:
  postgres-personas:
    image: postgres:15
    environment:
      POSTGRES_DB: personasdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"

  postgres-cuentas:
    image: postgres:15
    environment:
      POSTGRES_DB: cuentasdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5434:5432"

  personas-clientes:
    build: ./personas-clientes
    ports:
      - "8080:8080"
    depends_on:
      - postgres-personas

  cuentas-movimientos:
    build: ./cuentas-movimientos
    ports:
      - "8081:8080"
    depends_on:
      - postgres-cuentas
```

---

## Autor
**Henry Martínez**  
Desarrollador Senior Fullstack  
[GitHub](https://github.com/henry-martinez) | [LinkedIn](https://linkedin.com/in/henry-martinez)
