# Shared Module

This document describes the shared Kotlin Multiplatform module used by all applications.

## Overview

The shared module provides common code, API clients, UI components, and theming across all targets: Android, Web (
JS/Wasm), JVM (server), and platform-specific implementations.

## Source Sets

| Source Set    | Description                      |
|---------------|----------------------------------|
| `commonMain`  | Shared code for all platforms    |
| `androidMain` | Android-specific implementations |
| `webMain`     | Shared web code (JS + Wasm)      |
| `jsMain`      | JavaScript-specific code         |
| `wasmJsMain`  | Wasm-specific code               |
| `jvmMain`     | JVM/server-specific code         |

## API Clients

HTTP clients for communicating with the server backend:

- `AuthApi` - Authentication endpoints
- `DishApi` - Dish CRUD operations
- `CustomerApi` - Customer management
- `DayApi` - Day and daily dish operations
- `MealPlanDayApi` - Meal plan day operations
- `PlanApi` - Plan management
- `ChatApi` - Chat messaging
- `FileApi` - File upload/download

The shared `HttpClient` configures the Ktor HTTP client with JSON serialization and authentication.

## Data Models

Shared data transfer objects and models:

- `DishWithMealDto` - Dish with associated meal information
- `PlanDays` - Plan day associations
- `PlanCalories` - Calorie information
- `Meal` - Meal type enum

## UI Components

Reusable Compose Multiplatform components:

- `LabeledNavigationBar` - Bottom navigation with labels
- `PullToRefresh` - Pull-to-refresh container
- `ChatUi` - Chat message UI components
- `AsyncImage` - Async image loading with Coil
- `SwipeToRemove` - Swipe-to-dismiss container
- `SquareImagesCarousel200` - Image carousel (200dp)
- `HorizontalUncontainedCarousel` - Horizontal carousel
- `ToggleButtonsRow` - Toggle button group
- `FabMenu` - Floating action button menu
- `BackHandler` - Back button handler
- `SingleLineText` - Single-line text component
- `ApplyIconButton` - Styled icon button

## Theming

Custom Material 3 color schemes:

- `GreenLightColorScheme` - Light green theme
- `GreenDarkColorScheme` - Dark green theme
- `BaselineLightColorScheme` - Baseline light theme
- `BaselineDarkColorScheme` - Baseline dark theme
- `ThemePreference` - Theme preference management

## Utilities

- `DateUtils` - Date formatting and manipulation
- `DishUiUtils` - Dish-related UI utilities
- `Constants` - Application constants
- `BaseUrl` - API base URL configuration
- `Locale` - Localization support
- `ApiPagingSource` - Paging source for API data

## Dependencies

Key dependencies:

- Compose Multiplatform (runtime, foundation, material3, animation)
- Ktor Client (core, resources, content negotiation, auth)
- kotlinx.serialization
- kotlinx.datetime
- Coil (image loading, Android only)
- AndroidX Paging (common)
