# Dish Management (Shared)

This document describes the shared, multiplatform implementation for dish management in the Admin app.

## Overview

The Dish management screen is shared across Android and Web. It provides:
- Listing dishes
- Creating/editing via the platform-specific editor
- Archiving via swipe-to-dismiss
- Pull-to-refresh for reloading

## Architecture

The implementation follows a shared MVVM pattern in `commonMain`:

1. **Model**: The data layer is the shared `DishApi` class
2. **View**: Shared `DishScreen` in `composeAppAdmin/src/commonMain/kotlin/kz/ruccola/food/admin/screens/DishScreen.kt`
3. **ViewModel**: Shared `DishViewModel` in `composeAppAdmin/src/commonMain/kotlin/kz/ruccola/food/viewmodel/DishViewModel.kt`

The ViewModel communicates directly with `DishApi` and provides a single source of truth for the dish list and UI state.

## Platform Editor

Editing and image/variant management remain platform-specific:
- Android uses `AndroidDishEditorScreen`
- Web uses `DishEditorScreen` (web implementation)

The shared `DishScreen` calls an `expect`ed `DishEditorScreen`, with platform-specific `actual` implementations.

## Usage

Both Android `MainActivity` and Web `main.kt` call the shared `DishScreen()` from `commonMain`.
