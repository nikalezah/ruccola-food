package kz.ruccola.food.ui

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class LabeledNavigationTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val showBadge: Boolean = false,
)

@Composable
fun LabeledNavigationBar(
    tabs: List<LabeledNavigationTab>,
    selected: () -> Int,
    onSelect: (Int) -> Unit,
) {
    NavigationBar {
        tabs.forEachIndexed { i, tab ->
            NavigationBarItem(
                selected = selected() == i,
                onClick = { onSelect(i) },
                icon = {
                    val icon = if (selected() == i) tab.selectedIcon else tab.unselectedIcon
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
