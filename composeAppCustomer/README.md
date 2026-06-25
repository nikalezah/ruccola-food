# Customer Application

This document describes the multiplatform Customer app for the food service.

## Overview

The Customer app is a Kotlin Multiplatform application targeting Android and Web. It allows customers to view meal
schedules, view dish details, chat with administrators, and manage their profiles.

## Screens

| Screen              | Description                  |
|---------------------|------------------------------|
| `LoginScreen`       | User authentication          |
| `RegisterScreen`    | New user registration        |
| `MainScreen`        | Main navigation hub          |
| `ScheduleScreen`    | View daily meal schedule     |
| `DishDetailsScreen` | View dish details and images |
| `ChatListScreen`    | List of chat conversations   |
| `ChatScreen`        | Chat with administrators     |
| `ProfileScreen`     | User profile and preferences |

## ViewModels

All ViewModels follow the MVVM pattern and communicate with shared APIs from the `shared` module.

- `LoginViewModel` - Handles authentication state
- `RegisterViewModel` - Handles registration flow
- `ScheduleViewModel` - Manages meal schedule display
- `DishViewModel` - Manages dish details loading
- `ChatViewModel` - Manages chat state and messages
- `ProfileViewModel` - Manages user profile

## Architecture

The implementation follows a shared MVVM pattern in `commonMain`:

1. **Model**: Data layer uses shared API classes (`AuthApi`, `DayApi`, `ChatApi`, etc.)
2. **View**: Shared Composable screens in `commonMain`
3. **ViewModel**: Shared ViewModels managing UI state via `StateFlow`

Platform-specific code:

- Android: `androidAppCustomer` contains `MainActivity` and app resources; `androidMain` contains `AppLocaleManager` and
  `AppPreferences`
- Web: `webMain` contains web-specific entry points

## Key Features

- User authentication and registration
- Daily meal schedule viewing
- Dish details with image carousel
- Real-time chat with administrators
- User profile management
- Localization support

## Build and Run

### Android

```shell
./gradlew :androidAppCustomer:assembleDebug
```

### Web (Wasm)

```shell
./gradlew :composeAppCustomer:wasmJsBrowserDevelopmentRun
```

### Web (JS)

```shell
./gradlew :composeAppCustomer:jsBrowserDevelopmentRun
```
