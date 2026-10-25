# Backend development

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
