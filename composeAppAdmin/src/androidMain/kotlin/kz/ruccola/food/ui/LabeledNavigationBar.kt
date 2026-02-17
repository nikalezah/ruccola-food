package kz.ruccola.food.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun LabeledNavigationBar(
    tabs: List<Triple<ImageVector, ImageVector, String>>,
    selected: () -> Int,
    onSelect: (Int) -> Unit,
) {
    NavigationBar {
        tabs.forEachIndexed { i, (selectedIcon, unselectedIcon, label) ->
            NavigationBarItem(
                selected = selected() == i,
                onClick = { onSelect(i) },
                icon = {
                    Icon(
                        if (selected() == i) selectedIcon else unselectedIcon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}
