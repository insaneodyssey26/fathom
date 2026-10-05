package com.masum.fathom.ui.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WidthClass { Compact, Medium, Expanded }

@Immutable
data class ShellMetrics(
    val widthClass: WidthClass = WidthClass.Compact,
    val usesRail: Boolean = false,
    val bottomInset: Dp = 0.dp,
    val startInset: Dp = 0.dp,
    val contentMaxWidth: Dp = 640.dp,
    val isShort: Boolean = false
)

val LocalShell = compositionLocalOf { ShellMetrics() }

fun widthClassOf(width: Dp): WidthClass = when {
    width < 600.dp -> WidthClass.Compact
    width < 840.dp -> WidthClass.Medium
    else -> WidthClass.Expanded
}
