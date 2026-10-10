# User Service

Owns user identity, role assignment, refresh-token records, profiles, and
addresses. It stores only BCrypt password hashes and SHA-256 hashes of opaque
refresh tokens. New public registrations always receive `CUSTOMER`; users
cannot submit a role or change their own roles.

## Run locally

Start PostgreSQL and the Service Registry. From the repository root, create
your local environment file and start the PostgreSQL container:

```powershell
$copyFrom = ".env.example"
Copy-Item $copyFrom .env
# Edit .env and replace DB_PASSWORD with your own local password.
docker compose up -d postgres
```

The container publishes PostgreSQL only on `127.0.0.1:5432`, persists its data
in the `fooddelivery_postgres_data` volume, and creates `user_db` on first
startup. Wait until `docker compose ps` reports the container as healthy.

When launching from PowerShell, load the local values from `.env` and start the
service:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#=][^=]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2], 'Process')
    }
}
cd services/user-service
mvn spring-boot:run
```

The service listens on port `8081` by default and registers as `user-service`.
Flyway creates its tables on startup; Hibernate validates the migrated schema.
The JWT key must be at least 32 characters and match the API Gateway key. Use a
managed secret in deployed environments.

STS does not automatically read `.env`. In the User Service Run Configuration,
add `DB_HOST=localhost`, `DB_PORT=5432`, `DB_NAME=user_db`, `DB_USERNAME`, and
`DB_PASSWORD` from the local `.env`. The JWT secret can use the matching local
development fallback configured by both services, or be set explicitly in
both run configurations.

Swagger UI: <http://localhost:8081/swagger-ui.html>

To stop the database container while keeping its data, run
`docker compose stop postgres`. Starting it again with `docker compose up -d
postgres` reuses the named volume. Flyway applies migrations when the User
Service starts.

## API

Public endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

Authenticated endpoints:

```text
GET    /api/v1/users/me
PATCH  /api/v1/users/me
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PUT    /api/v1/users/me/addresses/{addressId}
DELETE /api/v1/users/me/addresses/{addressId}
```

User and address IDs are derived from the verified JWT subject. Address lookup
includes the authenticated owner ID, so another user's address is not
disclosed.

## Token behavior

Access tokens are signed HS256 JWTs with a 15-minute default lifetime. Refresh
tokens are random opaque values, returned only once, persisted as SHA-256 hashes,
and rotated on every refresh. The old refresh record is locked and revoked in
the same database transaction that creates the replacement. Replaying an old
refresh token fails. Logout revokes the supplied refresh token; already-issued
access tokens remain valid until their short expiration.

The HS256 key is shared with the gateway so both components can issue/verify
the same token format. This is a simple learning-project boundary; a later
production deployment can move to asymmetric signing so services verify with a
public key and only the identity service holds the private signing key.
