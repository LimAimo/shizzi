package dev.shizzi.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

// Material 3 keeps labels in sentence case instead of forcing all-caps.
@Composable
@ReadOnlyComposable
fun themedLabel(text: String): String = text
