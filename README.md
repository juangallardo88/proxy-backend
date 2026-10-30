# AI Quota Backend — Proxy de rate limiting y cuotas

Backend en **Spring Boot 3 (Java 21)** del caso de estudio *"Plataforma de consumo de IA con Proxy de rate limiting y cuotas"*.
Usuarios consumen un servicio de IA simulado; dos proxies encadenados controlan las requests por minuto y la cuota mensual de tokens según el plan.

## Patrón Proxy aplicado

```
Cliente (frontend)
   │  POST /api/ai/generate
   ▼
AIController ──► RateLimitProxyService ──► QuotaProxyService ──► MockAIGenerationService
                 (429 + Retry-After)        (402 Payment Required)  (Thread.sleep 1200 ms)
```

| Rol del patrón | Clase |
|---|---|
| Subject (interfaz común) | `service/AIGenerationService` → `generate(GenerationRequest): GenerationResponse` |
| RealSubject (objeto real) | `service/MockAIGenerationService` |
| Proxy 1 | `proxy/RateLimitProxyService` — verifica N requests/min según el plan |
| Proxy 2 | `proxy/QuotaProxyService` — verifica y descuenta tokens del mes, registra el consumo |

La cadena se arma en `config/AppConfig.java`. El controlador solo conoce la interfaz, no sabe si habla con un proxy o con el servicio real.

## Planes

| Plan | Requests/min | Tokens/mes |
|---|---|---|
| FREE | 10 | 50.000 |
| PRO | 60 | 500.000 |
| ENTERPRISE | ilimitado | ilimitado |

En las respuestas, `-1` significa **ilimitado**. Todo usuario nuevo empieza en FREE.
Tokens: se estiman como `caracteres / 4` (redondeado hacia arriba), sumando prompt + respuesta.

## Endpoints

El usuario se identifica con el header **`X-User-Id`** (si no se envía se usa `demo-user`).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ai/generate` | Genera texto pasando por la cadena completa de proxies |
| POST | `/api/ai/estimate` | Estima tokens de un prompt (no consume cuota) |
| GET | `/api/quota/status` | Tokens usados, restantes, `resetDate`, plan y estado del rate limit |
| GET | `/api/quota/history` | Uso diario de los últimos 7 días |
| POST | `/api/quota/upgrade` | Upgrade de plan con pago simulado (FREE → PRO) |
| GET | `/api/health` | Health check |

### Ejemplos

```bash
# Generar
curl -X POST http://localhost:8080/api/ai/generate \
  -H "Content-Type: application/json" -H "X-User-Id: juan" \
  -d '{"prompt":"Explícame el patrón proxy"}'
```
```json
{
  "text": "El patrón Proxy coloca un intermediario ...",
  "promptTokens": 7, "completionTokens": 30, "totalTokens": 37, "latencyMs": 1201,
  "createdAt": "2026-10-30T23:10:00Z",
  "plan": "FREE", "tokensUsedThisMonth": 37, "tokensRemaining": 49963,
  "requestsRemainingThisMinute": 9
}
```
También devuelve los headers `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`.

**429 — Rate limit superado** (header `Retry-After: 28`):
```json
{ "status": 429, "error": "Too Many Requests",
  "message": "Superaste el límite de 10 requests por minuto del plan FREE. Intenta de nuevo en 28 segundos.",
  "details": { "plan": "FREE", "limitPerMinute": 10, "retryAfterSeconds": 28 } }
```

**402 — Cuota agotada:**
```json
{ "status": 402, "error": "Payment Required",
  "message": "Agotaste la cuota mensual de tokens del plan FREE (50000 de 50000). Mejora tu plan para continuar.",
  "details": { "plan": "FREE", "tokensUsed": 50000, "monthlyLimit": 50000, "tokensRequested": 3, "upgradeEndpoint": "/api/quota/upgrade" } }
```

```bash
# Estado de cuota
curl http://localhost:8080/api/quota/status -H "X-User-Id: juan"
```
```json
{ "userId": "juan", "plan": "FREE", "tokensUsed": 37, "tokensRemaining": 49963,
  "monthlyTokenLimit": 50000, "resetDate": "2026-11-01T00:00-05:00",
  "requestsThisMinute": 1, "requestsPerMinuteLimit": 10, "requestsRemainingThisMinute": 9,
  "secondsUntilRateLimitReset": 28, "rateLimitResetAt": "2026-10-30T18:11-05:00" }
```

```bash
# Historial (7 días, los días sin uso salen en 0)
curl http://localhost:8080/api/quota/history -H "X-User-Id: juan"

# Upgrade con pago simulado
curl -X POST http://localhost:8080/api/quota/upgrade \
  -H "Content-Type: application/json" -H "X-User-Id: juan" \
  -d '{"targetPlan":"PRO","paymentMethod":"CARD","cardLast4":"4242"}'

# Estimar tokens
curl -X POST http://localhost:8080/api/ai/estimate \
  -H "Content-Type: application/json" -d '{"prompt":"Hola mundo"}'
```

El upgrade acepta `PRO` (caso pedido) y también `ENTERPRISE`. No permite bajar de plan (400) ni repetir el actual (409).

## Tareas programadas

`scheduler/QuotaResetScheduler.java` (zona `America/Bogota`):
- `0 * * * * *` → cada minuto reinicia el contador de requests.
- `0 0 0 1 * *` → el día 1 de cada mes a medianoche reinicia la cuota de tokens.

## Estructura

```
src/main/java/com/ucc/aiproxy/
├── AiQuotaBackendApplication.java
├── config/       AppConfig (cadena de proxies), CorsConfig
├── controller/   AIController, QuotaController, HealthController
├── domain/       Plan, UserAccount, DailyUsage
├── dto/          requests y responses
├── exception/    RateLimitExceeded (429), QuotaExceeded (402), GlobalExceptionHandler
├── proxy/        RateLimitProxyService, QuotaProxyService
├── scheduler/    QuotaResetScheduler
├── service/      AIGenerationService, MockAIGenerationService, QuotaService, TokenEstimator
└── store/        UsageStore (memoria)
```

> Los datos se guardan **en memoria**: si Railway reinicia el servicio, el consumo vuelve a cero. Es suficiente para un servicio simulado.

## Ejecutar en local

Requisitos: Java 21 y Maven 3.9+.

```bash
mvn spring-boot:run      # levanta en http://localhost:8080
mvn test                 # pruebas de la cadena de proxies
```

## Subir a GitHub (repositorio solo del backend)

```bash
git init
git add .
git commit -m "Backend: proxy de rate limiting y cuotas"
git branch -M main
git remote add origin https://github.com/<tu-usuario>/ai-quota-backend.git
git push -u origin main
```

## Desplegar en Railway

1. En [railway.com](https://railway.com) → **New Project** → **Deploy from GitHub repo** → elige `ai-quota-backend`.
2. Railway detecta el `Dockerfile` (y `railway.json`) y compila solo.
3. En **Variables** agrega:
   - `CORS_ALLOWED_ORIGINS` = URL del frontend en Vercel (ej. `https://ai-quota-frontend.vercel.app`). Mientras no tengas el frontend puedes dejar `*`.
   - (opcional) `APP_TIMEZONE` = `America/Bogota`.
   - `PORT` no hace falta: Railway la define y la app la usa.
4. En **Settings → Networking → Generate Domain** para obtener la URL pública.
5. Prueba: `https://<tu-app>.up.railway.app/api/health` debe responder `{"status":"UP",...}`.

Esa URL pública es la que usará el frontend como `VITE_API_URL`.
