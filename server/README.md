# Server Application

Ktor/JVM backend for the Ruccola Food platform. Exposes a REST API for Admin and Customer clients,
persists data in PostgreSQL, and stores uploaded files on disk.

## Overview

The server handles authentication, business logic, and data access. All API endpoints are prefixed
with `/api/`. JWT bearer tokens authenticate admin and customer requests.

## Technology stack

| Layer          | Choice                                |
|----------------|---------------------------------------|
| Framework      | Ktor (CIO engine)                     |
| Database       | PostgreSQL via Exposed ORM with R2DBC |
| Authentication | JWT                                   |
| Serialization  | kotlinx.serialization                 |

API contracts and DTOs are defined in the [core](../core/README.md) module and mirrored on the
server side.

## API routes

| Route group         | Responsibility                                             |
|---------------------|------------------------------------------------------------|
| `AuthRoutes`        | Registration, login, token refresh                         |
| `DishRoutes`        | Dish CRUD, translations, and images                        |
| `CustomerRoutes`    | Customer profile, preferences, plan subscription, schedule |
| `DayRoutes`         | Calendar days and daily dish assignments                   |
| `MealPlanDayRoutes` | Meal plan day templates, dish binding, cycle rotation      |
| `PlanRoutes`        | Subscription plan CRUD                                     |
| `ChatRoutes`        | Chat threads and messages (REST + polling)                 |
| `FileRoutes`        | File upload and serving                                    |

## Services

Business logic is organized into service classes:

| Service              | Responsibility                                     |
|----------------------|----------------------------------------------------|
| `JwtService`         | Token generation and validation                    |
| `UserService`        | User lookup                                        |
| `DishService`        | Dish catalog operations                            |
| `MealPlanDayService` | Meal plan day templates and current-day rotation   |
| `DayService`         | Schedule — copy meal plan dishes to calendar dates |
| `PlanService`        | Subscription plan management                       |
| `CustomerService`    | Customer records, plan assignments, preferences    |
| `ChatService`        | Chat and message operations                        |
| `FileService`        | File storage and retrieval                         |

## Database

Schema is defined in `model/` and applied through versioned migrations in `database/migration/`.

### Tables

| Table                                        | Purpose                               |
|----------------------------------------------|---------------------------------------|
| `users`                                      | User accounts (admin and customer)    |
| `dishes`, `dish_images`, `dish_translations` | Dish catalog                          |
| `meal_plan_days`, `meal_plan_day_dishes`     | Rotating meal plan templates          |
| `days`, `day_dishes`                         | Calendar schedule                     |
| `plans`                                      | Subscription products                 |
| `customers`, `customer_plans`                | Customer records and plan assignments |
| `chats`, `messages`, `message_reads`         | Support chat                          |
| `files`                                      | Uploaded file metadata                |

## Configuration

`src/main/resources/application.conf` — database connection, JWT secret and issuer, file storage
path. Environment variables override defaults in production (`DB_URL`, `DB_USER`, `DB_PASSWORD`,
`PORT`).

## Build and run

### Gradle (development)

macOS / Linux:

```shell
./gradlew :server:run
```

Windows:

```shell
.\gradlew.bat :server:run
```

You can also use the **server** run configuration in the IDE toolbar.

The server listens on port **8080** by default.

### Docker Compose (full stack)

`docker-compose.yml` at the repository root starts the server, both web apps, and PostgreSQL:

| Service         | Port | Description              |
|-----------------|------|--------------------------|
| `server`        | 8080 | Ktor API                 |
| `admin-web`     | 8081 | Admin web app (nginx)    |
| `customer-web`  | 8082 | Customer web app (nginx) |
| `postgres`      | 5432 | Primary database         |
| `postgres_test` | 5433 | Test database            |

```shell
docker compose up --build
```

## Tests

Integration tests use the `postgres_test` database (port 5433). Start it before running server
tests:

```shell
docker compose up postgres_test -d
./gradlew :server:test
```
