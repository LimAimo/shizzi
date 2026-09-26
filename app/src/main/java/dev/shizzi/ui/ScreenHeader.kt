package dev.shizzi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shizzi.ui.theme.HeaderHeight
import dev.shizzi.ui.theme.ShizziTheme

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    action: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    containerColor: Color = ShizziTheme.colors.surface,
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 1.dp,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(horizontal = ShizziTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.xs),
        ) {
            BackButton(onBack)
            Text(
                text = title,
                style = ShizziTheme.typography.heading,
                color = ShizziTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            action()
            Spacer(Modifier.width(ShizziTheme.spacing.xs))
        }
    }
}
