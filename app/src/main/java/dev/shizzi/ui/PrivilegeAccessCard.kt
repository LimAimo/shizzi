package dev.shizzi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.shizzi.PrivilegeBackendType
import dev.shizzi.PrivilegeState
import dev.shizzi.R
import dev.shizzi.str
import dev.shizzi.ui.theme.ShizziTheme
import dev.shizzi.ui.theme.themedSurface

data class PrivilegeAccessActions(
    val onSelectBackend: (PrivilegeBackendType) -> Unit,
    val onShizukuAction: () -> Unit,
    val onOpenWirelessDebugging: () -> Unit,
    val onPairLocalAdb: (String) -> Unit,
)

@Composable
fun PrivilegeAccessCard(
    backend: PrivilegeBackendType,
    state: PrivilegeState,
    actions: PrivilegeAccessActions,
) {
    var pairingCode by remember(backend) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .themedSurface(fill = ShizziTheme.colors.surface)
            .padding(ShizziTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.md),
    ) {
        Text(str(R.string.privilege_access), style = ShizziTheme.typography.subheading)
        Text(providerDescription(backend), style = ShizziTheme.typography.body, color = ShizziTheme.colors.onSurfaceMuted)

        Row(horizontalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.sm)) {
            ProviderButton(
                label = str(R.string.shizuku),
                selected = backend == PrivilegeBackendType.SHIZUKU,
                onClick = { actions.onSelectBackend(PrivilegeBackendType.SHIZUKU) },
                modifier = Modifier.weight(1f),
            )
            ProviderButton(
                label = str(R.string.wireless_debugging),
                selected = backend == PrivilegeBackendType.LOCAL_ADB,
                onClick = { actions.onSelectBackend(PrivilegeBackendType.LOCAL_ADB) },
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = privilegeStatus(state),
            style = ShizziTheme.typography.label,
            color = if (state is PrivilegeState.Ready) ShizziTheme.colors.primary else ShizziTheme.colors.onSurfaceMuted,
        )

        when (backend) {
            PrivilegeBackendType.SHIZUKU -> if (state !is PrivilegeState.Ready) {
                Button(onClick = actions.onShizukuAction) { Text(str(R.string.grant_permission)) }
            }
            PrivilegeBackendType.LOCAL_ADB -> if (state !is PrivilegeState.Ready) {
                Text(str(R.string.pairing_hint), style = ShizziTheme.typography.body, color = ShizziTheme.colors.onSurfaceMuted)
                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = { pairingCode = it.filter(Char::isDigit).take(6) },
                    label = { Text(str(R.string.pairing_code)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.sm)) {
                    OutlinedButton(onClick = actions.onOpenWirelessDebugging, modifier = Modifier.weight(1f)) {
                        Text(str(R.string.open_wireless_debugging))
                    }
                    Button(
                        onClick = { actions.onPairLocalAdb(pairingCode) },
                        enabled = pairingCode.length == 6 && state !is PrivilegeState.Connecting,
                        modifier = Modifier.weight(1f),
                    ) { Text(str(R.string.pair_device)) }
                }
            }
        }
    }
}

@Composable
private fun ProviderButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(label) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
}

private fun providerDescription(backend: PrivilegeBackendType): String = when (backend) {
    PrivilegeBackendType.SHIZUKU -> str(R.string.shizuku_backend_description)
    PrivilegeBackendType.LOCAL_ADB -> str(R.string.local_adb_backend_description)
}

private fun privilegeStatus(state: PrivilegeState): String = when (state) {
    is PrivilegeState.Ready -> str(R.string.provider_ready)
    is PrivilegeState.SetupRequired -> str(R.string.provider_setup_required)
    is PrivilegeState.Connecting -> str(R.string.local_adb_connecting)
    is PrivilegeState.Unsupported -> str(R.string.local_adb_unsupported)
    is PrivilegeState.Error -> str(R.string.local_adb_error, state.message)
}
