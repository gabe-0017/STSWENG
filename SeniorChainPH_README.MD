# SeniorChain PH — Project Context (README)

> **Purpose of this file:** a compact context file for humans and AI agents working on this repo.
> Paste it into your agent's context / workspace instructions. The full rationale and detail live in
> `SeniorChainPH_Development-Plan.docx` — this file and that plan describe the **same** architecture.

**Course:** STSWENG (Advanced Software Engineering) · **Team:** Group 05 · **Version:** 1.0 (Nov 2026)

---

## 1. What we're building

A nationwide platform where **Senior Citizens (SCs)** register **once** and receive **one standard SC ID**
(digital + printable, with a QR code). Approved records are anchored on a **blockchain** so that:

- duplicate/fake IDs are detected (one record per person nationwide),
- establishments can **verify eligibility without seeing private details**,
- establishments **log discounts** and **file tax relief claims** the government can validate.

Scope: **SC IDs first**; data model already supports **PWD IDs later** (`recordType = SC | PWD`).

### Users / roles
| Role | Can do |
|---|---|
| `SENIOR_CITIZEN` | Submit registration, view own ID card / QR |
| `LGU_STAFF` (LGU / OSCA) | Review queue, approve / reject / revoke registrations |
| `ESTABLISHMENT` | Scan & verify IDs, log discounts, file tax relief claims |
| `GOV_ADMIN` (NCSC, BIR) | Oversee registry, validate claims, run ledger consistency check |

---

## 2. Architecture (one paragraph)

**Modular monolith.** One **Spring Boot** API (also acts as the API gateway) serves an already-built
static frontend. **PostgreSQL** holds all personal data and workflows. A small **Solidity smart contract**
holds **only hashes + status** of approved records. The backend writes to the chain **asynchronously**
through an **outbox table**, so requests never wait on the blockchain. Verification reads hit an
in-memory **Caffeine** cache → Postgres; the chain is the tamper-evident source of truth for audits.

```
Frontend (static, already built) ──► Vercel / Netlify / Cloudflare Pages (CDN + HTTPS)
        │ HTTPS + JWT
        ▼
Spring Boot API (modular monolith, also the "gateway") ──► Docker on Render
   ├── PostgreSQL (Neon/Supabase)   personal data, accounts, workflows, discounts, claims, outbox
   ├── Caffeine (in-process cache)  verification lookups, read-only chain calls
   └── web3j ──► Smart contract     local Hardhat node (dev/CI) · Ethereum Sepolia (staging)
                                     stores idHmac → {status, recordType, validUntil}
```

**Primary option = Option B (smart contract)** — matches our CI plan.
**Fallback = Option A:** replace the contract with an append-only, hash-chained `ledger_entry` table in
Postgres. Only the `ChainGateway` implementation changes; the domain `LedgerService` interface stays the same.

---

## 3. Tech stack

| Layer | Choice |
|---|---|
| Language / runtime | **Java 21** (LTS), **Spring Boot 4.1**, Gradle |
| Web / API | Spring Web (REST, JSON), springdoc-openapi (Swagger UI), RFC 7807 `ProblemDetail` errors |
| Auth | Spring Security + OAuth2 Resource Server (self-issued JWT), BCrypt passwords, `@PreAuthorize` per role |
| Validation | `spring-boot-starter-validation` |
| Persistence | Spring Data JPA (Hibernate), **PostgreSQL 16/17**, `ddl-auto=validate` |
| Migrations | **Liquibase** (`db/changelog` in `main/resources`) |
| Caching | Spring Cache + **Caffeine** (no Redis) |
| Rate limiting | **Bucket4j** on `/verify` and `/auth/login` |
| Blockchain | **Solidity** + **Hardhat 3** + **OpenZeppelin AccessControl**; **web3j** Java wrapper; **Slither** static analysis |
| Chains | Local `npx hardhat node` (dev/CI) · **Sepolia testnet** via Alchemy/Infura (staging) |
| QR | Frontend library, or **ZXing** if generated server-side |
| Health / metrics | Spring Boot Actuator |
| Container | Docker multi-stage, `eclipse-temurin:21-jre` |
| Hosting | **Render** (API) · **Neon or Supabase** (Postgres) · Vercel/Netlify/Cloudflare Pages (frontend) |
| CI/CD | **GitHub Actions** (`.github/workflows/ci.yml`), 5 phases — see §8 |
| Testing | JUnit 5, Mockito, **Testcontainers** (Postgres), Hardhat Solidity/TS tests, JaCoCo, k6 (load) |

**Explicitly NOT used:** microservices, Kubernetes, separate API gateway product, Redis, Kafka/RabbitMQ,
IPFS, Hyperledger Fabric, Rust, personal data on-chain, smart contracts for discounts/claims.

---

## 4. Code layout (DDD)

```
src/main/java/ph/seniorchain/
  domain/            # INSIDE: entities, value objects, repositories, domain services
    registration/    SeniorCitizen, Registration, RegistrationStatus, BirthDate(VO), IdHmac(VO)
    verification/    VerificationToken(VO), EligibilityResult(VO)
    discount/        Establishment, DiscountRecord, Money(VO)
    claims/          TaxReliefClaim, ClaimStatus
    ledger/          LedgerService (interface — domain does not know it's a blockchain)
  application/       # OUTSIDE: EmailService, QrService, TokenService, ChainGateway (web3j impl), OutboxProcessor
  web/               # OUTSIDE: controllers, DTOs, GlobalExceptionHandler (@RestControllerAdvice)
  config/            # Security, CORS, cache, web3j, rate-limit beans
contracts/           # Solidity: SeniorRegistry.sol, tests (*.t.sol), Hardhat config
```

Conventions: domain classes override `equals/hashCode/toString`; use business methods (`approve()`,
`revoke()`), not setters; validate all parameters; immutable value objects; ubiquitous language from §1.

---

## 5. Key implementation rules (agents: follow these)

1. **State machine:** `SUBMITTED → APPROVED → (AMENDED) → EXPIRED | REVOKED`, plus `SUBMITTED → REJECTED`.
   Only transitions out of `SUBMITTED` into `APPROVED`, and later `AMENDED/REVOKED/EXPIRED`, go to the chain.
   Use JPA `@Version` (optimistic locking) on `Registration`.
2. **Duplicate detection:** `idHmac = HMAC-SHA256(secretKey, normalizedGovIdNumber)`. **Never a plain hash**
   (ID numbers are brute-forceable). Unique index in Postgres; same value is the on-chain key.
3. **QR = signed token**, not a raw record ID: `{recordId, exp, signature}` signed by the server.
4. **`POST /verify` returns only** `{eligible, type, status, validUntil}` — no name, birthdate, address.
5. **Outbox pattern:** approval + `chain_outbox` insert happen in **one DB transaction**; a `@Scheduled`
   job sends pending rows to the contract (batched), marks `CONFIRMED`, retries on failure.
   UI shows "Approved – pending on-chain" until confirmed.
6. **Idempotent discounts:** `POST /discounts` requires `Idempotency-Key` header (unique column).
   Money is `BigDecimal` inside a `Money` value object.
7. **Scheduled jobs:** outbox processor (10–30 s), daily expiry job, nightly chain-consistency check.
8. **Cache invalidation:** evict a record's verification cache entry **immediately** on revoke/expire/amend. TTL 30–60 s.
9. **Reports/claims:** plain SQL/JPQL projections, not entity loading.
10. **Secrets** (JWT key, HMAC key, wallet private key, DB URL, RPC URL) only in env vars / GitHub secrets.
    **Never commit keys or real personal data.** All tests use synthetic SC records (Data Privacy Act of 2012).

---

## 6. API (v1, prefix `/api/v1`)

| Method & path | Role | Notes |
|---|---|---|
| `POST /auth/login` | public | returns JWT; rate limited |
| `POST /auth/signup` | public | SC self sign-up (other roles are created by an admin) |
| `GET /me` | any | own account |
| `GET /me/registration` | SC | own registration + ID card + `ledgerStatus` (PENDING = "pending on-chain") |
| `POST /registrations` | SC | creates `SUBMITTED` |
| `GET /registrations?status=SUBMITTED` | LGU | paginated queue |
| `POST /registrations/{id}/approve` | LGU | → outbox → chain |
| `POST /registrations/{id}/reject` | LGU | reason required |
| `POST /registrations/{id}/revoke` | LGU, GOV | → outbox → chain |
| `GET /me/id-card` | SC | ID data + signed QR token |
| `POST /verify` | ESTABLISHMENT | minimal result; rate limited; cached |
| `POST /discounts` | ESTABLISHMENT | idempotent |
| `GET /discounts?from=&to=` | ESTABLISHMENT | paginated |
| `POST /claims` | ESTABLISHMENT | built from verified discount records |
| `GET /claims`, `POST /claims/{id}/validate` | GOV | read-only review + validation |
| `GET /admin/ledger/consistency` | GOV | compares Postgres vs chain |

---

## 7. Smart contract (`SeniorRegistry.sol`)

```
struct Record { uint8 status; uint8 recordType; uint64 validUntil; uint64 updatedAt; }
mapping(bytes32 => Record) records;            // key = idHmac
register(idHmac, type, validUntil)  onlyRole(ISSUER_ROLE)
registerBatch(bytes32[] ...)        onlyRole(ISSUER_ROLE)
amend / revoke / expire(idHmac)     onlyRole(ISSUER_ROLE)
isEligible(idHmac) view → bool      // free read
events: Registered, Amended, Revoked, Expired
```
Only the backend wallet holds `ISSUER_ROLE`. Discounts/claims stay off-chain (optional: periodic Merkle-root anchor).

---

## 8. Environments & CI

| Env | Stack |
|---|---|
| Local | `docker compose up` → Postgres + Hardhat node; Spring profile `dev` |
| CI | Testcontainers Postgres + Hardhat node; synthetic data only |
| Staging | Render + Neon/Supabase + Sepolia; profile `staging` |

Branching: simplified GitFlow (`main`, `develop`, `feature/<id>-<name>`, `hotfix/<name>`); PR + 1 approval + green CI to merge.

| CI phase | Tools |
|---|---|
| 1 Checkout & setup | setup-java (Temurin 21, Gradle cache), setup-node (npm cache) |
| 2 Code quality | Spotless/Checkstyle, ESLint, Slither, OWASP Dependency-Check / `npm audit` |
| 3 Build | `./gradlew build -x test`, `npx hardhat compile`, frontend build → artifacts |
| 4 Test | JUnit + Mockito, `npx hardhat test`, Testcontainers E2E (register→approve→verify, duplicate, expired), JaCoCo |
| 5 Deploy | Render deploy hook (secret); deploy contract to Sepolia only when changed |

Triggers: push to `feature/*` → 1–2 + unit tests · PR to `develop` → 1–4 · merge to `develop` → 1–5 (staging) · tag `v*` on `main` → release.

---

## 9. Performance rules

- **Bottleneck is I/O** (DB, RPC, ~12 s Sepolia blocks), not CPU → **no Rust**, no second language.
- Measure first: Actuator metrics + **k6** load test on `/verify`; keep before/after numbers.
- Indexes on filtered columns; no N+1 (`JOIN FETCH` / DTO projections); paginate every list; Hikari pool 5–10.
- Chain stays **off the request path** (outbox); **batch** chain writes.
- `spring.threads.virtual.enabled=true` (Java 21 virtual threads).
- Caffeine cache for verification + chain reads.
- Render free (512 MB): `JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"`, lean deps, enable CDS.
  Free instance sleeps after 15 min idle → **warm up before demos**. Stretch: GraalVM native image.

---

## 10. Build order

1. Auth/roles + SC registration + LGU approval (Postgres only)
2. Signed QR token + `/verify`
3. Contract + Hardhat tests + outbox → chain
4. Discounts → claims → government view
5. Caching, rate limiting, consistency check, staging polish, load test
