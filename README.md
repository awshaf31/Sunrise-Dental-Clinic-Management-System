# Sunrise Dental Clinic Management System

Appointment and patient management system for a dental clinic — booking,
scheduling, billing, and reporting for receptionists and managers.

Built deliberately **without an application framework**: Jakarta Servlets and
JSP for the web tier, plain JDBC for persistence. The only third-party
libraries are [Gson](https://github.com/google/gson) (JSON), [BCrypt](https://github.com/patrickfav/bcrypt)
(password hashing), and the JUnit/Mockito test stack — none of which run the
application or manage its objects the way a framework's container would.

## Features

- **Authentication** — session-based login with BCrypt-hashed passwords and
  role-based access (`RECEPTIONIST`, `MANAGER`)
- **Appointment booking** — a day-rail availability view, double-booking
  prevention, and appointment status tracking
- **Billing** — itemized bills against treatment codes, with receipt view
- **Reporting** — manager-only usage and revenue reports
- **Patient records** — create, search, and view patient history

## Tech stack

| Layer          | Choice                                              |
|----------------|------------------------------------------------------|
| Language       | Java 21                                             |
| Web tier       | Jakarta Servlets 6.1 + JSP/JSTL (no MVC framework)  |
| Persistence    | Plain JDBC (hand-written DAOs, no ORM)              |
| Database       | MySQL 8+                                            |
| Build          | Maven (wrapper included, no local install needed)   |
| Test           | JUnit 5, Mockito, AssertJ, H2 (in-memory, DAO tests)|
| Dev server     | Embedded Tomcat (IDE convenience only — see below)  |
| Deploy target  | Standalone Apache Tomcat 10.1+ / 11.x                |

## Project structure

```
src/main/java/lk/icbt/clinic/
  servlet/    HTTP endpoints (web pages + JSON APIs)
  service/    business logic
  dao/        JDBC data access
  model/      domain entities
  dto/        request/response shapes for the JSON APIs
  filter/     auth/session servlet filters
  billing/    invoice/bill calculation
  util/       DB connection & config helpers
  exception/  application-specific exceptions

src/main/webapp/   JSP views, static CSS/JS
src/main/java/.../Main.java   embedded-Tomcat entry point for running from an IDE

db/   versioned SQL: setup, schema, stored procedures, seed data
```

## Running locally

**Prerequisites:** Java 21, MySQL 8+ (Maven is not required — use the
included `./mvnw` wrapper).

### 1. Set up the database

```bash
mysql -u root -p < db/00-setup.sql
```

Edit `db/00-setup.sql` first and replace `CHANGE_ME` with a password of your
choosing. Then load the schema, stored procedures, and seed data:

```bash
mysql -u clinic_app -p sunrise_dental < db/01-schema.sql
mysql -u clinic_app -p sunrise_dental < db/02-procedures.sql
mysql -u clinic_app -p sunrise_dental < db/03-seed.sql
```

### 2. Provide credentials

Create `local.properties` in the project root (already git-ignored):

```properties
DB_USERNAME=clinic_app
DB_PASSWORD=<the password you chose above>
```

Or export `DB_USERNAME` / `DB_PASSWORD` (and optionally `DB_URL`, which
defaults to `jdbc:mysql://localhost:3306/sunrise_dental`) as environment
variables instead.

### 3. Run

**From an IDE:** build the project, then run `lk.icbt.clinic.Main` — it
starts an embedded Tomcat on port 8080 with no separate server install or
deployment step.

**From the command line:**

```bash
./mvnw compile
./mvnw org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=lk.icbt.clinic.Main -Dexec.classpathScope=compile
```

Then open **http://localhost:8080/login**:

| Username    | Password      | Role         |
|-------------|---------------|--------------|
| `reception` | `Recept@123`  | Receptionist |
| `manager`   | `Manager@123` | Manager      |

### Deploying for real

`Main.java` and its embedded Tomcat are a development convenience only and
are excluded from the packaged artifact. Build the deployable WAR with:

```bash
./mvnw package
```

and deploy `target/sunrise-dental.war` to a standalone Apache Tomcat 10.1+
or 11.x, with `DB_USERNAME`/`DB_PASSWORD`/`DB_URL` set as real environment
variables on that server.

## Testing

```bash
./mvnw test
```

All unit/servlet-level tests run against Mockito-mocked DAOs and mocked
servlet request/response objects — no database is required. DAO tests that
need a real JDBC connection run against an in-memory H2 database instead of
MySQL.

Integration tests that require a real MySQL instance are tagged and
excluded by default; run them explicitly with:

```bash
./mvnw test -Pmysql-it
```

## Continuous integration

Every push and pull request runs the test suite and packages the WAR via
GitHub Actions (`.github/workflows/ci.yml`). Once this repo is pushed to
GitHub, add a status badge at the top of this file:

```markdown
![CI](https://github.com/<owner>/<repo>/actions/workflows/ci.yml/badge.svg)
```
