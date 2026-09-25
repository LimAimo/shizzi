package dev.shizzi.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.shizzi.CompatibilityState
import dev.shizzi.DownloadProgress
import dev.shizzi.ui.theme.ShizziTheme
import dev.shizzi.ui.theme.themedSurface

@Composable
fun TetheringProviderDownloadCard(state: CompatibilityState, hasNetwork: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .themedSurface(fill = ShizziTheme.colors.surface)
            .padding(ShizziTheme.spacing.lg),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.md),
    ) {
        ModuleStateIcon(state)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ShizziTheme.spacing.xs),
        ) {
            Text(
                text = "网络共享模块",
                style = ShizziTheme.typography.subheading,
                color = ShizziTheme.colors.onSurface,
            )

            Text(
                text = bodyFor(state, hasNetwork),
                style = ShizziTheme.typography.body,
                color = ShizziTheme.colors.onSurfaceMuted,
            )

            when (state) {
                is CompatibilityState.Downloading -> DownloadBar(state.progress)
                is CompatibilityState.DownloadFailed -> FailureDetail(state.failure.reason)
                else -> Unit
            }
        }
    }
}

@Composable
private fun DownloadBar(progress: DownloadProgress) {
    val fraction = when {
        progress.totalBytes > 0 -> progress.bytesRead.toFloat() / progress.totalBytes
        else -> 0f
    }

    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        color = ShizziTheme.colors.primary,
        trackColor = ShizziTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ShizziTheme.spacing.md),
    )
}

@Composable
private fun FailureDetail(reason: String) {
    Text(
        text = breakableIdentifiers(reason),
        style = ShizziTheme.typography.log,
        color = ShizziTheme.colors.onSurfaceMuted,
        modifier = Modifier.padding(top = ShizziTheme.spacing.xs),
    )
}

private fun bodyFor(state: CompatibilityState, hasNetwork: Boolean): String = when {
    state is CompatibilityState.Downloading -> "正在下载模块…"

    state is CompatibilityState.DownloadFailed && state.failure.isConnectivity ->
        "无法连接网络下载模块。请恢复网络连接后重试。"

    state is CompatibilityState.DownloadFailed ->
        "下载内容未能通过校验，因此没有安装任何内容。"

    !hasNetwork ->
        "你的手机需要新版网络共享模块才能转发热点流量。" +
            "请连接网络后下载。"

    else ->
        "安装新版网络共享模块后，这台手机即可运行本应用。" +
            "模块约 3 MB，安装完成后需要重启一次。"
}
