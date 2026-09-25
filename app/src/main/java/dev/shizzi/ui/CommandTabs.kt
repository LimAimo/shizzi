package dev.shizzi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.shizzi.AutomationCommand
import dev.shizzi.ui.theme.ShizziTheme

@Composable
fun CommandTabs(
    selected: AutomationCommand,
    onSelect: (AutomationCommand) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.md),
    ) {
        AutomationCommand.entries.forEach { command ->
            GhostButton(
                label = labelFor(command),
                onClick = { onSelect(command) },
                isActive = command == selected,
                padding = 0.dp,
            )
        }
    }
}

private fun labelFor(command: AutomationCommand): String = when (command) {
    AutomationCommand.START -> "开始"
    AutomationCommand.STOP -> "停止"
    AutomationCommand.TOGGLE -> "切换"
    AutomationCommand.QUERY_STATUS -> "状态"
}
