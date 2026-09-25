package dev.shizzi.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shizzi.ui.theme.ShizziTheme

private val ButtonWidth = 200.dp
private val ButtonHeight = 56.dp
private val SpinnerSize = 24.dp

@Composable
fun ConnectButton(
    label: String,
    state: ConnectButtonState,
    onClick: () -> Unit,
) {
    val colors = ShizziTheme.colors
    val isEnabled = state != ConnectButtonState.DISABLED && state != ConnectButtonState.LOADING
    val isPrimary = state == ConnectButtonState.START

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.width(ButtonWidth).height(ButtonHeight),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) colors.primary else colors.surfaceContainer,
            contentColor = if (isPrimary) colors.onPrimary else colors.onSurface,
            disabledContainerColor = colors.surfaceContainer,
            disabledContentColor = colors.onSurfaceMuted,
        ),
    ) {
        if (state == ConnectButtonState.LOADING) {
            CircularProgressIndicator(
                color = colors.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(SpinnerSize),
            )
        } else {
            Text(text = label, style = ShizziTheme.typography.title)
        }
    }
}

enum class ConnectButtonState { START, STOP, LOADING, DISABLED }

@Composable
fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = "取消",
            style = ShizziTheme.typography.label,
        )
    }
}
