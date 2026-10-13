# Restaurant Service

Owns restaurant profiles, owner association, geographic coordinates, opening
hours, and availability status. It stores the owner UUID from the JWT, without
a cross-service database foreign key. The User Service remains the authority
for user identity and roles.

## Local setup

The repository PostgreSQL Compose setup creates `restaurant_db` on a fresh
database volume. Start PostgreSQL and the Service Registry, then load the
project `.env` into the PowerShell process (instructions are in the User
Service README). Before starting this module, set:

```powershell
$env:RESTAURANT_DB_NAME = "restaurant_db"
cd services/restaurant-service
mvn spring-boot:run
```

The service listens on port `8082` and registers with Eureka as
`restaurant-service`. Flyway owns schema changes and Hibernate validates the
migrated schema. Use the same `JWT_SECRET` as the API Gateway and User Service.

Swagger UI: <http://localhost:8082/swagger-ui.html>

## API

Public discovery and detail:

```text
GET /api/v1/restaurants?keyword=thai&page=0&size=20&sort=name,asc
GET /api/v1/restaurants/{restaurantId}
```

Owner/admin operations:

```text
POST  /api/v1/restaurants
GET   /api/v1/restaurants/mine?page=0&size=20&sort=createdAt,desc
PUT   /api/v1/restaurants/{restaurantId}
PATCH /api/v1/restaurants/{restaurantId}/status
```

Owners can manage only restaurants whose `ownerId` matches their JWT subject.
Admins can manage any restaurant and are the only role allowed to set
`SUSPENDED`. Newly created restaurants begin as `CLOSED`; owners explicitly
change status when ready to accept orders. Public discovery never returns a
suspended restaurant. Because public registration only grants `CUSTOMER`, an
administrator must provision the `RESTAURANT_OWNER` role before a user can
create a restaurant; role self-assignment is not exposed.

For local development, after registering the owner account, an administrator
can grant that role in the User Service database:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'RESTAURANT_OWNER'
FROM users
WHERE email = lower('owner@example.com')
ON CONFLICT (user_id, role) DO NOTHING;
```

Log in again after provisioning so the newly issued JWT includes the role.

Search uses PostgreSQL name matching and bounded pagination. The separate
Search Service can later build richer query indexes without changing restaurant
ownership or persistence.
