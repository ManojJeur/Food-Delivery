# Local PostgreSQL

`docker-compose.yml` runs the official PostgreSQL 17 image, binds port `5432`
to localhost, and persists data in a named Docker volume. Copy `.env.example`
to `.env`; Compose uses the local PostgreSQL admin credentials to initialize a
least-privilege `fooddelivery_app` role and the per-service databases.

The initialization script runs only when PostgreSQL creates a fresh data
directory. If an existing volume predates the script, create any missing
service database as an administrator before starting that service. Do not
remove the data volume just to rerun initialization.

For local use:

```powershell
docker compose up -d postgres
docker compose ps
```

Wait for the `postgres` health status before starting a Spring service. Spring
applications connect to `localhost:5432` with the `DB_USERNAME` and
`DB_PASSWORD` values from `.env`; the admin user is only for local database
administration.
