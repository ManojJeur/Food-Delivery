# Menu Service

Owns restaurant menu categories and food items in `menu_db`. It does not query
the Restaurant Service database. Menu mutations forward the caller's JWT to a
Restaurant Service ownership check; the menu service fails closed if that
service cannot verify access. Restaurant and user IDs remain UUID references
across service boundaries, without cross-database foreign keys.

## Local setup

Start Docker PostgreSQL and the Service Registry. The Compose init script adds
`menu_db` only when PostgreSQL first initializes its data volume. If your
volume already existed before `menu_db` was added, create it once as a database
administrator, owned by the configured `DB_USERNAME`. For example, at a `psql`
prompt connected to `postgres`:

```sql
CREATE DATABASE menu_db OWNER fooddelivery_app;
```

Replace `fooddelivery_app` with the `DB_USERNAME` in your `.env`. Load the
project `.env` into the PowerShell process before launching from Maven. STS
does not load `.env` automatically. In the Menu Service Run Configuration's
Environment tab, add `DB_HOST=localhost`, `DB_PORT=5432`,
`DB_USERNAME` and `DB_PASSWORD` from `.env`, `MENU_DB_NAME=menu_db`,
`RESTAURANT_SERVICE_URL=http://localhost:8082`, and
`EUREKA_SERVER_URL=http://localhost:8761/eureka/`.
The service uses `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`,
`MENU_DB_NAME` (defaults to `menu_db`), `JWT_SECRET`, and `EUREKA_SERVER_URL`.
`RESTAURANT_SERVICE_URL` defaults to `http://localhost:8082`; in a container
network, set it to the Restaurant Service hostname and port.

From this module directory:

```powershell
mvn spring-boot:run
```

The service listens on port `8083` and registers with Eureka as `menu-service`.
Flyway owns schema changes and Hibernate validates the schema. Swagger UI is
at <http://localhost:8083/swagger-ui.html>.

## API

Public menu reads return only active categories and active, available items
from active categories. Item search supports keyword, category, vegetarian,
price range, sort, and pagination filters.

```text
GET /api/v1/menu/restaurants/{restaurantId}/categories
GET /api/v1/menu/restaurants/{restaurantId}/items?keyword=rice&categoryId={uuid}&vegetarian=true&minPrice=5&maxPrice=20&page=0&size=20&sort=price,asc
GET /api/v1/menu/items/{itemId}
POST /api/v1/menu/items/quote
```

The quote endpoint accepts `{ "itemIds": ["<uuid>", "<uuid>"] }` and returns
current prices for all requested items only if each one is active and
available. Order Service uses this to validate checkout and snapshot prices.

Restaurant owners and admins can manage categories and items. Category
deactivation hides its items from public reads. New items start unavailable;
owners explicitly toggle availability. Item deletion is a soft delete.

```text
GET    /api/v1/menu/restaurants/{restaurantId}/management/categories
POST   /api/v1/menu/categories
PUT    /api/v1/menu/categories/{categoryId}
PATCH  /api/v1/menu/categories/{categoryId}/active
GET    /api/v1/menu/restaurants/{restaurantId}/management/items
POST   /api/v1/menu/items
PUT    /api/v1/menu/items/{itemId}
PATCH  /api/v1/menu/items/{itemId}/availability
DELETE /api/v1/menu/items/{itemId}
```

All management requests require a JWT with `RESTAURANT_OWNER` or `ADMIN`. The
Restaurant Service confirms the caller owns the restaurant (or is an admin).
Item/category IDs are UUIDs. The schema enforces category-to-restaurant
consistency with a composite foreign key, price and preparation-time bounds,
and case-insensitive unique category names per restaurant. Optimistic locking
turns conflicting edits into HTTP 409 responses.

## Example requests

Create a category:

```json
{
  "restaurantId": "<restaurant-uuid>",
  "name": "Main Course",
  "description": "Rice and noodle dishes",
  "displayOrder": 1
}
```

Create an item (it remains unavailable until explicitly enabled):

```json
{
  "restaurantId": "<restaurant-uuid>",
  "categoryId": "<category-uuid>",
  "name": "Vegetable Fried Rice",
  "description": "Wok-fried rice with seasonal vegetables",
  "price": 9.50,
  "imageUrl": null,
  "vegetarian": true,
  "preparationTimeMinutes": 15
}
```

Then set `PATCH /api/v1/menu/items/{itemId}/availability` with
`{"available":true}` when the item is ready to be listed.
