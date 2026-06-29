# Customer Application

Kotlin Multiplatform app for end users to register, subscribe to a meal plan, view their delivery
schedule, and manage profile settings.

Targets: **Android**, **iOS**, **Web** (JS and Wasm), and **Desktop**.

## Features

| Area         | Screens                               | Purpose                                  |
|--------------|---------------------------------------|------------------------------------------|
| Auth         | `LoginScreen`, `RegisterScreen`       | Login and self-registration              |
| Schedule     | `ScheduleScreen`, `DishDetailsScreen` | Personal meal delivery calendar          |
| Subscription | `SubscriptionScreen`                  | Choose and manage a subscription plan    |
| Profile      | `ProfileScreen`                       | Personal info, preferences, and settings |
| Chat         | `ChatListScreen`, `ChatScreen`        | Support chat (**dormant** — see below)   |

## Package structure

```
kz.ruccola.food/
  App.kt
  navigation/          CustomerTab, AuthRoute (re-exports common AuthRoute)
  feature/
    MainScreen.kt                              # single-file feature
    auth/          LoginScreen, RegisterScreen, RegisterViewModel
    schedule/      ScheduleScreen, DishDetailsScreen, ScheduleViewModel
    subscription/  SubscriptionScreen, SubscriptionContent, SubscriptionViewModel, …
    profile/       ProfileScreen, ProfileContent, ProfileViewModel, …
    chat/          ChatListScreen, ChatScreen, ChatViewModel (dormant)
```

Shared UI and auth live in [app/common](../../common/README.md) (`LoginViewModel`, `LoginForm`,
`ThemePicker`, `AsyncContent`, …).

## Architecture

- **MVVM** — ViewModels expose `StateFlow`; Koin `factory()` for constructor injection
- **Navigation** — `CustomerTab` enum and `AuthRoute` for login/register flow
- **Session** — `rememberAppSession()` and `AppSessionProvider` in platform entry points
- **Localization** — `LocalLocale` plus per-app string resources (EN, RU, KK)

## Chat (dormant)

`feature/chat/` is implemented but disabled in the shell. To re-enable:

1. In `navigation/CustomerTab.kt` — uncomment the `Chat` enum entry
2. In `feature/MainScreen.kt` — uncomment the `LabeledNavigationTab` for chat and the `when` branch
   with `ChatListScreen`
3. Adjust `showBottomBar` logic if tab indices change

`isChatOpen`, `hasUnreadChat`, and related state are kept in `MainScreen` for quick restore.

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
./gradlew :app:customer:androidApp:assembleDebug
```

Windows:

```shell
.\gradlew.bat :app:customer:androidApp:assembleDebug
```

### Web (Wasm — recommended)

macOS / Linux:

```shell
./gradlew :app:customer:webApp:wasmJsBrowserDevelopmentRun
```

Windows:

```shell
.\gradlew.bat :app:customer:webApp:wasmJsBrowserDevelopmentRun
```

### Web (JS — older browsers)

macOS / Linux:

```shell
./gradlew :app:customer:webApp:jsBrowserDevelopmentRun
```

Windows:

```shell
.\gradlew.bat :app:customer:webApp:jsBrowserDevelopmentRun
```

### Desktop

macOS / Linux:

```shell
./gradlew :app:customer:desktopApp:run
```

Windows:

```shell
.\gradlew.bat :app:customer:desktopApp:run
```

### iOS

Requires macOS with Xcode.

1. Open `app/customer/iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or device and run
3. Set your development team in `Configuration/Config.xcconfig` if needed

The Xcode build invokes Gradle to compile the `CustomerShared` Kotlin framework. `BASE_URL` defaults
to `http://localhost:8080` for the Simulator; on a physical device, use the host machine's LAN IP.
