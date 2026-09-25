package dev.shizzi.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.shizzi.ui.theme.AccentChoice
import dev.shizzi.ui.theme.DesignLanguage
import dev.shizzi.ui.theme.ThemeChoice

data class AppearanceState(
    val theme: ThemeChoice,
    val design: DesignLanguage,
    val accent: AccentChoice,
    val customAccents: List<Int>,
)

data class AppearanceActions(
    val onSetTheme: (ThemeChoice) -> Unit,
    val onSetDesign: (DesignLanguage) -> Unit,
    val onSetAccent: (AccentChoice) -> Unit,
    val onAddCustomAccent: (Int) -> Unit,
)

@Composable
fun AppearanceSection(state: AppearanceState, actions: AppearanceActions) {
    var isAccentOpen by remember { mutableStateOf(false) }

    ThemePicker(selected = state.theme, onSelect = actions.onSetTheme)

    SettingsChoice(
        label = SettingsText(title = "强调色"),
        value = accentLabel(state.accent),
        onClick = { isAccentOpen = true },
    )

    if (!isAccentOpen) return

    AccentPicker(
        state = AccentPickerState(
            selected = state.accent,
            customAccents = state.customAccents,
        ),
        actions = AccentPickerActions(
            onSelect = actions.onSetAccent,
            onAddCustom = actions.onAddCustomAccent,
            onDismiss = { isAccentOpen = false },
        ),
    )
}
