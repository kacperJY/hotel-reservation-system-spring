# Hotel Reservation System

A backend application for managing hotel reservations, built with **Java 25** and **Spring Boot 3**.

This project was created as a hands-on exercise in building a larger business application with Spring Boot, Spring Security, JPA, and PostgreSQL. It covers the main reservation workflow: managing hotels and rooms, allowing guests to make reservations, handling administrative operations, and running automated background tasks.

---

# 🚀 Tech Stack

- **Java 25**
- **Spring Boot 3**
  - Spring Web
  - Spring Data JPA
  - Spring Security
- **PostgreSQL**
- **JWT Authentication**
- **Flyway**
- **Docker & Docker Compose**
- **Maven**

---

# ✨ Features

- JWT authentication and authorization
- Role-based access control (Guest / Manager / Admin)
- Hotel and room management
- Reservation management
- Automatic room availability generation
- Scheduled background jobs
- Currency exchange rate integration with the NBP API
- Global exception handling using RFC 7807 Problem Details
- Database migrations with Flyway

---

# 🏗️ Architecture

The application is divided into modules responsible for separate business areas.

```text
catalog
guest
manager
admin
user
security
repositories
scheduledServices
exception
```

The main design decisions include:

- using DTOs for API communication,
- separating business logic into services,
- handling exceptions globally with `@ControllerAdvice`,
- implementing authentication and authorization with Spring Security and JWT,
- managing database migrations with Flyway.

---

# ⚙️ Implementation Details

## Event-driven room availability

When a new room is created, the application publishes an event.

```text
Room Created
      │
      ▼
Application Event
      │
      ▼
Event Listener
      │
      ▼
Generate availability for the next 365 days
```

This keeps the room management module separate from the logic responsible for generating room availability.

---

## Row-level authorization

A manager can perform operations only on hotels assigned to them.

Authorization is implemented using:

- Spring Security,
- `@PreAuthorize`,
- SpEL,
- additional verification at the database query level.

---

## Scheduled cleanup

The system automatically removes unpaid reservations.

The cleanup task runs with `@Scheduled`. Records are processed in batches to limit the size of the Persistence Context.

After each batch, the application calls:

- `flush()`,
- `clear()`.

This helps keep memory usage stable while processing larger sets of records.

---

## JPA performance

The project uses:

- `@EntityGraph`,
- appropriately selected data-fetching strategies,
- dedicated JPQL queries.

These mechanisms help reduce the N+1 query problem and limit the number of database queries.

---

## Currency exchange cache

Currency exchange rates are retrieved from the NBP API.

The data is synchronized periodically in the background using `@Scheduled`. The application also uses a local cache with `@Cacheable`, so most requests do not require a call to the external API.

---

# 📦 Project Structure

```text
catalog
guest
manager
admin
user
security
repositories
scheduledServices
exception
config
```

---

# 🚀 Running the Project

## Requirements

- Java 25
- Docker
- Maven

---

## Development

1. Create a `db.env` file based on `db.env.example`.
2. Set the environment variables required for the database connection.
3. Start the application with the `dev` profile.

Spring Boot will automatically start a PostgreSQL container using Docker Compose.

The `dev` profile also runs `DatabaseSeeder`, which populates the database with sample data.

## Production

Configure the following files:

- `app.env`
- `db.env`

Then run:

```bash
docker compose --profile prod up -d --build
```

The `prod` profile does not run the database seeder and uses configuration intended for the production environment.

---

# 📌 Future Improvements

- OpenAPI / Swagger
- Monitoring with Micrometer and Prometheus
- Online payment support
- Email notifications
