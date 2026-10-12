# Library API: Spring Boot

Part of the DevFoundry Library API series. This is the
[Library API contract](https://github.com/devfoundry-training/Library_API_Contract) built with Spring Boot 3.5 on
Java 21: Spring Web for the HTTP layer, Spring Data JPA and Hibernate for the data, Flyway for the migrations. It runs
on SQLite on a laptop and on PostgreSQL 17 in Docker Compose, and it passes all 60 tests of the contract's
conformance suite on both.

The domain is the library from the Zero to Hero courses: books, members, loans and fines, with the same rules as the
task pack.

---

## Who Is This For?

- Java developers who want to see a layered Spring Boot API built against a fixed specification
- Delegates on Spring, JPA and backend courses who want a realistic project to read, run and change
- Java Zero to Hero learners taking the Library Manager from a console program to a web API
- Developers comparing stacks: the FastAPI, Django and ASP.NET Core versions answer the same requests in the same way

Comfort with Java, HTTP and basic SQL is assumed. No earlier Spring experience is needed to run it.

---

## Prerequisites

| Tool | Minimum Version | Needed for |
|------|-----------------|------------|
| Java (JDK) | 21 (the series uses Eclipse Temurin) | building and running the API |
| Python | 3.10+ (the series uses 3.13) | the conformance suite (standard library only) |
| Docker with Compose | 24+ | PostgreSQL and running the API in a container |

Maven is not needed: the Maven wrapper (`mvnw`, `mvnw.cmd`) downloads the right version on first use.

```bash
java -version
py -3.13 --version      # Windows
python3 --version       # macOS/Linux
```

---

## Getting Started

### Option 1: Local Run with SQLite (Recommended)

```bash
# Clone the repository
git clone https://github.com/devfoundry-training/Library_API_SpringBoot.git
cd Library_API_SpringBoot

# Start the API on http://localhost:8000
# Windows PowerShell:
.\mvnw.cmd spring-boot:run
# macOS/Linux:
./mvnw spring-boot:run
```

On start-up Flyway creates the tables in `library.db` (in the folder you started from) and, if the database is
empty, the sample data is loaded: 20 books, 9 members and 12 loans. Try it:

```bash
curl http://localhost:8000/health
curl http://localhost:8000/books/4
curl "http://localhost:8000/loans?overdue=true"
```

To build the jar once and run it directly:

```bash
./mvnw package
java -jar target/library-api.jar
```

To run in test mode (needed by the conformance suite):

```bash
# Windows PowerShell:
$env:TEST_MODE = "1"; .\mvnw.cmd spring-boot:run
# macOS/Linux:
TEST_MODE=1 ./mvnw spring-boot:run
```

### Option 2: Docker Compose with PostgreSQL

```bash
docker compose up --build                 # the API on http://localhost:8000, PostgreSQL 17 next to it
TEST_MODE=1 docker compose up --build     # the same, in test mode for the conformance suite
docker compose down -v                    # stop, and remove the database volume
```

The database port is not published, so it never clashes with a PostgreSQL already on your machine.

> **Note:** `TEST_MODE=1` lets any caller wipe the data with `POST /test/reset` and change today with the `X-Today`
> header. Use it for local runs and CI only, never on a deployed server.

### Environment Variables

| Variable | Default | Meaning |
|----------|---------|---------|
| `PORT` | `8000` | the port the API listens on |
| `DATABASE_URL` | `sqlite:///library.db` | where the data lives. PostgreSQL: `postgresql://library:library@db:5432/library` |
| `TEST_MODE` | unset (off) | `1` turns on `POST /test/reset` and the `X-Today` header. Any other value is off |
| `LIBRARY_TODAY` | unset | `YYYY-MM-DD`: fixes today for the whole server. An empty value counts as unset |

`DATABASE_URL` uses the same URL form as every other repo in the series. `config/DatabaseUrl.java` turns it into the
JDBC URL, user name and password that Spring needs.

---

## Architecture

Four layers, each only talking to the one below it:

```
  HTTP request
       |
       v
+---------------------------+   web/: controllers, request and response records,
|  API layer                |   validation, the error format, X-Today -> Today
+---------------------------+
       |  plain values (ids, validated input, today as a LocalDate)
       v
+---------------------------+   service/: availability, limits, due dates, fines,
|  Service layer            |   the order of the checks. No HTTP, no SQL.
+---------------------------+
       |  entities
       v
+---------------------------+   repository/: Spring Data JPA interfaces,
|  Repository layer         |   plus plain JDBC for loading the sample data
+---------------------------+
       |
       v
+---------------------------+   SQLite or PostgreSQL, tables built by Flyway
|  Database                 |   (src/main/resources/db/migration/)
+---------------------------+
```

### Where each rule lives

| Rule | Where |
|------|-------|
| Request bodies: required fields, types, lengths, ranges | `web/dto/*Request.java` (Bean Validation) and `config/JacksonConfig.java` (no type coercion) |
| Query values `active`, `overdue`, `member_id` | `web/QueryParams.java` |
| What is today (`X-Today` with `TEST_MODE=1`, then `LIBRARY_TODAY`, then the clock) | `web/TodayResolver.java` |
| Exceptions to `{"error": {"code", "message"}}` with the right status | `web/ApiExceptionHandler.java` |
| Loan days, fine per day and loan limit per member type | `service/LoanPolicy.java` |
| Borrowing checks and their order, returning, due date, days late, fine, overdue | `service/LoanService.java` |
| Available copies, copies not below those on loan, no deleting a book with loans | `service/BookService.java` |
| Unique email, `joined` defaults to today | `service/MemberService.java` |
| The exact 404 and 409 messages | `service/NotFoundException.java`, `service/RuleViolationException.java` |
| Search (text anywhere, whole genre, ignoring case) | `repository/BookRepository.java` |
| Loading the sample data and resetting the id counters | `service/SampleDataService.java`, `repository/SampleDataRepository.java` |

The service layer never reads the clock: every method that needs today takes it as a `LocalDate`, so a unit test
passes `2026-10-12` and gets the same answers as RULES.md.

### Repository Structure

```
src/main/java/uk/co/devfoundry/library/
  LibraryApplication.java          # Spring Boot entry point
  config/                          # Start-up wiring
    DatabaseUrl.java               # DATABASE_URL -> JDBC URL, credentials, Flyway folder
    LibraryEnvironmentPostProcessor.java   # reads DATABASE_URL and TEST_MODE before anything starts
    JacksonConfig.java             # strict JSON: "101", 4.5 and true are not whole numbers
    ClockConfig.java               # the real clock, in Europe/London
    SampleDataLoader.java          # loads the sample data into an empty database on start-up
    TomcatErrorConfig.java         # JSON instead of Tomcat's HTML for requests it refuses itself
  web/                             # API layer
    BookController.java, MemberController.java, LoanController.java, HealthController.java
    TestResetController.java       # POST /test/reset, only registered when TEST_MODE=1
    ApiExceptionHandler.java       # @RestControllerAdvice -> the error format
    FallbackErrorController.java   # replaces Spring Boot's /error page
    TodayResolver.java, Today.java # X-Today -> today, for every request
    QueryParams.java, BadRequestException.java
    dto/                           # request and response records
  service/                         # Service layer
    BookService.java, MemberService.java, LoanService.java, SampleDataService.java
    LoanPolicy.java                # standard 14/25p/3, student 21/10p/5, staff 28/0p/10
    LibraryException.java, NotFoundException.java, RuleViolationException.java
    BookData.java, MemberData.java, BookView.java, LoanView.java
  repository/                      # Repository layer
    BookRepository.java, MemberRepository.java, LoanRepository.java   # Spring Data JPA
    SampleDataRepository.java      # runs db/seed.sql, moves PostgreSQL sequences
  domain/                          # JPA entities
    Book.java, Member.java, Loan.java
src/main/resources/
  application.yml
  db/migration/sqlite/V1__schema.sql       # Flyway, SQLite
  db/migration/postgresql/V1__schema.sql   # Flyway, PostgreSQL (identity columns)
  db/seed.sql                      # a copy of the contract's db/seed.sql
  META-INF/spring.factories        # registers the environment post-processor
src/test/java/uk/co/devfoundry/library/
  service/                         # service unit tests (JUnit 5, in-memory SQLite)
  config/DatabaseUrlTest.java
.github/workflows/ci.yml           # build, unit tests, conformance on SQLite and PostgreSQL
pom.xml                            # Maven build, versions pinned by the Spring Boot 3.5.16 parent
mvnw, mvnw.cmd, .mvn/              # Maven wrapper
Dockerfile                         # multi-stage: JDK to build, JRE (Alpine) to run, non-root user
docker-compose.yml                 # the API and postgres:17
```

### Spring Boot traps this repo avoids

- Jackson turns `"101"` into 101 and `4.5` into 4 by default. `JacksonConfig` turns every scalar coercion off, so
  they are `400 validation_error` as the contract asks.
- Request records use `Integer`, not `int`, so a missing field is `null` and reported instead of becoming 0.
- Spring's own 404, 405 and bad-JSON answers are caught in `ApiExceptionHandler`. `/books/abc` is a type mismatch on
  the path variable, which becomes `404 not_found`.
- `spring.jpa.open-in-view=false` and `spring.jpa.hibernate.ddl-auto=validate`: Flyway owns the schema and
  Hibernate only checks that the entities match it.
- PostgreSQL does not move a sequence when rows are inserted with explicit ids, so after loading the sample data
  `SampleDataRepository` moves each sequence past the highest id. The next ids are book 21, member 110 and loan 13 on
  both databases.

---

## API

The API is described in the contract's [openapi.yaml](https://github.com/devfoundry-training/Library_API_Contract/blob/main/openapi.yaml),
with the business rules in [RULES.md](https://github.com/devfoundry-training/Library_API_Contract/blob/main/RULES.md).
This repo adds nothing to it and leaves nothing out.

| Method | Path | Success |
|--------|------|---------|
| GET | `/health` | 200 |
| GET, POST | `/books` | 200, 201 |
| GET, PUT, DELETE | `/books/{id}` | 200, 200, 204 |
| GET, POST | `/members` | 200, 201 |
| GET, PUT | `/members/{id}` | 200 |
| GET | `/members/{id}/loans?active=&overdue=` | 200 |
| GET, POST | `/loans?member_id=&active=&overdue=` | 200, 201 |
| GET | `/loans/{id}` | 200 |
| POST | `/loans/{id}/return` | 200 |
| POST | `/test/reset` | 204 (only with `TEST_MODE=1`) |

Errors always look like this:

```json
{"error": {"code": "loan_limit_reached", "message": "Ella Brown already has 3 loans (limit 3)"}}
```

---

## Running the Tests

### Unit Tests

```bash
# Windows PowerShell:
.\mvnw.cmd test
# macOS/Linux:
./mvnw test
```

44 JUnit 5 tests. The service tests use `@DataJpaTest` with an in-memory SQLite database built by the same Flyway
migrations, load the sample data before each test and roll back afterwards. No web server and no clock: today is
passed in. They cover borrowing, due dates and fines, returning, the loan limits and the order of the checks, the
book and member rules, and the `DATABASE_URL` conversion.

### Conformance Suite

The suite lives in the contract repo and tests the API from the outside over HTTP.

```bash
# 1. Start the API in test mode (from this repo)
$env:TEST_MODE = "1"; .\mvnw.cmd spring-boot:run       # Windows PowerShell
TEST_MODE=1 ./mvnw spring-boot:run                     # macOS/Linux

# 2. Run the suite (from a clone of the contract repo, next to this one)
py -3.13 conformance/run.py --base-url http://localhost:8000      # Windows
python3 conformance/run.py --base-url http://localhost:8000       # macOS/Linux
```

For PostgreSQL, start the API with `TEST_MODE=1 docker compose up --build` instead and run the same command. The
summary line should read `60 passed, 0 failed, 60 tests`.

CI (`.github/workflows/ci.yml`) runs the unit tests, then the conformance suite against SQLite and against
PostgreSQL 17, on every push and pull request.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 21 (Eclipse Temurin) | Language and runtime |
| Spring Boot 3.5.16 | Application framework |
| Spring Web (Spring MVC, Tomcat) | The HTTP layer |
| Spring Data JPA, Hibernate ORM 6.6 | Repositories and the ORM |
| Flyway 11 | Database migrations |
| Bean Validation (Hibernate Validator) | Request validation |
| SQLite (sqlite-jdbc, Hibernate community dialect) | Local database |
| PostgreSQL 17 | Database in Docker Compose and CI |
| JUnit 5, AssertJ | Unit tests |
| Maven wrapper | Build |
| Docker, Docker Compose | Containers |
| GitHub Actions | CI |

No Lombok. No MapStruct. No code generation.

---

## Related DevFoundry Courses

[Building APIs with FastAPI](https://devfoundry.co.uk/courses/fastapi-apis) ·
[Django Web Development](https://devfoundry.co.uk/courses/django-web-development) ·
[ASP.NET Core Web APIs](https://devfoundry.co.uk/courses/aspnet-core-web-apis) ·
[Database Access with SQLAlchemy](https://devfoundry.co.uk/courses/sqlalchemy-python) ·
[Entity Framework Core](https://devfoundry.co.uk/courses/entity-framework-core)

---

## What's Next?

Run the conformance suite against this API on SQLite, then again on PostgreSQL through Docker Compose. Then read
the same API in another stack: the layers, the rules and the answers are identical, so the differences are the
lesson.

- [Library_API_Contract](https://github.com/devfoundry-training/Library_API_Contract): the OpenAPI description,
  the rules, the sample data and the conformance suite
- [Library_API_FastAPI](https://github.com/devfoundry-training/Library_API_FastAPI): Python 3.13, FastAPI,
  SQLAlchemy 2.0, Alembic
- [Library_API_Django](https://github.com/devfoundry-training/Library_API_Django): Python 3.13, Django, Django REST
  framework
- [Library_API_DotNet](https://github.com/devfoundry-training/Library_API_DotNet): .NET 10, ASP.NET Core minimal
  API, EF Core
- Library_API_Express: Node.js, Express, TypeScript (later)

### What This Version Leaves Out

This is the **basic** version: the contract and nothing more. No authentication, no pagination, no caching, no
events. Advanced copies come later, each built on a passing basic version (see
[Basic and Advanced](https://github.com/devfoundry-training/Library_API_Contract#basic-and-advanced) in the
contract):

- **Security:** JWT authentication, member and librarian roles, members only see their own loans
- **Scale:** pagination, sorting, ETags and response caching, Redis
- **Events:** `loan.created` and `loan.returned` events through RabbitMQ or Kafka, an outbox table
- **Services:** catalogue, members and loans services behind an API gateway
- **Operations:** structured logs, metrics, tracing (OpenTelemetry), health and readiness probes
- **Delivery:** Kubernetes manifests or Helm, Argo CD

The library itself comes from the Zero to Hero course family, where the same books, members and loans are built
step by step as a console program:

- [Java Zero to Hero](https://github.com/devfoundry-training/Java_zero_hero)
- [Python Zero to Hero](https://github.com/devfoundry-training/Python_zero_hero)
- [C# Zero to Hero](https://github.com/devfoundry-training/CS_zero_hero)
