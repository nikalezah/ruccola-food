package kz.ruccola.food.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun FabMenu(items: List<Triple<ImageVector?, String, () -> Unit>>) {
    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val focusRequester = remember { FocusRequester() }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    FloatingActionButtonMenu(
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                        contentDescription = "Toggle menu"
                    }
                    .animateFloatingActionButton(
                        visible = fabVisible || fabMenuExpanded,
                        alignment = Alignment.BottomEnd,
                    )
                    .focusRequester(focusRequester),
                checked = fabMenuExpanded,
                onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        items.forEachIndexed { i, (icon, label, onClick) ->
            FloatingActionButtonMenuItem(
                modifier = Modifier.semantics {
                    isTraversalGroup = true
                    // Add a custom a11y action to allow closing the menu when focusing on
                    // the last menu item, since the close button comes before the first
                    // menu item in the traversal order.
                    if (i == items.size - 1) {
                        customActions = listOf(
                            CustomAccessibilityAction(
                                label = "Close menu",
                                action = {
                                    fabMenuExpanded = false
                                    true
                                },
                            ),
                        )
                    }
                }
                    .then(
                        if (i == 0) {
                            Modifier.onKeyEvent {
                                // Navigating back from the first item should go back to the
                                // FAB menu button.
                                if (
                                    it.type == KeyEventType.KeyDown &&
                                    (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))
                                ) {
                                    focusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                                return@onKeyEvent false
                            }
                        } else {
                            Modifier
                        },
                    ),
                onClick = {
                    onClick()
                    fabMenuExpanded = false
                },
                icon = { icon?.let { Icon(it, contentDescription = label) } },
                text = { Text(label) },
            )
        }
    }
}
