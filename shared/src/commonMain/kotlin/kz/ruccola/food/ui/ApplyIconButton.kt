package kz.ruccola.food.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplyIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Filled.Check,
    contentDescription: String = "Save",
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(
            IconButtonDefaults.smallContainerSize(
                widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
            ),
        ),
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}
