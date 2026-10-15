# 📚 Catálogo Completo de Endpoints - Statikk.gg

Guía de referencia y documentación de todos los endpoints de la plataforma de microservicios **Statikk.gg**, incluyendo especificaciones de cabeceras, parámetros, payloads JSON de ejemplo para peticiones `POST` y `PUT`, respuestas esperadas y ejemplos listos para `cURL` o clientes HTTP (Postman, Insomnia, Thunder Client).

---

## 🌐 Configuración General y Entornos

Todas las peticiones pueden realizarse de forma centralizada a través del **API Gateway** o accediendo directamente al microservicio correspondiente:

| Componente | Host / Puerto | Descripción |
| :--- | :--- | :--- |
| **API Gateway (Recomendado)** | `http://localhost:8080` | Punto de entrada unificado con enrutamiento dinámico, validación JWT y Rate Limiting |
| **ms-auth** | `http://localhost:8081` | Autenticación, registro, login y usuarios |
| **ms-player** | `http://localhost:8082` | Perfiles de jugador y catálogo de videojuegos |
| **ms-matches** | `http://localhost:8083` | Gestión individual e historial de partidas |
| **ms-stats** | `http://localhost:8084` | Métricas agregadas, KDA, winrate y procesamiento por lotes |
| **ms-excel-parser** | `http://localhost:8085` | Ingesta masiva y validación de archivos Excel (`.xlsx`) y CSV |
| **ms-notification** | `http://localhost:8086` | Notificaciones internas, correos (Mailhog) y WebSocket STOMP |
| **ms-registry (Eureka)** | `http://localhost:8761` | Servidor de descubrimiento de servicios |

---

## 🔐 Autenticación y Seguridad (JWT)

- **Endpoints Públicos**:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/validate`
  - `GET /api/excel/template`
  - `/actuator/**`
  - `/ws/**`
- **Endpoints Protegidos**: Todos los demás endpoints requieren el encabezado HTTP:
  ```http
  Authorization: Bearer <TU_TOKEN_JWT>
  ```
- **Formato general de peticiones con body**:
  ```http
  Content-Type: application/json
  ```

---

## 🔄 Flujo Rápido de Prueba (Happy Path)

1. Registrar usuario en **`POST /api/auth/register`**.
2. Obtener JWT en **`POST /api/auth/login`**.
3. Crear perfil de jugador en **`POST /api/players/profile`**.
4. Registrar un videojuego en **`POST /api/players/{userId}/games`**.
5. Crear partidas manuales con **`POST /api/matches`** o masivas con **`POST /api/excel/upload`**.
6. Visualizar métricas en **`GET /api/stats/dashboard/{userId}`**.

---

## 1. 🛡️ Microservicio de Autenticación (`ms-auth`)

Ruta base Gateway: `http://localhost:8080/api/auth`  
Ruta directa: `http://localhost:8081/api/auth`

---

### 1.1 Registrar Usuario (RF-01)
Crea una nueva cuenta de usuario con contraseña encriptada mediante BCrypt.

- **Método**: `POST`
- **Ruta**: `/api/auth/register`
- **Acceso**: Público
- **Headers**: `Content-Type: application/json`

#### Payload JSON (Request Body):
```json
{
  "username": "faker_god",
  "email": "faker@t1.gg",
  "password": "Password123!"
}
```

#### Validaciones del Body:
* `username`: Obligatorio (`@NotBlank`), entre 3 y 50 caracteres.
* `email`: Obligatorio (`@NotBlank`), formato de correo electrónico válido (`@Email`).
* `password`: Obligatorio (`@NotBlank`), mínimo 6 caracteres.

#### Respuesta Exitosa (`201 CREATED`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmYWtlckB0MS5nZyIsInVzZXJJZCI6MSwicm9sZSI6IlJPTEVfVVNFUiJ9...",
  "userId": 1,
  "username": "faker_god",
  "email": "faker@t1.gg",
  "role": "ROLE_USER",
  "createdAt": "2026-10-15T09:30:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "faker_god",
    "email": "faker@t1.gg",
    "password": "Password123!"
  }'
```

---

### 1.2 Iniciar Sesión / Login (RF-02)
Valida las credenciales y devuelve el token JWT para autorizar peticiones protegidas.

- **Método**: `POST`
- **Ruta**: `/api/auth/login`
- **Acceso**: Público
- **Headers**: `Content-Type: application/json`

#### Payload JSON (Request Body):
```json
{
  "email": "faker@t1.gg",
  "password": "Password123!"
}
```

#### Validaciones del Body:
* `email`: Obligatorio (`@NotBlank`), formato email.
* `password`: Obligatorio (`@NotBlank`).

#### Respuesta Exitosa (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmYWtlckB0MS5nZyIsInVzZXJJZCI6MSwicm9sZSI6IlJPTEVfVVNFUiJ9...",
  "userId": 1,
  "username": "faker_god",
  "email": "faker@t1.gg",
  "role": "ROLE_USER",
  "createdAt": "2026-10-15T09:30:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "faker@t1.gg",
    "password": "Password123!"
  }'
```

---

### 1.3 Validar Token JWT
Verifica la validez y extrae la identidad codificada en un token JWT.

- **Método**: `POST`
- **Ruta**: `/api/auth/validate?token={jwtToken}`
- **Acceso**: Público
- **Parámetro Query**:
  * `token`: Cadena del JWT a validar.

#### Respuesta Exitosa (`200 OK`):
```json
{
  "valid": true,
  "userId": 1,
  "email": "faker@t1.gg",
  "role": "ROLE_USER"
}
```

#### Ejemplo cURL:
```bash
curl -X POST "http://localhost:8080/api/auth/validate?token=eyJhbGciOi..."
```

---

### 1.4 Obtener Perfil de Usuario por ID
Consulta los datos básicos del usuario registrado en el servicio de autenticación.

- **Método**: `GET`
- **Ruta**: `/api/auth/users/{id}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "username": "faker_god",
  "email": "faker@t1.gg",
  "role": "ROLE_USER",
  "createdAt": "2026-10-15T09:30:00"
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/auth/users/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

## 2. 🎮 Microservicio de Jugadores y Juegos (`ms-player`)

Ruta base Gateway: `http://localhost:8080/api/players`  
Ruta directa: `http://localhost:8082/api/players`

---

### 2.1 Crear o Actualizar Perfil de Jugador
Crea o actualiza el perfil público, biografía, región y avatar de un usuario.

- **Método**: `POST`
- **Ruta**: `/api/players/profile`
- **Acceso**: Protegido (`Bearer <token>`)
- **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <TOKEN_JWT>`

#### Payload JSON (Request Body):
```json
{
  "userId": 1,
  "nickname": "Hide on bush",
  "bio": "Mid laner for T1. 4-times World Champion.",
  "avatarUrl": "https://cdn.statikk.gg/avatars/faker.png",
  "primaryRegion": "KR"
}
```

#### Validaciones del Body:
* `userId`: Obligatorio (`@NotNull`), ID del usuario en `auth_db`.
* `nickname`: Obligatorio (`@NotBlank`), entre 2 y 50 caracteres.
* `bio`, `avatarUrl`, `primaryRegion`: Opcionales.

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "nickname": "Hide on bush",
  "bio": "Mid laner for T1. 4-times World Champion.",
  "avatarUrl": "https://cdn.statikk.gg/avatars/faker.png",
  "primaryRegion": "KR",
  "games": [],
  "createdAt": "2026-10-15T09:35:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/players/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -d '{
    "userId": 1,
    "nickname": "Hide on bush",
    "bio": "Mid laner for T1. 4-times World Champion.",
    "avatarUrl": "https://cdn.statikk.gg/avatars/faker.png",
    "primaryRegion": "KR"
  }'
```

---

### 2.2 Obtener Perfil de Jugador por User ID
Recupera el perfil del jugador junto a la lista de videojuegos que tiene asociados en su biblioteca.

- **Método**: `GET`
- **Ruta**: `/api/players/{userId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "nickname": "Hide on bush",
  "bio": "Mid laner for T1. 4-times World Champion.",
  "avatarUrl": "https://cdn.statikk.gg/avatars/faker.png",
  "primaryRegion": "KR",
  "games": [
    {
      "id": 1,
      "userId": 1,
      "gameName": "League of Legends",
      "genre": "MOBA",
      "platform": "PC",
      "registeredAt": "2026-10-15T09:40:00"
    }
  ],
  "createdAt": "2026-10-15T09:35:00"
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/players/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 2.3 Ingresar / Asociar Videojuego a un Jugador (RF-03)
Registra un nuevo videojuego en el catálogo personal del jugador para comenzar a trackear partidas y estadísticas.

- **Método**: `POST`
- **Ruta**: `/api/players/{userId}/games`
- **Acceso**: Protegido (`Bearer <token>`)
- **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <TOKEN_JWT>`

#### Payload JSON (Request Body):
```json
{
  "gameName": "League of Legends",
  "genre": "MOBA",
  "platform": "PC"
}
```

#### Validaciones del Body:
* `gameName`: Obligatorio (`@NotBlank`), entre 1 y 100 caracteres.
* `genre`: Opcional (ej: `MOBA`, `FPS`, `RPG`, `Battle Royale`).
* `platform`: Opcional (ej: `PC`, `PlayStation 5`, `Xbox Series X`, `Switch`).

#### Respuesta Exitosa (`201 CREATED`):
```json
{
  "id": 1,
  "userId": 1,
  "gameName": "League of Legends",
  "genre": "MOBA",
  "platform": "PC",
  "registeredAt": "2026-10-15T09:40:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/players/1/games \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -d '{
    "gameName": "League of Legends",
    "genre": "MOBA",
    "platform": "PC"
  }'
```

---

### 2.4 Listar Videojuegos Asociados al Jugador
Obtiene todos los videojuegos dados de alta por un usuario.

- **Método**: `GET`
- **Ruta**: `/api/players/{userId}/games`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
[
  {
    "id": 1,
    "userId": 1,
    "gameName": "League of Legends",
    "genre": "MOBA",
    "platform": "PC",
    "registeredAt": "2026-10-15T09:40:00"
  },
  {
    "id": 2,
    "userId": 1,
    "gameName": "Valorant",
    "genre": "Tactical Shooter",
    "platform": "PC",
    "registeredAt": "2026-10-15T09:45:00"
  }
]
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/players/1/games \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 2.5 Obtener un Videojuego Específico del Jugador
Consulta el detalle de un juego asociado por su ID.

- **Método**: `GET`
- **Ruta**: `/api/players/{userId}/games/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "gameName": "League of Legends",
  "genre": "MOBA",
  "platform": "PC",
  "registeredAt": "2026-10-15T09:40:00"
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/players/1/games/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 2.6 Eliminar Registro de Videojuego (RF-07)
Elimina el videojuego de la biblioteca del jugador y dispara el evento en RabbitMQ (`player.game.deleted`) para purgar automáticamente en cascada las partidas en `ms-matches` y recalcular/limpiar métricas en `ms-stats`.

- **Método**: `DELETE`
- **Ruta**: `/api/players/{userId}/games/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "message": "Game registration and associated history successfully purged",
  "success": true
}
```

#### Ejemplo cURL:
```bash
curl -X DELETE http://localhost:8080/api/players/1/games/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

## 3. ⚔️ Microservicio de Partidas (`ms-matches`)

Ruta base Gateway: `http://localhost:8080/api/matches`  
Ruta directa: `http://localhost:8083/api/matches`

---

### 3.1 Registrar Partida Manual
Permite ingresar individualmente el resultado y detalle estadístico de una partida jugada.

- **Método**: `POST`
- **Ruta**: `/api/matches`
- **Acceso**: Protegido (`Bearer <token>`)
- **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <TOKEN_JWT>`

#### Payload JSON (Request Body):
```json
{
  "userId": 1,
  "gameId": 1,
  "result": "WIN",
  "durationMinutes": 32.5,
  "hoursPlayed": 0.54,
  "kills": 12,
  "deaths": 2,
  "assists": 8,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Pentakill en minuto 28 para asegurar Barón",
  "playedAt": "2026-10-15T15:30:00"
}
```

#### Validaciones del Body:
* `userId`: Obligatorio (`@NotNull`).
* `gameId`: Obligatorio (`@NotNull`).
* `result`: Obligatorio (`@NotBlank`), valores permitidos: `WIN`, `LOSS`, `DRAW`.
* `hoursPlayed`: Obligatorio (`@NotNull`), no negativo (`@PositiveOrZero`).
* `kills`, `deaths`, `assists`: No negativos (`@PositiveOrZero`).
* `durationMinutes`, `championOrCharacter`, `gameMode`, `notes`, `playedAt`: Opcionales.

#### Respuesta Exitosa (`201 CREATED`):
```json
{
  "id": 1,
  "userId": 1,
  "gameId": 1,
  "result": "WIN",
  "durationMinutes": 32.5,
  "hoursPlayed": 0.54,
  "kills": 12,
  "deaths": 2,
  "assists": 8,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Pentakill en minuto 28 para asegurar Barón",
  "playedAt": "2026-10-15T15:30:00",
  "createdAt": "2026-10-15T16:05:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/matches \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -d '{
    "userId": 1,
    "gameId": 1,
    "result": "WIN",
    "durationMinutes": 32.5,
    "hoursPlayed": 0.54,
    "kills": 12,
    "deaths": 2,
    "assists": 8,
    "championOrCharacter": "Ahri",
    "gameMode": "Ranked Solo/Duo",
    "notes": "Pentakill en minuto 28 para asegurar Barón",
    "playedAt": "2026-10-15T15:30:00"
  }'
```

---

### 3.2 Actualizar Partida Existente (RF-06)
Modifica estadísticas de una partida errónea o incompleta y emite un evento en RabbitMQ (`matches.updated`) para recalcular métricas agregadas automáticamente.

- **Método**: `PUT`
- **Ruta**: `/api/matches/{id}`
- **Acceso**: Protegido (`Bearer <token>`)
- **Headers**:
  * `Content-Type: application/json`
  * `Authorization: Bearer <TOKEN_JWT>`

#### Payload JSON (Request Body):
```json
{
  "result": "WIN",
  "durationMinutes": 35.0,
  "hoursPlayed": 0.58,
  "kills": 14,
  "deaths": 2,
  "assists": 10,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Corrección de asistencias y duración final",
  "playedAt": "2026-10-15T15:30:00"
}
```

#### Validaciones del Body:
* `result`: Opcional (`WIN`, `LOSS`, `DRAW`).
* `hoursPlayed`, `kills`, `deaths`, `assists`: No negativos (`@PositiveOrZero`).

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "gameId": 1,
  "result": "WIN",
  "durationMinutes": 35.0,
  "hoursPlayed": 0.58,
  "kills": 14,
  "deaths": 2,
  "assists": 10,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Corrección de asistencias y duración final",
  "playedAt": "2026-10-15T15:30:00",
  "createdAt": "2026-10-15T16:05:00"
}
```

#### Ejemplo cURL:
```bash
curl -X PUT http://localhost:8080/api/matches/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -d '{
    "result": "WIN",
    "durationMinutes": 35.0,
    "hoursPlayed": 0.58,
    "kills": 14,
    "deaths": 2,
    "assists": 10,
    "championOrCharacter": "Ahri",
    "gameMode": "Ranked Solo/Duo",
    "notes": "Corrección de asistencias y duración final",
    "playedAt": "2026-10-15T15:30:00"
  }'
```

---

### 3.3 Obtener Partida por ID
- **Método**: `GET`
- **Ruta**: `/api/matches/{id}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "gameId": 1,
  "result": "WIN",
  "durationMinutes": 35.0,
  "hoursPlayed": 0.58,
  "kills": 14,
  "deaths": 2,
  "assists": 10,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Corrección de asistencias y duración final",
  "playedAt": "2026-10-15T15:30:00",
  "createdAt": "2026-10-15T16:05:00"
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/matches/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 3.4 Listar Todas las Partidas de un Usuario
- **Método**: `GET`
- **Ruta**: `/api/matches/user/{userId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
[
  {
    "id": 1,
    "userId": 1,
    "gameId": 1,
    "result": "WIN",
    "durationMinutes": 35.0,
    "hoursPlayed": 0.58,
    "kills": 14,
    "deaths": 2,
    "assists": 10,
    "championOrCharacter": "Ahri",
    "gameMode": "Ranked Solo/Duo",
    "notes": "Corrección de asistencias y duración final",
    "playedAt": "2026-10-15T15:30:00",
    "createdAt": "2026-10-15T16:05:00"
  }
]
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/matches/user/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 3.5 Listar Partidas de un Usuario por Juego Específico
- **Método**: `GET`
- **Ruta**: `/api/matches/user/{userId}/game/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/matches/user/1/game/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 3.6 Eliminar Partida por ID
- **Método**: `DELETE`
- **Ruta**: `/api/matches/{id}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "message": "Match deleted successfully",
  "success": true
}
```

#### Ejemplo cURL:
```bash
curl -X DELETE http://localhost:8080/api/matches/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 3.7 Eliminar Todas las Partidas de un Juego para un Usuario
- **Método**: `DELETE`
- **Ruta**: `/api/matches/user/{userId}/game/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "message": "All matches for user and game deleted successfully",
  "success": true
}
```

#### Ejemplo cURL:
```bash
curl -X DELETE http://localhost:8080/api/matches/user/1/game/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

## 4. 📊 Microservicio de Estadísticas (`ms-stats`)

Ruta base Gateway: `http://localhost:8080/api/stats`  
Ruta directa: `http://localhost:8084/api/stats`

---

### 4.1 Panel Centralizado y Métricas Globales del Jugador (RF-05)
Obtiene el resumen consolidado de todos los videojuegos trackeados del usuario: horas acumuladas, ratio V/D global, promedio KDA y detalle por cada videojuego (con soporte de caché distribuida en Redis).

- **Método**: `GET`
- **Ruta**: `/api/stats/dashboard/{userId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "userId": 1,
  "totalGamesTracked": 2,
  "grandTotalMatches": 25,
  "grandTotalWins": 18,
  "grandTotalLosses": 7,
  "overallWinRate": 72.0,
  "grandTotalHoursPlayed": 14.8,
  "overallAverageKda": 4.25,
  "gameStatsList": [
    {
      "id": 1,
      "userId": 1,
      "gameId": 1,
      "totalMatches": 15,
      "totalWins": 11,
      "totalLosses": 4,
      "totalDraws": 0,
      "winRate": 73.33,
      "totalHoursPlayed": 8.5,
      "totalKills": 140,
      "totalDeaths": 45,
      "totalAssists": 110,
      "averageKda": 5.56,
      "lastCalculatedAt": "2026-10-15T09:50:00"
    },
    {
      "id": 2,
      "userId": 1,
      "gameId": 2,
      "totalMatches": 10,
      "totalWins": 7,
      "totalLosses": 3,
      "totalDraws": 0,
      "winRate": 70.0,
      "totalHoursPlayed": 6.3,
      "totalKills": 190,
      "totalDeaths": 80,
      "totalAssists": 45,
      "averageKda": 2.94,
      "lastCalculatedAt": "2026-10-15T09:50:00"
    }
  ]
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/stats/dashboard/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 4.2 Consultar Estadísticas de un Videojuego Específico (RF-05)
Consulta las estadísticas calculadas para una combinación de usuario y videojuego.

- **Método**: `GET`
- **Ruta**: `/api/stats/user/{userId}/game/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "gameId": 1,
  "totalMatches": 15,
  "totalWins": 11,
  "totalLosses": 4,
  "totalDraws": 0,
  "winRate": 73.33,
  "totalHoursPlayed": 8.5,
  "totalKills": 140,
  "totalDeaths": 45,
  "totalAssists": 110,
  "averageKda": 5.56,
  "lastCalculatedAt": "2026-10-15T09:50:00"
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/stats/user/1/game/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 4.3 Listar Estadísticas de Todos los Juegos del Usuario
- **Método**: `GET`
- **Ruta**: `/api/stats/user/{userId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/stats/user/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 4.4 Forzar Recálculo Manual de Estadísticas (RF-06)
Dispara la agregación en tiempo real de todas las partidas registradas en `ms-matches` para el usuario y juego especificados, invalidando la caché de Redis y devolviendo la métrica actualizada.

- **Método**: `POST`
- **Ruta**: `/api/stats/recalculate/user/{userId}/game/{gameId}`
- **Acceso**: Protegido (`Bearer <token>`)
- **Body**: *Vacío* (se utilizan las variables de ruta)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "gameId": 1,
  "totalMatches": 16,
  "totalWins": 12,
  "totalLosses": 4,
  "totalDraws": 0,
  "winRate": 75.0,
  "totalHoursPlayed": 9.08,
  "totalKills": 154,
  "totalDeaths": 47,
  "totalAssists": 120,
  "averageKda": 5.83,
  "lastCalculatedAt": "2026-10-15T10:00:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/stats/recalculate/user/1/game/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 4.5 Ejecutar Job Masivo de Spring Batch
Ejecuta el job asíncrono de agregación por lotes para todos los usuarios y partidas del sistema.

- **Método**: `POST`
- **Ruta**: `/api/stats/batch/run`
- **Acceso**: Protegido (`Bearer <token>`)
- **Body**: *Vacío*

#### Respuesta Exitosa (`200 OK`):
```json
{
  "status": "SUCCESS",
  "message": "Batch stats aggregation job launched successfully"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/stats/batch/run \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

## 5. 📑 Microservicio Ingesta Masiva Excel / CSV (`ms-excel-parser`)

Ruta base Gateway: `http://localhost:8080/api/excel`  
Ruta directa: `http://localhost:8085/api/excel`

---

### 5.1 Carga Masiva de Estadísticas vía Excel/CSV (RF-04)
Carga un archivo en formato `.xlsx` o `.csv`. Valida cabeceras requeridas, coherencia de tipos y emite asíncronamente los eventos a RabbitMQ (`excel.matches.imported`) para registrar las partidas masivamente.

- **Método**: `POST`
- **Ruta**: `/api/excel/upload`
- **Acceso**: Protegido (`Bearer <token>`)
- **Content-Type**: `multipart/form-data`
- **Parámetros Form-Data**:
  * `file`: Archivo binario (`.xlsx` o `.csv`).
  * `userId`: ID del usuario (`Long`).
  * `gameId`: ID del juego asociado (`Long`).

#### Cabeceras Esperadas en el archivo:
```csv
gameName,result,durationMinutes,kills,deaths,assists,playedAt
League of Legends,WIN,32,9,3,12,2026-10-01T18:30:00Z
League of Legends,LOSS,28,4,8,6,2026-10-02T20:10:00Z
```

#### Respuesta Exitosa (`200 OK`):
```json
{
  "filename": "partidas-ejemplo.csv",
  "userId": 1,
  "gameId": 1,
  "totalRowsParsed": 5,
  "status": "SUCCESS",
  "message": "Successfully parsed 5 matches and queued for processing",
  "timestamp": "2026-10-15T10:05:00"
}
```

#### Ejemplo cURL:
```bash
curl -X POST http://localhost:8080/api/excel/upload \
  -H "Authorization: Bearer <TOKEN_JWT>" \
  -F "file=@samples/partidas-ejemplo.csv" \
  -F "userId=1" \
  -F "gameId=1"
```

---

### 5.2 Descargar Plantilla Oficial Excel
Descarga una plantilla preformateada en formato `.xlsx` con las columnas exactas requeridas para la carga masiva.

- **Método**: `GET`
- **Ruta**: `/api/excel/template`
- **Acceso**: Público
- **Respuesta**: Archivo binario `statikk_template.xlsx` con cabecera `Content-Disposition: attachment; filename=statikk_template.xlsx`.

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/excel/template \
  --output statikk_template.xlsx
```

---

## 6. 🔔 Microservicio de Notificaciones (`ms-notification`)

Ruta base Gateway: `http://localhost:8080/api/notifications`  
Ruta directa: `http://localhost:8086/api/notifications`

---

### 6.1 Listar Notificaciones de un Usuario
Consulta el historial de notificaciones generadas en el sistema (ej. bienvenida, carga de Excel completada, hitos de KDA).

- **Método**: `GET`
- **Ruta**: `/api/notifications/user/{userId}`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "¡Bienvenido a Statikk.gg!",
    "message": "Tu cuenta faker_god ha sido creada exitosamente.",
    "type": "WELCOME",
    "readStatus": false,
    "createdAt": "2026-10-15T09:30:00"
  },
  {
    "id": 2,
    "userId": 1,
    "title": "Carga de partidas procesada",
    "message": "Se han importado 5 partidas para League of Legends.",
    "type": "EXCEL_PROCESSED",
    "readStatus": false,
    "createdAt": "2026-10-15T10:05:00"
  }
]
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/notifications/user/1 \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 6.2 Marcar Notificación como Leída
Actualiza el estado de lectura de una notificación.

- **Método**: `PUT`
- **Ruta**: `/api/notifications/{id}/read`
- **Acceso**: Protegido (`Bearer <token>`)
- **Body**: *Vacío* (se utiliza el ID de la URL)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "id": 1,
  "userId": 1,
  "readStatus": true,
  "message": "Notification marked as read successfully"
}
```

#### Ejemplo cURL:
```bash
curl -X PUT http://localhost:8080/api/notifications/1/read \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 6.3 Obtener Contador de Notificaciones No Leídas
Devuelve la cantidad de notificaciones pendientes de lectura para mostrar en la campana de alertas del cliente web/móvil.

- **Método**: `GET`
- **Ruta**: `/api/notifications/user/{userId}/unread-count`
- **Acceso**: Protegido (`Bearer <token>`)

#### Respuesta Exitosa (`200 OK`):
```json
{
  "unreadCount": 2
}
```

#### Ejemplo cURL:
```bash
curl -X GET http://localhost:8080/api/notifications/user/1/unread-count \
  -H "Authorization: Bearer <TOKEN_JWT>"
```

---

### 6.4 Conexión en Tiempo Real vía WebSocket (STOMP)
Permite a la aplicación cliente recibir notificaciones push en tiempo real a través de WebSockets.

- **Endpoint de Conexión**: `ws://localhost:8080/ws` (o con SockJS `http://localhost:8080/ws`)
- **Prefijo de Destino**: `/topic` y `/queue`
- **Topic de Usuario**: `/topic/notifications/{userId}`
- **Cliente de prueba incluido en el proyecto**: `samples/ws-client.html`

---

## 7. 📑 Resumen Rápido de Payloads JSON (Copiar y Pegar)

### `POST /api/auth/register`
```json
{
  "username": "faker_god",
  "email": "faker@t1.gg",
  "password": "Password123!"
}
```

### `POST /api/auth/login`
```json
{
  "email": "faker@t1.gg",
  "password": "Password123!"
}
```

### `POST /api/players/profile`
```json
{
  "userId": 1,
  "nickname": "Hide on bush",
  "bio": "Mid laner for T1. 4-times World Champion.",
  "avatarUrl": "https://cdn.statikk.gg/avatars/faker.png",
  "primaryRegion": "KR"
}
```

### `POST /api/players/{userId}/games`
```json
{
  "gameName": "League of Legends",
  "genre": "MOBA",
  "platform": "PC"
}
```

### `POST /api/matches`
```json
{
  "userId": 1,
  "gameId": 1,
  "result": "WIN",
  "durationMinutes": 32.5,
  "hoursPlayed": 0.54,
  "kills": 12,
  "deaths": 2,
  "assists": 8,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Pentakill en minuto 28 para asegurar Barón",
  "playedAt": "2026-10-15T15:30:00"
}
```

### `PUT /api/matches/{id}`
```json
{
  "result": "WIN",
  "durationMinutes": 35.0,
  "hoursPlayed": 0.58,
  "kills": 14,
  "deaths": 2,
  "assists": 10,
  "championOrCharacter": "Ahri",
  "gameMode": "Ranked Solo/Duo",
  "notes": "Corrección de asistencias y duración final",
  "playedAt": "2026-10-15T15:30:00"
}
```

---

## 8. 🩺 Actuator y Monitoreo de Microservicios

Endpoints estándar de Spring Boot Actuator disponibles en cada microservicio y a través del API Gateway:

* **Salud del sistema**: `GET http://localhost:8080/actuator/health`
* **Información de la aplicación**: `GET http://localhost:8080/actuator/info`
* **Métricas**: `GET http://localhost:8080/actuator/metrics`
* **Dashboard Eureka Service Discovery**: `http://localhost:8761`
* **Servidor de Correos de Prueba Mailhog (Web UI)**: `http://localhost:8025`
* **RabbitMQ Management Dashboard**: `http://localhost:15672` (usuario: `guest`, contraseña: `guest`)
