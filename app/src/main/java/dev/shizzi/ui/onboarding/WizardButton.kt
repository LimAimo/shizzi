package dev.shizzi.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shizzi.ui.theme.ShizziTheme
import dev.shizzi.ui.theme.standardTween

private val ButtonHeight = 56.dp

@Composable
fun WizardButton(action: WizardAction, isPrimary: Boolean) {
    val modifier = Modifier.fillMaxWidth().height(ButtonHeight)

    if (isPrimary) {
        Button(
            onClick = action.onClick,
            enabled = action.isEnabled,
            modifier = modifier,
        ) {
            WizardButtonLabel(action.label)
        }
    } else {
        OutlinedButton(
            onClick = action.onClick,
            enabled = action.isEnabled,
            modifier = modifier,
        ) {
            WizardButtonLabel(action.label)
        }
    }
}

@Composable
private fun WizardButtonLabel(label: String) {
    val fadeSpec = standardTween<Float>()

    AnimatedContent(
        targetState = label,
        transitionSpec = { fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec) },
        label = "wizardButtonLabel",
    ) { text ->
        Text(
            text = text,
            style = ShizziTheme.typography.title,
            color = LocalContentColor.current,
        )
    }
}
