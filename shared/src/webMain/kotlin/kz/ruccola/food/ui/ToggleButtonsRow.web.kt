package kz.ruccola.food.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
actual fun ToggleButtonsRow(
    options: List<String>,
    initialSelectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
) {
    if (options.isEmpty()) return
    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) }
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { i, o ->
            ToggleButton(
                checked = selectedIndex == i,
                onCheckedChange = {
                    selectedIndex = i
                    onSelectedIndexChange(i)
                },
                modifier = Modifier.weight(if (selectedIndex == i) 1.3f else 1f).semantics { role = Role.RadioButton },
                shapes =
                    when (i) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                if (selectedIndex == i) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
                Text(o)
            }
        }
    }
}

@Composable
private fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shapes: ToggleButtonShapes = ToggleButtonDefaults.shapes(),
    colors: ToggleButtonColors = ToggleButtonDefaults.toggleButtonColors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ToggleButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by resolvedInteractionSource.collectIsPressedAsState()
    val containerColor = colors.containerColor(enabled, checked)
    val contentColor = colors.contentColor(enabled, checked)
    val buttonShape = shapes.shapeFor(pressed, checked)

    Surface(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics { role = Role.Checkbox },
        enabled = enabled,
        shape = buttonShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 0.dp,
        border = border,
        interactionSource = resolvedInteractionSource,
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Row(
                Modifier.defaultMinSize(minHeight = ToggleButtonDefaults.MinHeight)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

private object ButtonGroupDefaults {
    val ConnectedSpaceBetween = 2.dp
    private val ConnectedInnerCornerSize = CornerSize(8.dp)
    private val ConnectedPressedInnerCornerSize = CornerSize(4.dp)
    private val ConnectedButtonCheckedShape: Shape = RoundedCornerShape(CornerSize(100))

    @Composable
    fun connectedLeadingButtonShapes(
        shape: Shape = connectedLeadingButtonShape(),
        pressedShape: Shape = connectedLeadingButtonPressShape(),
        checkedShape: Shape = ConnectedButtonCheckedShape,
    ): ToggleButtonShapes = ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)

    @Composable
    fun connectedMiddleButtonShapes(
        shape: Shape = RoundedCornerShape(8.dp),
        pressedShape: Shape = RoundedCornerShape(ConnectedPressedInnerCornerSize),
        checkedShape: Shape = ConnectedButtonCheckedShape,
    ): ToggleButtonShapes = ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)

    @Composable
    fun connectedTrailingButtonShapes(
        shape: Shape = connectedTrailingButtonShape(),
        pressedShape: Shape = connectedTrailingButtonPressShape(),
        checkedShape: Shape = ConnectedButtonCheckedShape,
    ): ToggleButtonShapes = ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)

    @Composable
    private fun connectedLeadingButtonShape(): Shape =
        RoundedCornerShape(
            topStart = CornerSize(100),
            bottomStart = CornerSize(100),
            topEnd = ConnectedInnerCornerSize,
            bottomEnd = ConnectedInnerCornerSize,
        )

    @Composable
    private fun connectedLeadingButtonPressShape(): Shape =
        RoundedCornerShape(
            topStart = CornerSize(100),
            bottomStart = CornerSize(100),
            topEnd = ConnectedPressedInnerCornerSize,
            bottomEnd = ConnectedPressedInnerCornerSize,
        )

    @Composable
    private fun connectedTrailingButtonShape(): Shape =
        RoundedCornerShape(
            topEnd = CornerSize(100),
            bottomEnd = CornerSize(100),
            topStart = ConnectedInnerCornerSize,
            bottomStart = ConnectedInnerCornerSize,
        )

    @Composable
    private fun connectedTrailingButtonPressShape(): Shape =
        RoundedCornerShape(
            topEnd = CornerSize(100),
            bottomEnd = CornerSize(100),
            topStart = ConnectedPressedInnerCornerSize,
            bottomStart = ConnectedPressedInnerCornerSize,
        )
}

private object ToggleButtonDefaults {
    val MinHeight = 40.dp
    val IconSpacing = 8.dp
    val IconSize = 20.dp
    val ContentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)

    @Composable
    fun toggleButtonColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
        checkedContainerColor: Color = Color.Unspecified,
        checkedContentColor: Color = Color.Unspecified,
    ): ToggleButtonColors =
        defaultToggleButtonColors().copy(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
            checkedContainerColor = checkedContainerColor,
            checkedContentColor = checkedContentColor,
        )

    @Composable
    fun shapes(
        shape: Shape? = null,
        pressedShape: Shape? = null,
        checkedShape: Shape? = null,
    ): ToggleButtonShapes =
        ToggleButtonShapes(
            shape = shape ?: RoundedCornerShape(CornerSize(100)),
            pressedShape = pressedShape ?: RoundedCornerShape(6.dp),
            checkedShape = checkedShape ?: RoundedCornerShape(12.dp),
        )

    @Composable
    private fun defaultToggleButtonColors(): ToggleButtonColors {
        val scheme = MaterialTheme.colorScheme
        return ToggleButtonColors(
            containerColor = scheme.surfaceContainer,
            contentColor = scheme.onSurfaceVariant,
            disabledContainerColor = scheme.onSurface.copy(alpha = 0.1f),
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            checkedContainerColor = scheme.primary,
            checkedContentColor = scheme.onPrimary,
        )
    }
}

@Immutable
private class ToggleButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val checkedContainerColor: Color,
    val checkedContentColor: Color,
) {
    fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
        disabledContainerColor: Color = this.disabledContainerColor,
        disabledContentColor: Color = this.disabledContentColor,
        checkedContainerColor: Color = this.checkedContainerColor,
        checkedContentColor: Color = this.checkedContentColor,
    ) = ToggleButtonColors(
        containerColor.takeOrElse { this.containerColor },
        contentColor.takeOrElse { this.contentColor },
        disabledContainerColor.takeOrElse { this.disabledContainerColor },
        disabledContentColor.takeOrElse { this.disabledContentColor },
        checkedContainerColor.takeOrElse { this.checkedContainerColor },
        checkedContentColor.takeOrElse { this.checkedContentColor },
    )

    @Stable
    fun containerColor(
        enabled: Boolean,
        checked: Boolean,
    ): Color =
        when {
            enabled && checked -> checkedContainerColor
            enabled && !checked -> containerColor
            else -> disabledContainerColor
        }

    @Stable
    fun contentColor(
        enabled: Boolean,
        checked: Boolean,
    ): Color =
        when {
            enabled && checked -> checkedContentColor
            enabled && !checked -> contentColor
            else -> disabledContentColor
        }
}

@Immutable
private class ToggleButtonShapes(
    val shape: Shape,
    val pressedShape: Shape,
    val checkedShape: Shape,
) {
    fun shapeFor(
        pressed: Boolean,
        checked: Boolean,
    ): Shape =
        if (pressed) {
            pressedShape
        } else if (checked) {
            checkedShape
        } else {
            shape
        }
}
