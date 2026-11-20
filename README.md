# 🏨 Hotel Reservation System -- rooms, availability and reservations.

> The project supports development, automated test and containerized production profiles.

---

# 📖 About

Hotel Reservation System is a REST backend built with Java and Spring Boot.

The project was created to practice building a larger business application involving multiple user roles, database relationships, transactional operations, scheduled processing and integration with an external API.

Its main purpose is to develop practical backend engineering skills in areas such as:

* application structure and separation of responsibilities,
* authentication and authorization,
* transactional reservation processing,
* JPA and Hibernate data access,
* scheduled batch operations,
* external API integration,
* caching,
* integration testing with real infrastructure,
* environment-specific configuration.

The repository is primarily a learning and development project rather than a production-ready commercial system.

---

# 🏗️ Application Structure

The codebase is organized around the main business responsibilities of the application.

### Authentication and users

Responsible for:

* user registration,
* login,
* JWT generation and validation,
* role-based access control.

The application supports the following roles:

* `GUEST`,
* `MANAGER`,
* `ADMIN`.

### Guest operations

Guests can:

* search for available rooms,
* filter rooms by city, capacity and reservation dates,
* display prices in a selected currency,
* create reservations,
* view paginated reservation history,
* cancel eligible reservations.

### Manager operations

Managers can:

* view reservations for assigned facilities,
* update reservation statuses,
* access only facilities assigned to their account.

Facility-level access is verified using Spring Security method authorization and a database-level ownership check.

### Administrator operations

Administrators can:

* create facilities,
* add rooms,
* assign managers to facilities,
* view and update reservations in any facility.

### Background processing

Scheduled services are responsible for:

* maintaining the room availability window,
* cancelling outdated pending reservations,
* releasing room availability after cancellation,
* synchronizing currency exchange rates.

---

# 🔄 Reservation Workflow

```text
Administrator creates a room
          │
          ▼
Spring application event is published
          │
          ▼
Availability is generated for the configured date range
          │
          ▼
Guest searches for an available room
          │
          ▼
Reservation is created in a transaction
          │
          ▼
Selected availability dates are marked as occupied
          │
          ▼
Manager processes the reservation status
```

Room availability records use optimistic locking to detect concurrent modifications of the same dates.

---

# ✨ Implemented Features

* JWT authentication and authorization
* Role-based access control
* User registration and login
* Hotel, apartment and room management
* Manager assignment to facilities
* Room search by:

  * city,
  * guest capacity,
  * check-in and check-out dates
* Reservation creation and cancellation
* Paginated reservation history
* Reservation status management
* Optimistic locking for room availability
* Automatic availability generation after room creation
* Daily extension of the availability window
* Automatic cancellation of outdated pending reservations
* Currency conversion using the NBP API
* Local caching of currency exchange rates
* Scheduled currency synchronization
* Database migrations with Flyway
* Global exception handling using RFC 7807 Problem Details
* DTO validation with Bean Validation
* Development-only database seeding
* Environment-specific `dev`, `test` and `prod` profiles
* Docker Compose development and production environments

---

# 💻 Tech Stack

* Java 25
* Spring Boot 4.0.6
* Spring Web MVC
* Spring Data JPA
* Hibernate
* Spring Security
* Bean Validation
* Spring Cache
* PostgreSQL
* Flyway
* JJWT
* Docker
* Docker Compose
* Maven Wrapper
* JUnit 5
* Mockito
* Testcontainers
* WireMock
* MockMvc

---

# ⚙️ Technical Details

## Event-based room availability generation

Creating a room publishes a Spring application event.

A dedicated listener handles the event and generates room availability records for the configured reservation window. The listener runs asynchronously, only after the room creation transaction has committed, and uses its own transaction. Room creation therefore does not wait for the availability records, and the listener never sees an uncommitted room. This separates room creation from availability generation without introducing external messaging infrastructure.

## Optimistic locking

Room availability records contain a JPA `@Version` field.

When multiple transactions attempt to modify the same availability record, optimistic locking allows Hibernate to detect conflicting updates instead of silently overwriting them.

## Scheduled availability maintenance

The application maintains a rolling availability window.

A scheduled task:

* removes outdated availability records,
* creates availability for the next required date,
* processes records in configurable batches,
* periodically flushes and clears the Persistence Context.

This limits the number of managed entities retained during larger batch operations.

## Pending reservation cleanup

Reservations remain in the `PENDING` state until processed by a manager.

A scheduled task finds pending reservations older than the configured limit, changes their status to `CANCELLED` and releases the associated room dates.

## Currency exchange integration

Room prices are stored in PLN.

The application retrieves exchange rates from the public NBP API and can return room prices in another supported currency.

Currency data is:

* stored in PostgreSQL,
* synchronized periodically,
* cached locally with Spring Cache,
* reused when the external API is temporarily unavailable.

## JPA query optimization

The persistence layer uses:

* `@EntityGraph`,
* dedicated JPQL queries,
* lazy relationships,
* JDBC batching,
* pagination,
* explicit Persistence Context cleanup during batch processing.

These mechanisms reduce unnecessary database queries and limit memory usage during background operations.

---

# 🧪 Testing

The project contains both service-level and integration tests.

## Service tests

Service logic is tested with:

* JUnit 5,
* Mockito.

## Integration tests

Integration tests run with:

* `@SpringBootTest`,
* MockMvc,
* a real PostgreSQL instance managed by Testcontainers,
* WireMock as a replacement for the external NBP API,
* the dedicated `test` Spring profile.

Run the complete test suite with:

```bash
./mvnw verify
```

Docker must be running because the integration tests start a PostgreSQL container.

No manually configured external database is required for the test profile.

---

# 🚀 Running the Project

## Requirements

* JDK 25
* Docker
* Docker Compose

The repository includes Maven Wrapper, so a separate Maven installation is not required.

---

## Development Mode

Development mode runs the Spring Boot application locally and starts PostgreSQL through Spring Boot Docker Compose support.

### 1. Prepare the database configuration

Copy the example file:

```bash
cp db.env.example db.env
```

Configure the PostgreSQL variables inside `db.env`.

### 2. Start the application

```bash
./mvnw spring-boot:run
```

The default Spring profile is `dev`.

In development mode:

* PostgreSQL is started automatically with Docker Compose,
* Flyway applies database migrations,
* sample users, facilities and rooms are inserted by `DatabaseSeeder`.

The application is available at:

```text
http://localhost:8080
```

---

## Test Mode

Run all unit and integration tests with:

```bash
./mvnw verify
```

The integration test configuration automatically provides:

* the `test` Spring profile,
* PostgreSQL through Testcontainers,
* a mocked NBP API through WireMock.

---

## Containerized Production Profile

The production profile runs both PostgreSQL and the application in Docker containers.

### 1. Prepare environment files

```bash
cp db.env.example db.env
cp app.env.example app.env
```

Configure the database credentials in `db.env`.

Configure the following application values in `app.env`:

* database connection,
* application port,
* `prod` Spring profile,
* JWT secret.

### 2. Build and start the containers

```bash
docker compose --profile prod up -d --build
```

The production profile:

* builds the application image using a multi-stage Dockerfile,
* waits for PostgreSQL to pass its health check,
* applies Flyway migrations,
* does not run the development database seeder.

To stop the environment:

```bash
docker compose --profile prod down
```

---

# 📌 Possible Further Improvements

Potential areas for further development include:

* OpenAPI and Swagger documentation,
* GitHub Actions CI,
* additional concurrency integration tests,
* application metrics and monitoring,
* more detailed API-level test coverage,
* further code and package structure cleanup.

---

# 📄 Project Status

The current business scope is implemented and the application can be started in development, test and containerized production configurations.

The project is maintained primarily as a practical learning environment for improving Spring Boot, JPA, testing and application configuration skills. Future work will focus mainly on technical improvements rather than turning the application into a complete commercial hotel platform.
