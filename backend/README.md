# Backend development

## Start on a newly cloned machine

Follow the [root quick start](../README.md): install JDK 25/Maven/MySQL, create an **empty** `ktpm_sales_db`, then run `scripts/run-backend.ps1` from the repository root. The script prompts for the local MySQL password and generates a temporary JWT secret if one is not already set. Flyway creates the schema and demo users on first startup.

Do not apply `database/schema.sql` to that empty database before starting the backend: Flyway V1 also creates those tables. A database with existing tables but no `flyway_schema_history` needs separate review; do not reset it or enable automatic baselining without checking its data and schema.

To run Maven directly instead of the script, set `DB_PASSWORD` and `JWT_SECRET` in your own process environment, then run `mvn spring-boot:run` from `backend/`. Never commit either value.

## Required environment variables

- `DB_URL` (optional local default targets `ktpm_sales_db`)
- `DB_USERNAME` (optional local default: `root`)
- `DB_PASSWORD` (no committed default secret)
- `JWT_SECRET` (required, at least 32 bytes)
- `JWT_EXPIRATION` (optional, milliseconds; default `3600000`)
- `CORS_ALLOWED_ORIGINS` (optional comma-separated localhost origins)

No `.env` file or secret is committed.

## Demo authentication data

Flyway migration `V2__seed_demo_users.sql` inserts three development/demo accounts:

| Username | Role |
|---|---|
| `admin01` | `ADMIN` |
| `sales01` | `SALES_STAFF` |
| `warehouse01` | `WAREHOUSE_STAFF` |

Development-only demo password: `Demo@123`. The database stores only its BCrypt hash.

## Stateless logout

`POST /api/auth/logout` validates the bearer JWT and acknowledges logout. The backend stores no HTTP login session, token blacklist or logout table. The client must delete its access token; an already-issued token remains valid until expiration.

The `/api/admin/ping`, `/api/sales/ping` and `/api/warehouse/ping` routes are security verification endpoints, not final business APIs.
