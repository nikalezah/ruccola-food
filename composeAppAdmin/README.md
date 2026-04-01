# Admin Application

This document describes the multiplatform Admin app for managing the food service.

## Overview

The Admin app is a Kotlin Multiplatform application targeting Android and Web. It provides administrative functionality
for managing dishes, meal plans, customers, and chat communications.

## Screens

| Screen                    | Description                                                      |
|---------------------------|------------------------------------------------------------------|
| `LoginScreen`             | User authentication                                              |
| `MainScreen`              | Main navigation hub                                              |
| `SettingsScreen`          | App settings and preferences                                     |
| `DishScreen`              | List and manage dishes with swipe-to-archive and pull-to-refresh |
| `DishEditorScreen`        | Create/edit dish details                                         |
| `DishImagesEditorScreen`  | Manage dish images                                               |
| `MealPlanDayScreen`       | View and manage meal plan days                                   |
| `MealPlanDayEditorScreen` | Edit meal plan day dishes                                        |
| `DayScreen`               | View and manage days                                             |
| `PlanScreen`              | View and manage plans                                            |
| `CustomerScreen`          | List customers                                                   |
| `CustomerDetailsScreen`   | View customer details                                            |
| `ChatScreen`              | Chat with customers                                              |

## ViewModels

All ViewModels follow the MVVM pattern and communicate with shared APIs from the `shared` module.

- `LoginViewModel` - Handles authentication state
- `DishViewModel` - Manages dish list and operations
- `DishEditorViewModel` - Manages dish editing state
- `DishImagesViewModel` - Manages dish image operations
- `MealPlanDayViewModel` - Manages meal plan day operations
- `DayViewModel` - Manages day operations
- `PlanViewModel` - Manages plan operations
- `CustomersViewModel` - Manages customer list
- `ChatViewModel` - Manages chat state

## Architecture

The implementation follows a shared MVVM pattern in `commonMain`:

1. **Model**: Data layer uses shared API classes (`DishApi`, `PlanApi`, etc.)
2. **View**: Shared Composable screens in `commonMain`
3. **ViewModel**: Shared ViewModels managing UI state via `StateFlow`

Platform-specific code:

- Android: `androidMain` contains `MainActivity` and platform utilities
- Web: `webMain` contains web-specific entry points

## Key Features

- Dish management with images
- Meal plan day scheduling with dish assignment
- Day management with dish tracking
- Plan management
- Customer management and details
- Real-time chat with customers
- Settings and preferences

## Build and Run

### Android

```shell
./gradlew :composeAppAdmin:assembleDebug
```

### Web (Wasm)

```shell
./gradlew :composeAppAdmin:wasmJsBrowserDevelopmentRun
```

### Web (JS)

```shell
./gradlew :composeAppAdmin:jsBrowserDevelopmentRun
```
