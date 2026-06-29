# Common UI Module

Shared Compose Multiplatform library used by both the Admin and Customer applications. Provides
authentication UI, theming, reusable composables, adaptive layouts, and session management.

## Overview

`app/common` sits between [core](../../core/README.md) (API and models) and the app-specific shared
modules (`app/admin/shared`, `app/customer/shared`). App features import common UI primitives;
business screens and ViewModels stay in each app's `shared` module.

## Source sets

| Source set    | Description                                    |
|---------------|------------------------------------------------|
| `commonMain`  | Shared composables, theme, auth, and utilities |
| `androidMain` | Android-specific UI implementations            |
| `webMain`     | Shared web UI (JS + Wasm)                      |
| `nativeMain`  | iOS and Desktop shared implementations         |

## Authentication and session

- `LoginViewModel` — login form state and API call
- `LoginForm` — shared login composable
- `AuthRoute` — login/register navigation route
- `AppSessionProvider`, `SessionViewModelStoreOwner` — session scope for ViewModels
- `rememberAppSession()` — session holder used in platform entry points

## Theming

Material 3 color schemes and preference storage:

- `GreenLightColorScheme`, `GreenDarkColorScheme`
- `BaselineLightColorScheme`, `BaselineDarkColorScheme`
- `ThemePreference`, `ThemePicker`
- `ColorScheme` — theme composition root

## UI components

Reusable Compose Multiplatform primitives:

| Component                       | Purpose                                               |
|---------------------------------|-------------------------------------------------------|
| `LabeledNavigationBar`          | Bottom navigation with labels                         |
| `AdaptiveNavigationScaffold`    | Responsive shell with navigation rail on wide screens |
| `PullToRefresh`                 | Pull-to-refresh container                             |
| `AsyncContent`                  | Loading / error / content scaffold                    |
| `ChatUi`                        | Chat message list and input                           |
| `AsyncImage`                    | Remote image loading (Coil on Android)                |
| `SwipeToRemove`                 | Swipe-to-dismiss list item                            |
| `SquareImagesCarousel200`       | 200 dp image carousel                                 |
| `HorizontalUncontainedCarousel` | Horizontal image carousel                             |
| `ToggleButtonsRow`              | Segmented toggle row                                  |
| `FabMenu`                       | Floating action button menu                           |
| `BackHandler`                   | Platform back-button handler                          |
| `SingleLineText`                | Single-line ellipsized text                           |
| `ApplyIconButton`               | Styled icon button                                    |
| `DetailTopBar`                  | Detail screen top bar                                 |
| `LogoutButton`                  | Settings logout action                                |

## Adaptive layouts

- `WindowSizeClass` — compact / medium / expanded breakpoints
- `AdaptiveLayouts` — width-based layout helpers

## Localization

- `LocalLocale` — composition local for the active locale
- `LocaleFormatting` — locale-aware formatting helpers
- `composeResources/values*` — shared string resources (EN, RU, KK)

Per-app string resources for feature-specific copy live in each app's `shared` module.

## Utilities

- `DateUtils` — date formatting and manipulation
- `DishUiUtils` — dish display helpers (meal labels, formatting)

## Dependencies

- Compose Multiplatform (runtime, foundation, material3, animation)
- [core](../../core/README.md) module (API clients, models)
- Coil (image loading, Android only)
- AndroidX Lifecycle ViewModel
