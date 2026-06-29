# Ruccola Food

A meal subscription and delivery management platform. Staff configure menus and subscription
offerings; customers subscribe to a plan and follow their personal meal schedule.

Built as a Kotlin Multiplatform monorepo: a shared backend, a shared API contract layer, and two
client applications (Admin and Customer) for Android, iOS, Web, and Desktop.

## How it works

### Dishes

The dish catalog stores meals with localized names (English, Russian, Kazakh), images, and metadata.
Dishes are the building blocks for every menu.

### Meal plan days

A **meal plan day** is a reusable daily template: a numbered slot in a rotating cycle (day 1, day 2,
…). Each template assigns dishes to meal times — breakfast, brunch, lunch, afternoon snack, and
dinner. Exactly one meal plan day is marked **current** at a time; the cycle advances to the next
serial when staff roll the plan forward.

### Days (schedule)

A **day** is a calendar date with the dishes that will be delivered on that date. Staff copy dishes
from a meal plan day onto specific dates to build the delivery schedule. Customers see this schedule
in their app.

### Plans

A **plan** is a subscription product defined by a calorie tier, a billing period (number of days),
and a price per day. Customers choose a plan when subscribing.

### Customers

A **customer** is an end user with a profile, delivery preferences, and an active (or historical)
plan assignment. Staff manage customers from the admin app; customers register and manage their own
account from the customer app.

### Chat

Each customer can have a chat thread with staff for support. Messaging is implemented on the server
and in both apps, but the UI entry points are currently disabled (see app READMEs for how to
re-enable).

### Files

Dish images and other uploads are stored on the server and referenced by URL.

## Applications

| Application  | Role                                                                                    |
|--------------|-----------------------------------------------------------------------------------------|
| **Admin**    | Manage dishes, meal plan days, the delivery schedule, subscription plans, and customers |
| **Customer** | Register, subscribe to a plan, view the meal schedule, and manage profile settings      |

Both apps share authentication, theming, and UI primitives from a common module and talk to the same
REST API.

## Documentation

| Module                                               | Description                                                           |
|------------------------------------------------------|-----------------------------------------------------------------------|
| [core](core/README.md)                               | Shared API contracts, DTOs, HTTP clients, and platform-neutral models |
| [server](server/README.md)                           | Ktor backend, database, REST API, and deployment                      |
| [app](app/README.md)                                 | Client applications overview and project layout                       |
| [app/common](app/common/README.md)                   | Shared Compose UI, theming, auth, and adaptive layouts                |
| [app/admin/shared](app/admin/shared/README.md)       | Admin application — features, architecture, and build                 |
| [app/customer/shared](app/customer/shared/README.md) | Customer application — features, architecture, and build              |
