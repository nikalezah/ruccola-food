package kz.ruccola.food.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun FabMenu(items: List<Triple<ImageVector?, String, () -> Unit>>)
