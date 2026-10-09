# API Gateway

Reactive Spring Cloud Gateway entry point for the Food Delivery services.
It routes `/api/v1/...` paths by Eureka service ID, validates signed HS256 JWTs
for protected routes, applies Redis token-bucket rate limits, adds secure
response headers, and propagates a validated `X-Correlation-ID`.

## Run locally

Start Redis and the Service Registry first. Then, from this directory:

```powershell
mvn spring-boot:run
```

The gateway listens on <http://localhost:8080>. Registry URL defaults to
`http://localhost:8761/eureka/`; Redis defaults to `localhost:6379`.

Set `JWT_SECRET` to the same secret the User Service uses. For local development
only, the gateway has a placeholder fallback; never use that fallback in a
deployed environment. The key must contain at least 32 characters. Public
authentication paths are `/api/v1/auth/**`; all other paths require a valid
bearer token. Services must still enforce their own authorization rules.

Rate limiting is keyed by the authenticated principal when present, and by
the direct client IP for anonymous requests. Defaults allow 20 requests per
second with a burst of 40; override with `RATE_LIMIT_REPLENISH_RATE` and
`RATE_LIMIT_BURST_CAPACITY`.

## Routes

| Path | Eureka service ID |
|---|---|
| `/api/v1/auth/**`, `/api/v1/users/**` | `user-service` |
| `/api/v1/restaurants/**` | `restaurant-service` |
| `/api/v1/menu/**` | `menu-service` |
| `/api/v1/search/**` | `search-service` |
| `/api/v1/orders/**` | `order-service` |
| `/api/v1/payments/**` | `payment-service` |
| `/api/v1/deliveries/**` | `delivery-service` |

Gateway routing is configured by service ID, so instances can scale without
hard-coded host addresses. Until those services exist and register, routed
requests cannot be served.
