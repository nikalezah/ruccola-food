# Admin Application

Kotlin Multiplatform app for staff to manage the meal delivery operation: dish catalog, meal plan
days, delivery schedule, subscription plans, and customers.

Targets: **Android**, **iOS**, **Web** (JS and Wasm), and **Desktop**.

## Features

| Area          | Screens                                        | Purpose                                                 |
|---------------|------------------------------------------------|---------------------------------------------------------|
| Auth          | `LoginScreen`                                  | Staff login                                             |
| Plan          | `PlanScreen`                                   | Manage subscription plans (calorie tier, period, price) |
| Dish          | `DishScreen`, `DishEditorScreen`               | Dish catalog with translations and images               |
| Meal plan day | `MealPlanDayScreen`, `MealPlanDayEditorScreen` | Rotating daily menu templates                           |
| Day           | `DayScreen`                                    | Calendar schedule — assign dishes to delivery dates     |
| Customer      | `CustomerScreen`, `CustomerDetailsScreen`      | Customer list, profile, and plan assignment             |
| Settings      | `SettingsScreen`                               | Theme and logout                                        |
| Chat          | `ChatScreen`                                   | Customer support chat (**dormant** — see below)         |

## Package structure

```
kz.ruccola.food/
  App.kt, ImagePicker.kt
  navigation/          AdminTab, MealPlanDaysRoute
  feature/
    MainScreen.kt, LoginScreen.kt, SettingsScreen.kt   # single-file features
    plan/              PlanScreen, PlanContent, PlanViewModel, …
    dish/              DishScreen, DishEditorScreen, …
    mealplanday/       MealPlanDayScreen, MealPlanDayEditorScreen, …
    day/               DayScreen, DayViewModel
    customer/          CustomerScreen, CustomerDetailsScreen, CustomersViewModel
    chat/              ChatScreen, ChatViewModel (dormant)
```

Shared UI and auth live in [app/common](../../common/README.md) (`LoginViewModel`, `LoginForm`,
`ThemePicker`, `AsyncContent`, …).

## Architecture

- **MVVM** — ViewModels expose `StateFlow`; Koin `factory()` for constructor injection
- **Navigation** — `AdminTab` enum plus local overlay state (no Navigation library)
- **Session** — `rememberAppSession()` and `AppSessionProvider` in platform entry points

## Chat (dormant)

`feature/chat/` is implemented but disabled in the shell. To re-enable:

1. In `feature/customer/CustomerScreen.kt` — uncomment the `trailingContent` chat button block
2. In `feature/MainScreen.kt` — uncomment chat-related tab badge / `onChatOpenChanged` wiring if needed

Chat overlay and state variables are preserved for quick restore.

## Project layout

| Path          | Role                                                        |
|---------------|-------------------------------------------------------------|
| `shared/`     | KMP library — screens, ViewModels, navigation (this module) |
| `androidApp/` | Android entry point                                         |
| `iosApp/`     | iOS entry point (Xcode)                                     |
| `webApp/`     | Web entry point (JS + Wasm)                                 |
| `desktopApp/` | Desktop entry point                                         |

## Build and run

Use the IDE run configuration for the target platform, or run from the terminal.

### Android

macOS / Linux:

```shell
./gradlew :app:admin:androidApp:assembleDebug
```

Windows:

```shell
.\gradlew.bat :app:admin:androidApp:assembleDebug
```

### Web (Wasm — recommended)

macOS / Linux:

```shell
./gradlew :app:admin:webApp:wasmJsBrowserDevelopmentRun
```

Windows:

```shell
.\gradlew.bat :app:admin:webApp:wasmJsBrowserDevelopmentRun
```

### Web (JS — older browsers)

macOS / Linux:

```shell
./gradlew :app:admin:webApp:jsBrowserDevelopmentRun
```

Windows:

```shell
.\gradlew.bat :app:admin:webApp:jsBrowserDevelopmentRun
```

### Desktop

macOS / Linux:

```shell
./gradlew :app:admin:desktopApp:run
```

Windows:

```shell
.\gradlew.bat :app:admin:desktopApp:run
```

### iOS

Requires macOS with Xcode.

1. Open `app/admin/iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or device and run
3. Set your development team in `Configuration/Config.xcconfig` if needed

The Xcode build invokes Gradle to compile the `AdminShared` Kotlin framework. `BASE_URL` defaults to
`http://localhost:8080` for the Simulator; on a physical device, use the host machine's LAN IP.
