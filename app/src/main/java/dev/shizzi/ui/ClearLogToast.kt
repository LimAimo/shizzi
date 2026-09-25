package dev.shizzi.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue

@Composable
fun ClearLogToast(
    isConfirming: Boolean,
    toasts: ToastState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {

    val confirm by rememberUpdatedState(onConfirm)
    val cancel by rememberUpdatedState(onCancel)

    LaunchedEffect(isConfirming) {
        if (!isConfirming) {
            toasts.dismiss(ToastKeys.CLEAR_LOG)
            return@LaunchedEffect
        }

        var isAnswered = false

        toasts.show(
            Toast(
                key = ToastKeys.CLEAR_LOG,
                message = "清空日志？",
                detail = "此操作无法撤销。",

                duration = ToastDuration.Indefinite,
                action = ToastAction("清空") {
                    isAnswered = true
                    confirm()
                },
                onDismiss = { if (!isAnswered) cancel() },
            ),
        )
    }
}

fun clearedToast(problem: String?): Toast = when (problem) {
    null -> Toast(
        key = ToastKeys.CLEAR_LOG,
        message = "日志已清空",
    )

    else -> Toast(
        key = ToastKeys.CLEAR_LOG,
        message = "仅清除了本应用的日志记录",
        detail = problem,
        duration = ToastDuration.Indefinite,
    )
}
