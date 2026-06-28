package kz.ruccola.food.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Material 3 adaptive navigation container that automatically swaps between layouts based on the
 * available window width, following the M3 Expressive responsive navigation guidelines:
 *
 *  - Compact (< 600dp): bottom [LabeledNavigationBar] (phone / narrow window).
 *  - Medium / Expanded (600dp..<1200dp): collapsed [WideNavigationRail] on the side.
 *  - Large (>= 1200dp): expanded [WideNavigationRail] showing labels (desktop / wide window).
 *
 * It also publishes the current [WindowWidthClass] via [LocalWindowWidthClass] so nested screens can
 * adapt their content (e.g. switch to a list-detail layout).
 *
 * Because the breakpoints are derived from [BoxWithConstraints], resizing the browser or desktop
 * window recomposes and switches the layout automatically.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdaptiveNavigationScaffold(
    tabs: List<LabeledNavigationTab>,
    selected: () -> Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showNavigation: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val widthClass = windowWidthClassFor(maxWidth)
        val useBottomBar = widthClass == WindowWidthClass.Compact
        val expandedRail = maxWidth >= expandedRailBreakpoint

        CompositionLocalProvider(LocalWindowWidthClass provides widthClass) {
            if (useBottomBar) {
                Scaffold(
                    bottomBar = {
                        if (showNavigation) {
                            LabeledNavigationBar(tabs = tabs, selected = selected, onSelect = onSelect)
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                ) { padding ->
                    content(padding)
                }
            } else {
                Row(Modifier.fillMaxSize()) {
                    if (showNavigation) {
                        AdaptiveNavigationRail(
                            tabs = tabs,
                            selected = selected,
                            onSelect = onSelect,
                            expanded = expandedRail,
                        )
                    }
                    Scaffold(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    ) { padding ->
                        content(padding)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AdaptiveNavigationRail(
    tabs: List<LabeledNavigationTab>,
    selected: () -> Int,
    onSelect: (Int) -> Unit,
    expanded: Boolean,
) {
    val railState = rememberWideNavigationRailState()
    LaunchedEffect(expanded) {
        if (expanded) railState.expand() else railState.collapse()
    }
    val railExpanded = railState.targetValue == WideNavigationRailValue.Expanded
    WideNavigationRail(state = railState) {
        tabs.forEachIndexed { i, tab ->
            val isSelected = selected() == i
            WideNavigationRailItem(
                selected = isSelected,
                onClick = { onSelect(i) },
                railExpanded = railExpanded,
                icon = {
                    val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                    if (tab.showBadge) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(icon, contentDescription = tab.label)
                        }
                    } else {
                        Icon(icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label) },
            )
        }
    }
}

private val expandedRailBreakpoint = 1200.dp
