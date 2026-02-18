package kz.ruccola.food.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.unit.dp

@Composable
actual fun FabMenu(items: List<Triple<ImageVector?, String, () -> Unit>>) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val fabFocusRequester = remember { FocusRequester() }
    val itemFocusRequesters = remember(items.size) { List(items.size) { FocusRequester() } }
    val progress by animateFloatAsState(
        targetValue = if (fabMenuExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "FabMenuProgress",
    )

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Box(
        modifier = Modifier.onKeyEvent {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape && fabMenuExpanded) {
                fabMenuExpanded = false
                true
            } else {
                false
            }
        },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            items.forEachIndexed { i, (icon, label, onClick) ->
                val delay = i * 30
                AnimatedVisibility(
                    visible = fabMenuExpanded,
                    enter = fadeIn(tween(150, delay)) +
                        slideInVertically(tween(150, delay)) { it / 2 } +
                        scaleIn(tween(150, delay), initialScale = 0.9f),
                    exit = fadeOut(tween(120)) +
                        slideOutVertically(tween(120)) { it / 2 } +
                        scaleOut(tween(120), targetScale = 0.9f),
                ) {
                    val containerColor = MaterialTheme.colorScheme.secondaryContainer
                    Surface(
                        onClick = {
                            onClick()
                            fabMenuExpanded = false
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .focusRequester(itemFocusRequesters[i])
                            .focusable()
                            .semantics {
                                isTraversalGroup = true
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
                                        if (
                                            it.type == KeyEventType.KeyDown &&
                                            (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))
                                        ) {
                                            fabFocusRequester.requestFocus()
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                        shape = CircleShape,
                        color = containerColor,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (icon != null) {
                                Icon(icon, contentDescription = label)
                                Spacer(Modifier.size(8.dp))
                            }
                            Text(label, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .semantics {
                    traversalIndex = -1f
                    stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                    contentDescription = "Toggle menu"
                }
                .focusRequester(fabFocusRequester)
                .focusable(),
            onClick = { fabMenuExpanded = !fabMenuExpanded },
            shape = if (fabMenuExpanded) CircleShape else FloatingActionButtonDefaults.shape,
        ) {
            Crossfade(targetState = fabMenuExpanded, label = "FabMenuIcon") { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = progress * 180f },
                )
            }
        }
    }
}
