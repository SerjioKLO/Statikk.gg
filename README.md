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

## Qué sigue (v0.3)

Construir `ms-player`: perfiles de jugador y videojuegos asociados, consumiendo el `userId` que ya viaja en el JWT emitido por `ms-auth`.
