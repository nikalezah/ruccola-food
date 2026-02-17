# Android Dish Management Implementation

This document describes the Android-specific implementation for dish management functionality in the Food application.

## Overview

The Android implementation provides a complete solution for managing dishes with the following features:
- View a list of all dishes
- View details of a specific dish
- Create a new dish
- Update an existing dish
- Archive a dish (soft delete)

## Architecture

The implementation follows the MVVM (Model-View-ViewModel) architecture pattern:

1. **Model**: The data layer is provided by the shared `DishApi` class
2. **View**: Android-specific UI components in `AndroidDishScreen.kt`
3. **ViewModel**: Android-specific `DishViewModel` that manages UI state and business logic
4. **Repository**: Android-specific `DishRepository` that handles API calls and error handling

## Components

### DishRepository

Located in `repository/DishRepository.kt`, this class:
- Wraps the shared `DishApi` with Android-specific error handling
- Uses Kotlin's `Result` type for better error handling
- Performs network operations on the IO dispatcher
- Provides Android logging for debugging

### DishViewModel

Located in `viewmodel/DishViewModel.kt`, this class:
- Manages UI state using `StateFlow`
- Handles all dish operations through the repository
- Provides methods for UI interactions like showing/hiding dialogs
- Implements proper error handling and loading states

### AndroidDishScreen

Located in `screens/AndroidDishScreen.kt`, this composable:
- Displays the list of dishes with Material Design components
- Shows loading, error, and empty states
- Provides UI for adding, editing, and archiving dishes
- Uses Material icons for better visual cues
- Implements Toast messages for user feedback

## Usage

The Android implementation is automatically used when the app runs on Android devices. The `MainActivity` sets `AndroidDishScreen` as the main content.

## Error Handling

The implementation includes comprehensive error handling:
- Network errors are caught and displayed to the user
- Input validation for required fields
- Visual feedback for loading states
- Toast messages for operation results

## UI Enhancements

Android-specific UI enhancements include:
- Material Design components and icons
- Toast messages for user feedback
- Improved input validation
- Better visual hierarchy with cards and typography
- Floating action buttons for primary actions