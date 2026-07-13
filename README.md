# FundooNotes Backend

Token-based note-taking backend, built on the same Spring Boot patterns as
`springboot-greeting-app`, covering the 10 modules from the spec:

1. User Management (register / login / password recovery)
2. Authentication & Authorization (opaque bearer tokens)
3. Notes Management (create / delete)
4. Pin / Archive / Trash
5. Search & Filter
6. Tags / Labels Management
7. Reminder & Notification (scheduled email reminders)
8. File Attachment (optional, local disk upload)
9. Asynchronous Operations via JMS
10. Token Caching via Redis

## Data architecture (important — read this first)

- **Redis is the PRIMARY datastore.** Every API read and write for `User`,
  `Note`, and `Tag` goes directly through Redis (via Spring Data Redis
  `@RedisHash` repositories) — there is no H2, and Postgres is never read
  from during a normal request.
- **PostgreSQL is a SECONDARY / backup datastore.** Whenever something is
  written to Redis, the app publishes a message onto a JMS queue
  (`fundoo.sync.queue`, backed by an **embedded Artemis broker** — no
  separate broker install needed). A background `@JmsListener`
  (`SyncListener`) consumes that message off the request thread and mirrors
  the change into Postgres via JPA (`*_backup` tables). This is module 9
  ("Asynchronous Operations via JMS") and module 10 ("Token Caching via
  Redis") working together: Redis stays fast and authoritative, Postgres is
  an eventually-consistent audit/backup copy.
- Auth tokens (opaque UUIDs) are cached in Redis directly via
  `RedisTemplate`, separate from the entity repositories, with a 2 hour TTL.

## What changed vs. the reference `springboot-greeting-app`

- Package renamed `com.greet` → `com.fundoo`; `Greeting` → `Note`.
- `H2` removed entirely; `PostgreSQL` added as the JPA-backed secondary store.
- `RabbitMQ`/AMQP removed; replaced with `JMS` (embedded Artemis broker).
- Google OAuth2 login was dropped (not in the spec's 10 modules) to avoid
  requiring real Google API credentials just to boot the app locally. It can
  be added back on request.
- `Long` auto-increment IDs (JPA/H2 style) replaced with `String` UUIDs,
  since Redis is now the primary store and doesn't auto-increment.
- Packaging changed from a runnable JAR to a **WAR**, deployed to an
  external **Apache Tomcat** server (`ServletInitializer.java` added for
  this). No Docker is used anywhere — Redis and PostgreSQL are installed
  and run natively.

## Prerequisites

- Java 17+
- Maven
- Redis (installed locally — no Docker)
- PostgreSQL (installed locally — no Docker)
- Apache Tomcat 10.x ([download here](https://tomcat.apache.org/download-10.cgi))
  — required because the app is packaged as a WAR for deployment to an
  external Tomcat, not run via an embedded server

## Step 1 — Install and start Redis and PostgreSQL

**Redis:**

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y redis-server
sudo systemctl enable --now redis-server

# macOS (Homebrew)
brew install redis
brew services start redis

# Windows
# Use WSL and follow the Ubuntu steps above, or install Redis via
# https://github.com/microsoftarchive/redis/releases
```

Verify it's running: `redis-cli ping` → should print `PONG`.

**PostgreSQL:**

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y postgresql
sudo systemctl enable --now postgresql

# macOS (Homebrew)
brew install postgresql@16
brew services start postgresql@16
```

Create the database used for the backup store:

```bash
sudo -u postgres psql -c "CREATE DATABASE fundoodb;"
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
```

(No manual table setup needed — Hibernate creates the `*_backup` tables
automatically via `ddl-auto: update` the first time the app starts.)

## Step 2 — Configure `.env`

A `.env` file is already included at the project root with working local
defaults for Redis/Postgres. You only need to edit it if:

- Your Redis/Postgres are running somewhere other than `localhost`.
- You want real reminder/welcome emails to send — set `SMTP_USERNAME` and
  `SMTP_PASSWORD` to a Gmail address and an
  [App Password](https://myaccount.google.com/apppasswords) (regular Gmail
  passwords won't work). If you leave these as placeholders, the app still
  runs fine — email sends just fail silently and get logged.

## Step 3 — Install Tomcat (skip if you already have one running)

```bash
# Download and extract (adjust version as needed)
curl -O https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.28/bin/apache-tomcat-10.1.28.tar.gz
tar xzf apache-tomcat-10.1.28.tar.gz
mv apache-tomcat-10.1.28 ~/tomcat
export CATALINA_HOME=~/tomcat
chmod +x $CATALINA_HOME/bin/*.sh
```

Tomcat listens on port **8080** by default.

## Step 4 — Build the WAR and deploy it to Tomcat

Build the WAR file:

```bash
mvn clean package
```

This produces `target/fundoonotes-backend.war`.

Deploy it by copying the WAR into Tomcat's `webapps` folder — Tomcat
auto-deploys anything dropped in there:

```bash
cp target/fundoonotes-backend.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

Watch the deployment log to confirm it started cleanly:

```bash
tail -f $CATALINA_HOME/logs/catalina.out
```

Look for `Started Application in ... seconds` with no stack traces above it.

**Important — the base URL changes when deployed to an external Tomcat.**
When run via the embedded server (`mvn spring-boot:run`, e.g. for local
dev/debugging), the app uses the `server.servlet.context-path: /api` from
`application.yml`, so it's at `http://localhost:8082/api`.

When deployed as a WAR to an **external** Tomcat, the container assigns the
context path from the WAR's filename instead, and `server.servlet.context-path`
is ignored. So with `fundoonotes-backend.war`, everything in this README
under `http://localhost:8082/api/...` becomes:

```
http://localhost:8080/fundoonotes-backend/...
```

(e.g. `POST http://localhost:8080/fundoonotes-backend/auth/register`,
Swagger UI at `http://localhost:8080/fundoonotes-backend/swagger-ui/index.html`).

If you'd rather it be served at the root path (`http://localhost:8080/...`,
no `/fundoonotes-backend` prefix), rename the file to `ROOT.war` before
copying it into `webapps/`.

To stop Tomcat: `$CATALINA_HOME/bin/shutdown.sh`

### Local development without Tomcat

You can still run the app directly with the embedded server while
developing, without touching Tomcat at all:

```bash
mvn spring-boot:run
```
This uses `http://localhost:8082/api` as before (see the rest of this README).

## Step 5 — Test it end-to-end

You can use the commands below (curl), or import them into Postman/Swagger UI.

> The commands below use `http://localhost:8082/api` (embedded server /
> `mvn spring-boot:run`). If you deployed the WAR to Tomcat instead, replace
> that with `http://localhost:8080/fundoonotes-backend` (see Step 4) in
> every command.

### 4.1 Register a user

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123","email":"alice@example.com"}'
```
Expected: `Registration successful.`

### 4.2 Log in (get a bearer token)

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'
```
Expected: JSON with `accessToken` and `username`. Copy the token — you'll
use it as `TOKEN` below.

```bash
export TOKEN="paste-the-accessToken-here"
```

### 4.3 Create a note

```bash
curl -X POST http://localhost:8082/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"Buy groceries"}'
```
Expected: JSON note object with a generated `id`. Copy it as `NOTE_ID`.

### 4.4 Pin / archive / trash

```bash
curl -X PUT http://localhost:8082/api/notes/$NOTE_ID/pin -H "Authorization: Bearer $TOKEN"
curl -X PUT http://localhost:8082/api/notes/$NOTE_ID/archive -H "Authorization: Bearer $TOKEN"
curl -X PUT http://localhost:8082/api/notes/$NOTE_ID/trash -H "Authorization: Bearer $TOKEN"
```

### 4.5 Add a tag

```bash
curl -X POST "http://localhost:8082/api/notes/$NOTE_ID/tags?tagName=personal" \
  -H "Authorization: Bearer $TOKEN"
```

### 4.6 Search & filter

```bash
curl "http://localhost:8082/api/notes/search?query=grocer&pinned=true" \
  -H "Authorization: Bearer $TOKEN"
```

### 4.7 Delete a note

```bash
curl -X DELETE http://localhost:8082/api/notes/$NOTE_ID -H "Authorization: Bearer $TOKEN"
```

## Step 6 — Verify the Redis (primary) vs Postgres (backup) split

**Check Redis has the data immediately (primary, synchronous):**

```bash
docker exec -it fundoo-redis redis-cli KEYS "*"
docker exec -it fundoo-redis redis-cli HGETALL "Note:<paste-a-note-id>"
```

**Check Postgres has the mirrored copy (secondary, async — may take a
second to appear because it's replicated via a JMS listener):**

```bash
docker exec -it fundoo-postgres psql -U postgres -d fundoodb -c "SELECT id, message, creator_username, pinned FROM notes_backup;"
docker exec -it fundoo-postgres psql -U postgres -d fundoodb -c "SELECT id, username, email FROM users_backup;"
```

If a note you deleted via the API still briefly appears in `notes_backup`,
that's expected — the delete is propagated asynchronously and will
disappear within a moment.

## Step 7 — Verify reminders

Create a note with a `reminderTime` a minute in the future:

```bash
curl -X POST http://localhost:8082/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Call mom\",\"reminderTime\":\"$(date -u -d '+1 minute' +%Y-%m-%dT%H:%M:%S)\"}"
```

Watch the application logs — `NoteScheduler` scans every 60 seconds and will
log `Sent reminder alert for Note ID: ...` once it's due (and attempt to
email the user, if SMTP credentials are configured).

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| App fails to start with a Redis connection error | Redis isn't running — `sudo systemctl status redis-server` / `redis-cli ping`, or check `REDIS_HOST`/`REDIS_PORT` in `.env` |
| App fails to start with a Postgres connection error | Postgres isn't running, or `fundoodb` doesn't exist — `sudo systemctl status postgresql`, or re-run the `CREATE DATABASE` command from Step 1 |
| WAR doesn't deploy / Tomcat shows a 404 for everything | Check `$CATALINA_HOME/logs/catalina.out` for a startup stack trace; confirm the WAR actually copied into `webapps/` and that you're hitting the right context path (see the URL note in Step 4) |
| `notes_backup` / `users_backup` tables stay empty | Check `catalina.out` for `SyncListener` errors; confirm the embedded Artemis broker started (look for `ARTEMIS` log lines at boot) |
| Emails never arrive | Expected if `SMTP_USERNAME`/`SMTP_PASSWORD` are placeholders — check logs for `Failed to send ... email`, this is caught and non-fatal |
| `401 Unauthorized` on `/notes/**` | Missing or expired `Authorization: Bearer <token>` header — log in again |
# fundoo_backend
