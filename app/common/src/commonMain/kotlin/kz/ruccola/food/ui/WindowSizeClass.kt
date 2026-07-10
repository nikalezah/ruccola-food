package kz.ruccola.food.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 window width size classes. Used across the apps to decide how to lay out content for the available width
 * (phone vs. tablet vs. desktop / wide browser window).
 *
 * Breakpoints follow the M3 window size class guidelines:
 * - [Compact]: width < 600dp (phones, narrow windows)
 * - [Medium]: 600dp <= width < 840dp (small tablets, split windows)
 * - [Expanded]: width >= 840dp (tablets in landscape, desktop, wide browser windows)
 */
enum class WindowWidthClass {
    Compact,
    Medium,
    Expanded,
}

fun windowWidthClassFor(width: Dp): WindowWidthClass =
    when {
        width < 600.dp -> WindowWidthClass.Compact
        width < 840.dp -> WindowWidthClass.Medium
        else -> WindowWidthClass.Expanded
    }

/**
 * Provides the current [WindowWidthClass] to the composition. It is set once by [AdaptiveNavigationScaffold] (the root
 * of each authenticated screen) so any nested screen can read it without measuring the window again.
 */
val LocalWindowWidthClass = staticCompositionLocalOf { WindowWidthClass.Compact }
