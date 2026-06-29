# Core Module

Shared Kotlin Multiplatform library used by client apps and aligned with the server API. Contains
HTTP clients, request/response DTOs, domain enums, and platform-neutral utilities — no UI code.

## Source sets

| Source set    | Description                      |
|---------------|----------------------------------|
| `commonMain`  | Shared code for all platforms    |
| `androidMain` | Android-specific implementations |
| `webMain`     | Shared web code (JS + Wasm)      |
| `jsMain`      | JavaScript-specific code         |
| `wasmJsMain`  | Wasm-specific code               |
| `jvmMain`     | JVM-specific code                |

The primary code lives under `src/commonMain/kotlin`. Add platform-specific code under the
corresponding source set when needed.

## API clients

Typed Ktor HTTP clients for the REST API (all paths are prefixed with `/api/`):

| Client           | Responsibility                                       |
|------------------|------------------------------------------------------|
| `AuthApi`        | Registration, login, and session                     |
| `DishApi`        | Dish CRUD and image management                       |
| `CustomerApi`    | Customer profile, preferences, and plan subscription |
| `DayApi`         | Calendar schedule and daily dish assignments         |
| `MealPlanDayApi` | Meal plan day templates and rotation                 |
| `PlanApi`        | Subscription plan management                         |
| `ChatApi`        | Chat threads and messages                            |
| `FileApi`        | File upload and download                             |

`HttpClient` configures the shared Ktor client with JSON serialization and bearer-token
authentication.

## Data models

### Domain enums and value types (`model/`)

- `Meal` — meal slot in a day (breakfast, brunch, lunch, afternoon snack, dinner) with a default time
- `PlanCalories` — allowed calorie tiers for subscription plans
- `PlanDays` — allowed billing-period lengths for subscription plans

### API DTOs (`api/`)

DTOs are co-located with their API client. Key types include:

- `DishDto`, `DishWithMealDto`, `DishWithTranslationsDto` — dish catalog
- `MealPlanDayDto` — meal plan day template with assigned dishes
- `DayDto` — calendar day with assigned dishes
- `PlanDto` — subscription plan
- `CustomerDto`, `CustomerPlanDetailsDto`, `ScheduledDayDto` — customer and schedule
- `ChatDto`, `MessageDto` — chat messaging
- `AuthResponseDto`, `UserDto` — authentication

`PagingResponse<T>` wraps paginated list endpoints.

## Utilities

| Symbol            | Purpose                                      |
|-------------------|----------------------------------------------|
| `BaseUrl`         | API base URL per platform                    |
| `Constants`       | Shared application constants                 |
| `Language`        | Supported content languages (EN, RU, KK)     |
| `ApiPagingSource` | Paging source adapter for API list endpoints |

## Dependencies

- Ktor Client (core, content negotiation, auth, resources)
- kotlinx.serialization
- kotlinx.datetime
- AndroidX Paging (common)

UI components, theming, and Compose-specific utilities live in [app/common](../app/common/README.md).
