# Statikk.gg - Video Game Statistics Tracking Microservices Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2023.0.3](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Plataforma distribuida de alto rendimiento basada en microservicios para el registro, ingesta masiva (Excel/CSV), procesamiento analítico por lotes y visualización centralizada de estadísticas de videojuegos.

---

## 🏛️ 1. Arquitectura del Sistema

```
                        [ Cliente Web / Móvil ]
                                  │
                                  ▼
               ┌──────────────────────────────────────┐
               │     ms-gateway (Spring Cloud)        │ :8080
               │  - Filtro Global JWT & Seguridad     │
               │  - Rate Limiting con Redis           │
               └──────────────────┬───────────────────┘
                                  │
      ┌──────────────┬────────────┼────────────┬─────────────┬─────────────┐
      ▼              ▼            ▼            ▼             ▼             ▼
┌───────────┐  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌──────────────┐
│  ms-auth  │  │ ms-player │ │ms-matches │ │ ms-stats  │ │ ms-notif  │ │ms-excel-parse│
│   :8081   │  │   :8082   │ │   :8083   │ │   :8084   │ │   :8086   │ │    :8085     │
│ (auth_db) │  │(player_db)│ │ (match_db)│ │ (stats_db)│ │ (notif_db)│ │  Apache POI  │
└─────┬─────┘  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └─────┬─────┘ └──────┬───────┘
      │              │             │             │             │              │
      └──────────────┴─────────────┴──────┬──────┴─────────────┴──────────────┘
                                          │
                     ┌────────────────────▼───────────────────┐
                     │         RabbitMQ Event Broker          │ :5672
                     │  - excel.matches.imported              │
                     │  - matches.updated                     │
                     │  - stats.calculated                    │
                     │  - player.game.deleted                 │
                     └────────────────────────────────────────┘
```

---

## 📦 2. Catálogo de Microservicios y Stack Tecnológico

| ID | Microservicio | Directorio | Puerto | Base de Datos (MySQL) | Stack Tecnológico Principal |
|:---|:---|:---|:---:|:---:|:---|
| **MS-01** | `ms-gateway` | `api-gateway/` | `:8080` | — | Spring Cloud Gateway · Reactive WebFlux · Redis Rate Limiter · Global JWT Filter |
| **MS-02** | `ms-registry` | `registry/` | `:8761` | — | Spring Cloud Netflix Eureka Server · Service Discovery |
| **MS-03** | `ms-auth` | `auth/` | `:8081` | `auth_db` | Spring Security · JJWT · BCrypt · Spring Data JPA · Flyway |
| **MS-04** | `ms-player` | `player/` | `:8082` | `player_db` | Spring Data JPA · Redis Cache · OpenFeign · RabbitMQ · Flyway |
| **MS-05** | `ms-matches` | `matches/` | `:8083` | `match_db` | Spring Data JPA · RabbitMQ AMQP · Flyway Migration |
| **MS-06** | `ms-stats` | `stats/` | `:8084` | `stats_db` | Spring Batch · Redis Cache · CQRS · OpenFeign · RabbitMQ · Flyway |
| **MS-07** | `ms-excel-parser` | `excel-parser/` | `:8085` | — | Apache POI (`poi-ooxml`) · Spring Web · RabbitMQ Publisher |
| **MS-08** | `ms-notification` | `notification/` | `:8086` | `notification_db` | Spring WebSocket (STOMP) · JavaMailSender · Spring AMQP · Flyway |

---

## 🎯 3. Matriz de Trazabilidad de Requisitos

### Requisitos Funcionales (RF)
* **RF-01 · Registrar Usuarios (`ms-auth`)**: `POST /api/auth/register` - Valida formato de correo, verifica unicidad, aplica hashing BCrypt y almacena en `auth_db`.
* **RF-02 · Ingresar Usuarios (`ms-auth`)**: `POST /api/auth/login` - Autenticación con verificación BCrypt y emisión de token JWT firmado.
* **RF-03 · Ingresar Videojuego (`ms-player`)**: `POST /api/players/{userId}/games` - Valida nombre no vacío y asocia el videojuego al perfil del jugador.
* **RF-04 · Carga Masiva de Estadísticas vía Excel (`ms-excel-parser` / `ms-matches`)**: `POST /api/excel/upload` - Ingesta de planillas `.xlsx` o `.csv` con Apache POI, cálculo de duraciones y emisión asíncrona a RabbitMQ.
* **RF-05 · Visualizar Métricas y Promedios (`ms-stats`)**: `GET /api/stats/dashboard/{userId}` y `GET /api/stats/user/{userId}/game/{gameId}` - Panel con ratio V/D, horas acumuladas y KDA promedio con caché en Redis.
* **RF-06 · Actualizar Estadísticas (`ms-matches` / `ms-stats`)**: `PUT /api/matches/{id}` y `POST /api/stats/recalculate/...` - Reemplaza valores y recalcula métricas.
* **RF-07 · Eliminar Registro de Videojuego (`ms-player` / `ms-matches` / `ms-stats`)**: `DELETE /api/players/{userId}/games/{gameId}` - Borra la relación del juego y emite evento RabbitMQ `player.game.deleted` para purgar historial y estadísticas.

### Requisitos No Funcionales (RNF)
* **RNF-01 · Rendimiento**: Tiempos de respuesta inferiores a 2 segundos mediante caché distribuida Redis y procesamiento en lote Spring Batch.
* **RNF-02 · Seguridad**: Hashing de contraseñas con BCrypt sin texto plano, tokens JWT RSA/HMAC y validación estricta de tipos MIME en subida de archivos.
* **RNF-03 · Escalabilidad**: Arquitectura desacoplada en microservicios comunicados vía colas RabbitMQ y descubrimiento dinámico Eureka.
* **RNF-04 · Disponibilidad**: Tolerancia a fallos con fallbacks declarativos Feign y health checks en todos los servicios.
* **RNF-05 · Integridad de Datos**: Validación exhaustiva de cabeceras y celdas en hojas Excel/CSV; rechazo transaccional (`CorruptDataException`) en caso de datos inválidos.

---

## 🚀 4. Guía de Ejecución

### Opción A: Despliegue Completo con Docker Compose
```bash
# Construir e iniciar todos los servicios e infraestructura (MySQL, Redis, RabbitMQ, Mailhog)
docker-compose up --build -d

# Verificar el estado de los contenedores
docker-compose ps
```

### Opción B: Ejecución Local con Maven
1. **Compilar proyecto padre y todos los módulos**:
   ```bash
   mvn clean install -DskipTests
   ```
2. **Ejecutar pruebas unitarias e integración de toda la suite**:
   ```bash
   mvn test
   ```

---

## 📡 5. Colección de Endpoints Principales (API Gateway :8080)

### Autenticación (`/api/auth`)
* `POST /api/auth/register` - Registro de usuario (RF-01)
* `POST /api/auth/login` - Login y obtención de JWT (RF-02)
* `POST /api/auth/validate` - Validación de token JWT

### Jugadores y Videojuegos (`/api/players`)
* `POST /api/players/profile` - Crear/actualizar perfil
* `GET /api/players/{userId}` - Obtener perfil del jugador
* `POST /api/players/{userId}/games` - Asociar videojuego (RF-03)
* `GET /api/players/{userId}/games` - Listar videojuegos asociados
* `DELETE /api/players/{userId}/games/{gameId}` - Eliminar videojuego e historial (RF-07)

### Partidas e Historial (`/api/matches`)
* `POST /api/matches` - Crear partida manual
* `GET /api/matches/user/{userId}/game/{gameId}` - Listar partidas por juego
* `PUT /api/matches/{id}` - Actualizar partida (RF-06)
* `DELETE /api/matches/{id}` - Eliminar partida

### Estadísticas y Métricas (`/api/stats`)
* `GET /api/stats/dashboard/{userId}` - Panel centralizado de métricas y promedios (RF-05)
* `GET /api/stats/user/{userId}/game/{gameId}` - Métricas específicas de un juego (RF-05)
* `POST /api/stats/recalculate/user/{userId}/game/{gameId}` - Forzar recálculo (RF-06)
* `POST /api/stats/batch/run` - Ejecutar Job de agregación masiva con Spring Batch

### Ingesta Excel / CSV (`/api/excel`)
* `POST /api/excel/upload` - Carga masiva de planilla `.xlsx` / `.csv` (RF-04)
* `GET /api/excel/template` - Descargar plantilla oficial `.xlsx`

### Notificaciones y WebSocket (`/api/notifications`)
* `GET /api/notifications/user/{userId}` - Historial de alertas
* `PUT /api/notifications/{id}/read` - Marcar como leída
* `WS /ws` - Endpoint WebSocket STOMP (`/topic/notifications/{userId}`)
