# Client Applications

Two Kotlin Multiplatform applications — **Admin** and **Customer** — share a common UI layer and
the [core](../core/README.md) API module. Each app is split into platform entry points and a shared
KMP library that holds screens, ViewModels, and navigation.

## Module layout

| Path                      | Role                                                                     |
|---------------------------|--------------------------------------------------------------------------|
| `app/common`              | Shared Compose UI, theming, auth, and adaptive layouts used by both apps |
| `app/admin/shared`        | Admin screens, ViewModels, and navigation                                |
| `app/admin/androidApp`    | Android entry point (AGP 9)                                              |
| `app/admin/iosApp`        | iOS entry point (Xcode)                                                  |
| `app/admin/webApp`        | Web entry point (JS and Wasm targets)                                    |
| `app/admin/desktopApp`    | Desktop entry point (JVM)                                                |
| `app/customer/shared`     | Customer screens, ViewModels, and navigation                             |
| `app/customer/androidApp` | Android entry point (AGP 9)                                              |
| `app/customer/iosApp`     | iOS entry point (Xcode)                                                  |
| `app/customer/webApp`     | Web entry point (JS and Wasm targets)                                    |
| `app/customer/desktopApp` | Desktop entry point (JVM)                                                |

Admin and customer shared code organize features under `feature/` packages: multi-file domains live
in `feature/<name>/`, single-screen features sit directly in `feature/`.

## Documentation

| Module                                       | Description                                                 |
|----------------------------------------------|-------------------------------------------------------------|
| [common](common/README.md)                   | Shared UI components, theming, auth, and session management |
| [admin/shared](admin/shared/README.md)       | Admin application                                           |
| [customer/shared](customer/shared/README.md) | Customer application                                        |

## API base URL

Client apps connect to the server at `http://localhost:8080` by default. On a physical iOS device,
use the host machine's LAN IP instead of `localhost`. Configuration lives in
[core/src/commonMain/kotlin/kz/ruccola/food/BaseUrl.kt](../core/src/commonMain/kotlin/kz/ruccola/food/BaseUrl.kt).