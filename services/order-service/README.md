# Order Service

Owns customer carts, order records, price snapshots, idempotency records, and
order status history in `order_db`. It does not read Menu or Restaurant
databases. It calls Menu Service for current item quotes and Restaurant
Service to confirm that the restaurant is open. Order line names, unit prices,
quantities, and totals are copied into the order database at checkout so later
menu edits cannot change an existing order.

## Local setup

Start Docker PostgreSQL, Service Registry, Restaurant Service, and Menu
Service. The PostgreSQL init script creates `order_db` only when its data volume
is first initialized. If you already used the volume before this database was
added, create it once as the database administrator, owned by `DB_USERNAME`.
For the local `fooddelivery_app` account, the command is:

```powershell
docker compose exec postgres psql -U fooddelivery_app -d user_db -c "CREATE DATABASE order_db OWNER fooddelivery_app;"
```

In STS, add these variables to the Order Service Run Configuration's
Environment tab: `DB_HOST=localhost`, `DB_PORT=5432`, `DB_USERNAME` and
`DB_PASSWORD` from `.env`, `ORDER_DB_NAME=order_db`,
`MENU_SERVICE_URL=http://localhost:8083`,
`RESTAURANT_SERVICE_URL=http://localhost:8082`, and
`EUREKA_SERVER_URL=http://localhost:8761/eureka/`. STS does not load `.env`
automatically. The service listens on port `8084` and registers as
`order-service`. Swagger UI is at <http://localhost:8084/swagger-ui.html>.

From `services/order-service`:

```powershell
mvn spring-boot:run
```

Flyway creates and versions the order schema; Hibernate validates it at
startup. JWT signing configuration must match the User Service.

## API

Cart and order creation require a `CUSTOMER` JWT. One active cart is stored per
customer and it may contain items from only one restaurant. Adding an item
checks that it is currently available. The cart price is a preview; checkout
fetches current menu quotes again and rejects unavailable items or a restaurant
that is not `OPEN`.

```text
GET    /api/v1/orders/cart
POST   /api/v1/orders/cart/items
PUT    /api/v1/orders/cart/items/{menuItemId}
DELETE /api/v1/orders/cart/items/{menuItemId}
DELETE /api/v1/orders/cart
POST   /api/v1/orders/checkout
GET    /api/v1/orders/mine?page=0&size=20&sort=createdAt,desc
GET    /api/v1/orders/{orderId}
PATCH  /api/v1/orders/{orderId}/status
```

Add an item using `{ "menuItemId": "<uuid>", "quantity": 2 }`. Checkout
requires an `Idempotency-Key` header and a delivery address:

```http
POST /api/v1/orders/checkout
Authorization: Bearer <customer-jwt>
Idempotency-Key: 1c9a9a40-e05b-43c8-a875-78890a022e82
Content-Type: application/json
```

```json
{ "deliveryAddress": "12 Example Street, City" }
```

The key is unique per customer. Repeating it with the same delivery address
returns the original order; reusing it with a different address returns HTTP
409. The idempotency reservation, order, item snapshots, initial history entry,
and cart clear commit in one PostgreSQL transaction. A unique database
constraint handles concurrent retries. Cart row locking serializes edits and
checkout; the cart version is rechecked after upstream price validation.

The initial order status is `PENDING`. Valid transitions are:

```text
PENDING -> CONFIRMED -> RESTAURANT_ACCEPTED -> PREPARING
        -> READY_FOR_PICKUP -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
PENDING -> CANCELLED
CONFIRMED -> CANCELLED
```

Restaurant owners can advance the restaurant workflow through
`READY_FOR_PICKUP`, after Restaurant Service verifies ownership. Customers can
cancel their own order while it is pending or confirmed. Admins can perform any
valid transition. Pickup and delivery transitions are admin-only until the
Delivery Service assigns a partner. Every change is stored in
`order_status_history`; optimistic versions and row locks prevent concurrent
requests from silently overwriting each other.

Delivery fee and tax are currently zero because their pricing rules and
Payment Service are later phases. The total is the current menu subtotal until
those policies are introduced.
