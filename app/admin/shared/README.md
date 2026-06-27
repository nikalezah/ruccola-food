# Admin Application

Kotlin Multiplatform admin app (Android, iOS, Web, Desktop).

## Package structure

```
kz.ruccola.food/
  App.kt, ImagePicker.kt
  navigation/          AdminTab, MealPlanDaysRoute
  feature/
    MainScreen.kt, LoginScreen.kt, SettingsScreen.kt   # package feature (single-file)
    plan/              PlanScreen, PlanContent, PlanViewModel, …
    dish/              DishScreen, DishEditorScreen, …
    mealplanday/       MealPlanDayScreen, MealPlanDayEditorScreen, …
    day/               DayScreen, DayViewModel
    customer/          CustomerScreen, CustomerDetailsScreen, CustomersViewModel
    chat/              ChatScreen, ChatViewModel (dormant)
```

Shared UI and auth live in `app/common` (`LoginViewModel`, `LoginForm`, `ThemePicker`, `AsyncContent`, …).

## Architecture

- MVVM: ViewModels use `StateFlow`, constructor injection with `factory()`
- Navigation: `AdminTab` enum + local overlay state (no Navigation library)
- Session: `rememberAppSession()` + `AppSessionProvider` in platform entry points

## Chat (dormant)

`feature/chat/` is implemented but disabled in the shell. To re-enable:

1. In `feature/customer/CustomerScreen.kt` — uncomment the `trailingContent` chat button block
2. In `feature/MainScreen.kt` — uncomment chat-related tab badge / `onChatOpenChanged` wiring if needed

Chat overlay and state variables are preserved in commented/active form for quick restore.

## Build and Run

### Android

```shell
./gradlew :app:admin:androidApp:assembleDebug
```

### Web (Wasm)

```shell
./gradlew :app:admin:webApp:wasmJsBrowserDevelopmentRun
```

### Web (JS)

```shell
./gradlew :app:admin:webApp:jsBrowserDevelopmentRun
```
