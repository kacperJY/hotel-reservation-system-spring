# Hotel Reservation System

Backend aplikacji do zarządzania rezerwacjami hotelowymi napisany w **Java 25** i **Spring Boot 3**.

Projekt powstał jako ćwiczenie budowy większej aplikacji biznesowej z wykorzystaniem Spring Boot, Spring Security, JPA oraz PostgreSQL. Obejmuje pełny proces obsługi rezerwacji – od zarządzania hotelami i pokojami, przez składanie rezerwacji przez gości, po panel administracyjny oraz automatyczne zadania wykonywane w tle.

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

- JWT Authentication & Authorization
- Role-based access (Guest / Manager / Admin)
- Hotel and room management
- Reservation management
- Automatic room availability generation
- Scheduled background jobs
- Currency exchange integration (NBP API)
- Global exception handling (RFC 7807 Problem Details)
- Database migrations with Flyway

---

# 🏗️ Architecture

Projekt został podzielony na niezależne moduły odpowiadające za konkretne obszary biznesowe.

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

Najważniejsze decyzje projektowe:

- wykorzystanie DTO do komunikacji z API,
- podział logiki biznesowej na serwisy,
- globalna obsługa wyjątków (`@ControllerAdvice`),
- autoryzacja oparta o Spring Security oraz JWT,
- migracje bazy danych przy pomocy Flyway.

---

# ⚙️ Interesting Implementation Details

## Event-driven room availability

Po utworzeniu nowego pokoju publikowane jest zdarzenie aplikacyjne.

```
Room Created
      │
      ▼
Application Event
      │
      ▼
Event Listener
      │
      ▼
Generate availability for next 365 days
```

Dzięki temu moduł zarządzania pokojami pozostaje odseparowany od logiki odpowiedzialnej za generowanie dostępności.

---

## Row-level authorization

Manager może wykonywać operacje wyłącznie na hotelach, do których został przypisany.

Autoryzacja realizowana jest przy pomocy:

- Spring Security
- `@PreAuthorize`
- SpEL
- dodatkowej weryfikacji na poziomie zapytań do bazy.

---

## Scheduled cleanup

System automatycznie usuwa nieopłacone rezerwacje.

Zadanie wykonywane jest przez `@Scheduled`, a rekordy przetwarzane są partiami, aby ograniczyć rozmiar Persistence Context.

Po każdej partii wykonywane jest:

- `flush()`
- `clear()`

co pozwala utrzymać stabilne zużycie pamięci.

---

## JPA performance

Projekt wykorzystuje:

- `@EntityGraph`
- odpowiednio dobrane strategie pobierania danych
- dedykowane zapytania JPQL

aby ograniczyć problem N+1 i zmniejszyć liczbę zapytań do bazy.

---

## Currency exchange cache

Kursy walut pobierane są z API NBP.

Synchronizacja odbywa się cyklicznie w tle (`@Scheduled`), natomiast aplikacja korzysta z lokalnego cache (`@Cacheable`), dzięki czemu większość zapytań nie wymaga komunikacji z zewnętrznym API.

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

# 🚀 Running the project

## Requirements

- Java 25
- Docker
- Maven

---

## Development

1. Utwórz plik `db.env` na podstawie `db.env.example`.
2. Uzupełnij zmienne środowiskowe połączenia z bazą danych.
3. Uruchom aplikację z profilem `dev`.

Spring Boot automatycznie uruchomi kontener PostgreSQL za pomocą Docker Compose.

Profil `dev` dodatkowo uruchamia `DatabaseSeeder`, który zasila bazę przykładowymi danymi.

## Production

Skonfiguruj pliki:

- `app.env`
- `db.env`

Następnie uruchom:

```bash
docker compose --profile prod up -d --build
```

Profil `prod` nie uruchamia seedera i korzysta z konfiguracji przygotowanej dla środowiska produkcyjnego.

---

# 📌 Future Improvements

- OpenAPI / Swagger
- monitoring (Micrometer + Prometheus)
- obsługa płatności online
- powiadomienia e-mail
