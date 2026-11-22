# Running CivicCareID PH locally

Needs **Java 21** and **Docker Desktop** (running). Gradle is not needed: use the `gradlew` wrapper.

## Start

```bash
docker compose up -d                                   # Postgres on localhost:5432
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'   # API on http://localhost:8080
```

On Windows PowerShell use `.\gradlew.bat bootRun --args='--spring.profiles.active=dev'`.

- Swagger UI: http://localhost:8080/swagger-ui.html (log in, then click **Authorize** and paste the token)
- Health: http://localhost:8080/actuator/health

The `dev` profile seeds **synthetic** demo accounts (password `Demo1234!`):

| Email | Role |
|---|---|
| `lgu@civiccare.test` | LGU_STAFF (3 sample registrations waiting in the queue) |
| `senior@civiccare.test` | SENIOR_CITIZEN (not registered yet; use it to register live) |
| `establishment@civiccare.test` | ESTABLISHMENT |
| `admin@civiccare.test` | GOV_ADMIN |

Reset the local database: `docker compose down -v` then start again.

## Run the frontend (second terminal)

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173 (calls to /api are proxied to the backend on :8080)
```

## Run the single image locally (same as Render)

From the repository root (the folder that contains `frontend/` and `backend/`):

```bash
docker build -t civiccareid .
docker run --rm -p 8080:8080 --env-file .env civiccareid   # site AND API on http://localhost:8080
```

Use `SPRING_PROFILES_ACTIVE=dev` in `.env`, and point `SPRING_DATASOURCE_URL` at
`jdbc:postgresql://host.docker.internal:5432/civiccare` so the container can reach the compose Postgres.

## Test

```bash
cd backend
./gradlew test     # unit tests + integration tests (Testcontainers starts its own Postgres)
```

## Try the flow with curl

```bash
B=http://localhost:8080/api/v1
TOKEN=$(curl -s -X POST $B/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"lgu@civiccare.test","password":"Demo1234!"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
curl -s "$B/registrations?status=SUBMITTED" -H "Authorization: Bearer $TOKEN"
```

## Endpoints available now (Sprint 1 slice)

| Method & path | Role | Notes |
|---|---|---|
| `POST /api/v1/auth/signup` | public | Senior citizen self sign-up `{email, password, fullName}` |
| `POST /api/v1/auth/login` | public | Returns `{accessToken, expiresAt, role}` |
| `GET /api/v1/me` | any | Own account |
| `GET /api/v1/me/registration` | SC | Own registration, ID card and ledger status (404 if none) |
| `POST /api/v1/registrations` | SC | `{fullName, birthDate, address, contactNumber?, govIdNumber, lguCode}` |
| `GET /api/v1/registrations?status=&page=&size=` | LGU, GOV | Paginated queue, oldest first |
| `GET /api/v1/registrations/{id}` | LGU, GOV | Details |
| `POST /api/v1/registrations/{id}/approve` | LGU | Issues ID card, queues chain write (`ledgerStatus: PENDING`) |
| `POST /api/v1/registrations/{id}/reject` | LGU | `{reason}` |
| `POST /api/v1/registrations/{id}/revoke` | LGU, GOV | `{reason}`, queues chain write |

Errors use RFC 7807 (`{title, status, detail}`; validation errors add `errors: {field: message}`).

## Deploying to Render (one service)

1. New > Web Service > connect the repo, branch `main`, **Runtime: Docker**, leave Root Directory empty,
   Dockerfile Path `./Dockerfile`. (Or use `render.yaml` as a Blueprint.)
2. Health Check Path: `/actuator/health`.
3. Environment variables: `SPRING_PROFILES_ACTIVE=staging`, `SPRING_DATASOURCE_URL` (JDBC form),
   `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SIGNING_KEY`, `ID_HMAC_SECRET`.
   Optional: `DEMO_DATA_ENABLED=true` with a strong `DEMO_PASSWORD` for the demo accounts.
   For the database values, see "Supabase setup" below.
4. Open the service URL: the React site loads at `/`, the API answers at `/api/v1/**`, Swagger at `/swagger-ui.html`.

## Supabase setup (staging database)

Supabase is used as a plain managed Postgres. The backend connects over JDBC and Liquibase creates the tables.

1. **Connection string:** in the Supabase project, open **Connect** and copy the **Session pooler** string
   (port 5432). Do not use the direct connection: it is IPv6-only and Render cannot reach it. Avoid the
   transaction pooler (port 6543) too, since it breaks JDBC prepared statements.
   Set the Render variables like this:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME=postgres.<project-ref>` (the pooler needs the project ref in the username)
   - `SPRING_DATASOURCE_PASSWORD=<database password>`
2. **Turn off the Data API** (Project Settings > Data API). We don't use it, and if it is on, Supabase can
   publish tables in the `public` schema through a REST API that only needs the public key. Our tables hold
   personal data and do not use Supabase's row-level security.
3. **Schema changes go through Liquibase only** (`backend/src/main/resources/db/changelog`). The app checks
   the schema at startup (`ddl-auto=validate`) and will not start if a table was created or edited by hand
   in the Supabase table editor. Browsing data in the table editor is fine.
4. **Free projects pause** after about a week of low activity. Open the Supabase dashboard before a demo
   and restore the project if needed (on top of Render's 15-minute sleep).
5. Local development and tests do not use Supabase: they use `docker compose` and Testcontainers.

## Environment variables

See `.env.example`. In `dev` nothing is required. In `staging` the app refuses to start without
`SPRING_DATASOURCE_*`, `JWT_SIGNING_KEY` and `ID_HMAC_SECRET` (each secret at least 32 characters).
