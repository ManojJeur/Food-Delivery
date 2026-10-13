#!/bin/sh
set -eu

psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    --set=app_user="$DB_USERNAME" \
    --set=app_password="$DB_PASSWORD" \
    --set=ON_ERROR_STOP=1 <<'SQL'
SELECT format('CREATE ROLE %I WITH LOGIN PASSWORD %L', :'app_user', :'app_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'app_user')
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', databases.database_name, :'app_user')
FROM (VALUES ('user_db'), ('restaurant_db'), ('menu_db'), ('search_db'),
             ('order_db'), ('payment_db'), ('delivery_db')) AS databases(database_name)
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = databases.database_name)
\gexec
SQL
