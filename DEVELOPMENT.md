# Running SeniorChain PH locally

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
| `lgu@seniorchain.test` | LGU_STAFF (3 sample registrations waiting in the queue) |
| `senior@seniorchain.test` | SENIOR_CITIZEN (not registered yet; use it to register live) |
| `establishment@seniorchain.test` | ESTABLISHMENT |
| `admin@seniorchain.test` | GOV_ADMIN |

Reset the local database: `docker compose down -v` then start again.

## Test

```bash
cd backend
./gradlew test     # unit tests + integration tests (Testcontainers starts its own Postgres)
```

## Try the flow with curl

```bash
B=http://localhost:8080/api/v1
TOKEN=$(curl -s -X POST $B/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"lgu@seniorchain.test","password":"Demo1234!"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
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

## Environment variables

See `.env.example`. In `dev` nothing is required. In `staging` the app refuses to start without
`SPRING_DATASOURCE_*`, `JWT_SIGNING_KEY` and `ID_HMAC_SECRET` (each secret at least 32 characters).
