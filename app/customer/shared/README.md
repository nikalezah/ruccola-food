# Customer Application

Kotlin Multiplatform customer app (Android, iOS, Web, Desktop).

## Package structure

```
kz.ruccola.food/
  App.kt
  navigation/          CustomerTab, AuthRoute (re-exports common AuthRoute)
  feature/
    MainScreen.kt                              # package feature (single-file)
    auth/          LoginScreen, RegisterScreen, RegisterViewModel
    schedule/      ScheduleScreen, DishDetailsScreen, ScheduleViewModel
    subscription/  SubscriptionScreen, SubscriptionContent, SubscriptionViewModel, …
    profile/       ProfileScreen, ProfileContent, ProfileViewModel, …
    chat/          ChatListScreen, ChatScreen, ChatViewModel (dormant)
```

Shared UI and auth live in `app/common` (`LoginViewModel`, `LoginForm`, `ThemePicker`, `AsyncContent`, …).

## Architecture

- MVVM: ViewModels use `StateFlow`, constructor injection with `factory()`
- Navigation: `CustomerTab` enum + `AuthRoute` for login/register
- Session: `rememberAppSession()` + `AppSessionProvider` in platform entry points
- Localization: `LocalLocale` + per-app string resources (EN/RU/KK)

## Chat (dormant)

`feature/chat/` is implemented but disabled in the shell. To re-enable:

1. In `navigation/CustomerTab.kt` — uncomment `Chat` enum entry
2. In `feature/MainScreen.kt` — uncomment the `LabeledNavigationTab` for chat and the `when` branch with
   `ChatListScreen`
3. Adjust `showBottomBar` logic if tab indices change

`isChatOpen`, `hasUnreadChat`, and related state are kept in `MainScreen` for quick restore.

## Build and Run

### Android

```shell
./gradlew :app:customer:androidApp:assembleDebug
```

### Web (Wasm)

```shell
./gradlew :app:customer:webApp:wasmJsBrowserDevelopmentRun
```

### Web (JS)

```shell
./gradlew :app:customer:webApp:jsBrowserDevelopmentRun
```
