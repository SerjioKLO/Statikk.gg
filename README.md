# Statikk.gg — v0.1 Infraestructura base

Esta versión entrega únicamente la **columna vertebral** de la arquitectura:

- `ms-registry` (puerto 8761) — Eureka Server, descubrimiento de servicios.
- `ms-gateway` (puerto 8080) — Spring Cloud Gateway, con:
  - Enrutamiento pre-configurado hacia los 6 microservicios futuros (`lb://ms-auth`, `lb://ms-player`, etc.) vía Eureka.
  - Rate limiting con Redis (`RequestRateLimiter`) en las rutas de mayor tráfico esperado.
  - Filtro global de validación JWT (`JwtAuthenticationFilter`), ya cableado para cuando `ms-auth` (v0.2) empiece a emitir tokens con el mismo `jwt.secret`.

Como ningún microservicio de dominio existe todavía, las rutas `/api/auth/**`, `/api/players/**`, etc. devolverán `503 Service Unavailable` hasta que construyas esos servicios en las siguientes versiones — eso es esperado.

## Cómo correrlo

### Opción A — Docker Compose (recomendado)

```bash
cd statikk-v0.1
docker compose up --build
```

Esto levanta Redis, `ms-registry` y `ms-gateway` en orden correcto (el gateway espera a que el registry esté healthy).

### Opción B — Localmente con Maven

Necesitas Java 21 y Maven instalados, y Redis corriendo en `localhost:6379`.

```bash
cd statikk-v0.1
mvn clean install

# Terminal 1
cd ms-registry && mvn spring-boot:run

# Terminal 2 (esperar a que el registry esté arriba)
cd ms-gateway && mvn spring-boot:run
```

## Cómo verificar que funciona

1. **Dashboard de Eureka**: abre `http://localhost:8761` — deberías ver el panel de Eureka (sin instancias registradas todavía, porque `ms-gateway` no se auto-registra como servicio de negocio, solo consulta el registry).
2. **Salud del gateway**: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`.
3. **Ruta protegida sin token** (debe fallar con 401, confirma que el filtro JWT funciona):
   ```bash
   curl -i http://localhost:8080/api/players/123
   ```
4. **Ruta pública** (no debería requerir token, aunque devuelva 503 porque `ms-excel-parser` no existe aún):
   ```bash
   curl -i http://localhost:8080/api/excel/template
   ```

## v0.2 — ms-auth

Agrega `ms-auth` (puerto 8081): registro/login con BCrypt y emisión de JWT, usando **el mismo** `jwt.secret` que ya tenía configurado `ms-gateway` desde v0.1.

### Probarlo

```bash
docker compose up --build

# 1. Registrar un usuario
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"sergio","email":"sergio@statikk.gg","password":"supersecreta123"}' | jq

# 2. Guarda el token que te devolvió, y pruébalo contra una ruta protegida
curl -i http://localhost:8080/api/players/123 \
  -H "Authorization: Bearer <TOKEN_DEL_PASO_1>"
```

Si entre el paso 1 y el paso 2 pasan más de un par de segundos, vas a ver que el gateway devuelve `401` con `X-Auth-Error: Token inválido o expirado` — **aunque el token se acaba de emitir**. Ese es el bug conocido de esta versión: revisa `JwtService.generateToken()`.

## v0.3 — ms-player

Agrega `ms-player` (puerto 8082): perfiles de jugador y videojuegos asociados.

Novedades de esta versión:
- **`ms-gateway` ahora propaga identidad**: tras validar el JWT, agrega los headers `X-User-Id`, `X-Username` y `X-User-Role` a la petición antes de reenviarla. `ms-player` confía en ellos en vez de volver a parsear el token. En rutas públicas, el gateway borra esos headers si el cliente intentó mandarlos por su cuenta (anti-suplantación).
- **Caché Redis** en `GET /api/players/{id}`, invalidada en cada update/add-game/delete-game.
- **RabbitMQ**: al borrar un videojuego se publica `player.game.deleted` en el exchange `statikk.events` (lo consumirá `ms-stats` en v0.6).

### Probarlo de punta a punta

```bash
docker compose up --build

# 1. Login (requiere el fix del JWT de v0.2 ya aplicado)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sergio@statikk.gg","password":"supersecreta123"}' | jq -r .token)

# 2. Crear perfil de jugador (el userId lo pone el gateway, no tú)
curl -s -X POST http://localhost:8080/api/players \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"displayName":"Sergio","country":"CL"}' | jq

# 3. Agregar un videojuego (usa el "id" que devolvió el paso 2)
curl -s -X POST http://localhost:8080/api/players/1/games \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gameName":"League of Legends","platform":"PC","rankLabel":"Diamante","hoursPlayed":320}' | jq

# 4. Confirmar que quedó en caché (revisa los logs: la segunda llamada no debería pegarle a MySQL)
curl -s http://localhost:8080/api/players/1 | jq
curl -s http://localhost:8080/api/players/1 | jq
```

Prueba también el caso prohibido: intenta mandar `X-User-Id: 999` manualmente en el header de una petición — el gateway lo va a pisar igual con tu userId real del token, así que no sirve para suplantar a otro usuario.

## v0.4 — ms-matches

Agrega `ms-matches` (puerto 8083): historial de partidas de cada jugador.

Novedades de esta versión:
- **Primera comunicación síncrona entre microservicios**: antes de guardar o borrar una partida, `ms-matches` le pregunta a `ms-player` (vía **Feign**, resuelto por Eureka) si el perfil existe y a quién pertenece, para evitar que alguien registre partidas en el perfil de otro.
- **Tolerancia a fallos**: si `ms-player` responde 404 → `404` ("el perfil no existe"). Si `ms-player` está caído, lento o sin instancias en Eureka → `503` controlado, no un `500` genérico. Timeouts configurados: 2s de conexión, 3s de lectura.
- **KDA calculado** en cada respuesta: `(kills + assists) / max(deaths, 1)`.
- **Eventos RabbitMQ**: `match.created` y `match.deleted` en el exchange `statikk.events` (los consumirá `ms-stats` en v0.6).

### Probarlo de punta a punta

```bash
docker compose up --build

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sergio@statikk.gg","password":"supersecreta123"}' | jq -r .token)

# 1. Registrar una partida (playerProfileId = el id de tu perfil creado en v0.3)
curl -s -X POST http://localhost:8080/api/matches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"playerProfileId":1,"gameName":"League of Legends","result":"WIN","durationMinutes":32,"kills":9,"deaths":3,"assists":12}' | jq

# 2. Listar el historial del jugador
curl -s "http://localhost:8080/api/matches?playerProfileId=1" -H "Authorization: Bearer $TOKEN" | jq

# 3. Prueba de tolerancia a fallos: apaga ms-player y repite el paso 1
docker compose stop ms-player
curl -i -X POST http://localhost:8080/api/matches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"playerProfileId":1,"gameName":"League of Legends","result":"LOSS","kills":2,"deaths":7,"assists":4}'
# → 503 "ms-player no respondió al validar el perfil 1..."

# 4. Vuélvelo a levantar
docker compose start ms-player
```

También puedes probar el caso de propiedad: crea un segundo usuario, e intenta registrar una partida con el `playerProfileId` del primero → `403`.

## v0.5 — ms-excel-parser

Agrega `ms-excel-parser` (puerto 8085): carga masiva de partidas desde `.xlsx` o `.csv`.

Cómo funciona:
1. `GET /api/excel/template` (pública) descarga la plantilla `.xlsx`, con lista desplegable en `result` y una hoja de instrucciones.
2. `POST /api/excel/matches/import?playerProfileId=N` (multipart, campo `file`) valida el archivo y responde **`202 Accepted`**: la inserción es asíncrona.
3. El parser publica `excel.matches.imported` en RabbitMQ; **`ms-matches`** (nuevo consumidor) inserta las partidas y publica `match.bulk.imported` para `ms-stats` (v0.6).

Decisiones de diseño:
- **Todo o nada**: si UNA fila es inválida, no se importa ninguna y la respuesta es `422` con el detalle de cada error (fila, columna, motivo; máx. 50 listados).
- **Seguridad**: se valida por Feign que el `playerProfileId` pertenezca al usuario del token (si no, `403`). Extensión y Content-Type se validan; el contenido real se valida al abrir el archivo.
- **CSV de Excel en español**: se detecta automáticamente el separador `;` y el BOM UTF-8.
- **Idempotencia**: RabbitMQ puede entregar un mensaje dos veces. `ms-matches` guarda el `importId` en `processed_imports` y ignora los duplicados.
- **Sin mensajes envenenados**: el listener reintenta 3 veces y luego descarta (no reencola infinitamente).
- Límites: 5 MB por archivo, 5000 partidas por archivo, y rate limit de 2 req/s (ráfaga 5) en `/api/excel/**` desde el gateway.

### Probarlo

```bash
docker compose up --build

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sergio@statikk.gg","password":"supersecreta123"}' | jq -r .token)

# 1. Descargar la plantilla (no requiere token)
curl -o plantilla.xlsx http://localhost:8080/api/excel/template

# 2. Importar el CSV de ejemplo (5 partidas)
curl -s -X POST "http://localhost:8080/api/excel/matches/import?playerProfileId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@samples/partidas-ejemplo.csv;type=text/csv" | jq
# -> 202 {"importId":"...","rowsAccepted":5,"status":"QUEUED",...}

# 3. Esperar 1-2 segundos y revisar el historial (deberían aparecer las 5)
curl -s "http://localhost:8080/api/matches?playerProfileId=1" -H "Authorization: Bearer $TOKEN" | jq length

# 4. Archivo con errores -> 422 con detalle por fila, y NO se importa nada
curl -s -X POST "http://localhost:8080/api/excel/matches/import?playerProfileId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@samples/partidas-con-errores.csv;type=text/csv" | jq

# 5. Tipo no permitido -> 415
echo "hola" > malo.txt
curl -s -X POST "http://localhost:8080/api/excel/matches/import?playerProfileId=1" \
  -H "Authorization: Bearer $TOKEN" -F "file=@malo.txt" | jq
```

Revisa también el panel de RabbitMQ (`http://localhost:15672`, usuario `statikk` / `statikk`): la cola `ms-matches.excel-imports` debe quedar en 0 mensajes.

## v0.6 — ms-stats

Agrega `ms-stats` (puerto 8084): métricas agregadas por jugador (ratio V/D, KDA, horas jugadas), globales y por videojuego.

Arquitectura (CQRS + batch):
```
ms-matches ──match.created / match.deleted / match.bulk.imported / match.bulk.deleted──▶ cola ms-stats.match-events
                                                                                              │
                                  COMANDOS: el listener solo marca al jugador como "pendiente" ▼
                                            (tabla stats_pending_recalc, una fila por jugador)
                                                                                              │
                          cada 10 s, si hay pendientes: JOB SPRING BATCH (chunks de 20 jugadores)
                            reader    -> lee los pendientes
                            processor -> pide las partidas a ms-matches (Feign) y agrega por juego
                            writer    -> reemplaza player_game_stats, invalida Redis, publica stats.calculated
                                                                                              │
                          CONSULTAS: GET /api/stats/players/{id} lee player_game_stats (con caché Redis)
```

Decisiones de diseño:
- **Los eventos son mínimos**: a `ms-stats` solo le importa *qué jugador* cambió. Las partidas se vuelven a leer de `ms-matches` (fuente de verdad), así el cálculo es idempotente: recalcular dos veces da lo mismo.
- **Un recálculo por jugador, no por evento**: importar 5000 partidas marca al jugador una vez y se recalcula una vez.
- **Sin avisos perdidos**: el marcador lleva una `version`. Si llega un evento mientras se calcula, el marcador no se borra y el jugador se recalcula en la pasada siguiente.
- **Tolerancia a fallos**: si `ms-matches` no responde, ese jugador queda pendiente y se reintenta solo; el resto del lote no se afecta.
- **Consistencia eventual**: los números tardan hasta ~10-25 s en reflejar un cambio (ajustable con `STATS_BATCH_INTERVAL_MS`).
- **Borrado en cascada**: si quitas un videojuego de tu perfil (`DELETE /api/players/{id}/games/{gameId}`), `ms-matches` ahora también borra tus partidas de ese juego y avisa con `match.bulk.deleted`; recién ahí `ms-stats` recalcula. (Por eso `ms-stats` no escucha `player.game.deleted` directamente: podría recalcular antes de que se borren las partidas.)
- **Fórmulas**: `winRate = victorias/partidas·100`; `winLossRatio = victorias/max(derrotas,1)`; `kda = (kills+assists)/max(muertes,1)`, calculado sobre los *totales*; `hoursPlayed = minutos/60`. Las horas salen de la duración de las partidas, no de las horas que declaras a mano en el perfil.
- Publica `stats.calculated` (lo consumirá `ms-notification` en v0.7).

### Corrección en ms-player (v0.3)
La caché Redis de `ms-player` usaba un serializer genérico con un `ObjectMapper` propio sin información de tipos: la **segunda** lectura de un perfil cacheado habría fallado con `ClassCastException` (el valor volvía como `LinkedHashMap`). Ahora usa un serializer tipado. Mismo patrón en `ms-stats`.

### Probarlo

```bash
docker compose up --build

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sergio@statikk.gg","password":"supersecreta123"}' | jq -r .token)

# 1. Importar el CSV de ejemplo (v0.5) en un perfil NUEVO, sin otras partidas
curl -s -X POST "http://localhost:8080/api/excel/matches/import?playerProfileId=1" \
  -H "Authorization: Bearer $TOKEN" -F "file=@samples/partidas-ejemplo.csv;type=text/csv" | jq

# 2. Esperar ~25 segundos y pedir el dashboard
sleep 25
curl -s http://localhost:8080/api/stats/players/1 -H "Authorization: Bearer $TOKEN" | jq
```

Resultado esperado con solo ese CSV:

| | partidas | V/D/E | winRate | W/L | KDA | horas |
|---|---|---|---|---|---|---|
| **Global** | 5 | 3/1/1 | 60.0 | 3.0 | 2.38 | 2.77 |
| League of Legends | 3 | 2/1/0 | 66.67 | 2.0 | 3.92 | 1.5 |
| Valorant | 2 | 1/0/1 | 50.0 | 1.0 | 1.69 | 1.27 |

```bash
# 3. Cache: repite el GET anterior dos veces seguidas; la segunda sale de Redis (y ya no falla)

# 4. Cascada: quita Valorant de tu perfil (agrégalo antes con POST /api/players/1/games si no lo tienes)
curl -i -X DELETE http://localhost:8080/api/players/1/games/<ID_DEL_JUEGO> -H "Authorization: Bearer $TOKEN"
sleep 25
curl -s "http://localhost:8080/api/matches?playerProfileId=1" -H "Authorization: Bearer $TOKEN" | jq length   # solo quedan las de otros juegos
curl -s http://localhost:8080/api/stats/players/1 -H "Authorization: Bearer $TOKEN" | jq '.games[].gameName'  # Valorant ya no aparece
```

Para ver el job en acción: `docker compose logs -f ms-stats`.

## Qué sigue (v0.7)

Construir `ms-notification`: consume `stats.calculated` y avisa al jugador en tiempo real por WebSocket/STOMP, con envío de correo opcional.
