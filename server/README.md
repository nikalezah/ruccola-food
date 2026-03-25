# Server Application

This document describes the Ktor server backend for the food service.

## Overview

The server is a Kotlin/JVM application built with Ktor and Exposed ORM using R2DBC for reactive database access. It
provides REST API endpoints for the Admin and Customer applications.

## Technology Stack

- **Framework**: Ktor with CIO engine
- **Database**: PostgreSQL via Exposed ORM with R2DBC
- **Authentication**: JWT-based authentication
- **Serialization**: kotlinx.serialization

## API Routes

All API endpoints are prefixed with `/api/`.

| Route               | Description                   |
|---------------------|-------------------------------|
| `AuthRoutes`        | Login and authentication      |
| `DishRoutes`        | Dish CRUD operations          |
| `CustomerRoutes`    | Customer management           |
| `DayRoutes`         | Day and daily dish management |
| `MealPlanDayRoutes` | Meal plan day scheduling      |
| `PlanRoutes`        | Plan management               |
| `ChatRoutes`        | Real-time chat messaging      |
| `FileRoutes`        | File upload and serving       |

## Services

Business logic is organized into service classes:

- `JwtService` - JWT token generation and validation
- `UserService` - User lookup and management
- `DishService` - Dish and variant operations
- `CustomerService` - Customer operations
- `DayService` - Day and daily dish operations
- `MealPlanDayService` - Meal plan day operations
- `PlanService` - Plan operations
- `ChatService` - Chat and message operations
- `FileService` - File storage and retrieval

## Database

The database schema is managed through Exposed table definitions in `model/` and versioned migrations in
`database/migration/`.

### Tables

- `Users` - User accounts
- `Dishes` - Dish definitions
- `DishVariants` - Dish variant options
- `DishImages` - Dish image references
- `DishVariantCustomers` - Customer-variant associations
- `Customers` - Customer records
- `Days` - Daily menu entries
- `DayDishes` - Dishes assigned to days
- `MealPlanDays` - Meal plan day templates
- `MealPlanDayDishes` - Dishes in meal plan days
- `Plans` - Subscription plans
- `CustomerPlans` - Customer-plan associations
- `Chats` - Chat conversations
- `Messages` - Chat messages
- `MessageReads` - Message read status
- `Files` - Uploaded file metadata

## Configuration

Server configuration is in `src/main/resources/application.conf`:

- Database connection settings
- JWT secret and issuer
- File storage path

## Build and Run

```shell
./gradlew :server:run
```
