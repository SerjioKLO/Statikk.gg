# Statikk.gg — v0.1 Infraestructura base

Esta versión entrega únicamente la **columna vertebral** de la arquitectura:

- `ms-registry` (puerto 8761) — Eureka Server, descubrimiento de servicios.
- `ms-gateway` (puerto 8080) — Spring Cloud Gateway, con:
  - Enrutamiento pre-configurado hacia los 6 microservicios futuros (`lb://ms-auth`, `lb://ms-player`, etc.) vía Eureka.
  - Rate limiting con Redis (`RequestRateLimiter`) en las rutas de mayor tráfico esperado.
  - Filtro global de validación JWT (`JwtAuthenticationFilter`), ya cableado para cuando `ms-auth` (v0.2) empiece a emitir tokens con el mismo `jwt.secret`.

Como ningún microservicio de dominio existe todavía, las rutas `/api/auth/**`, `/api/players/**`, etc. devolverán `503 Service Unavailable` hasta que construyas esos servicios en las siguientes versiones — eso es esperado.

