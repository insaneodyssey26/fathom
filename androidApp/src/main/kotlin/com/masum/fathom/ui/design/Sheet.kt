package com.masum.fathom.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomTheme

class SheetCloser(private val run: (() -> Unit) -> Unit) {
    operator fun invoke(after: () -> Unit = {}) = run(after)
}

val LocalSheetCloser = compositionLocalOf<SheetCloser?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FathomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = FathomTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    val density = LocalDensity.current
    val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentMax = (windowHeight - statusTop - navBottom - 56.dp).coerceAtLeast(240.dp)
    val scope = rememberCoroutineScope()
    val latestDismiss by rememberUpdatedState(onDismiss)
    var closing by remember { mutableStateOf(false) }
    val closer = remember(sheetState) {
        SheetCloser { after ->
            if (!closing) {
                closing = true
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    latestDismiss()
                    after()
                }
            }
        }
    }

    CompositionLocalProvider(LocalBackdrop provides null, LocalSheetCloser provides closer) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = colors.surface,
            scrimColor = colors.scrim,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.textTertiary.copy(alpha = 0.45f))
                )
            },
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMax)
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                content = content
            )
        }
    }
}

@Composable
fun SheetHeader(
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 6.dp, bottom = 18.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MonoLabel(text = overline, color = FathomTheme.tone.hue)
            Text(text = title, style = typography.headline, color = colors.textPrimary)
            if (subtitle != null) {
                Text(text = subtitle, style = typography.body, color = colors.textSecondary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            trailing()
            if (onClose != null) {
                val haptics = LocalFathomHaptics.current
                val closer = LocalSheetCloser.current
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .tappable(scale = 0.9f, haptic = false) {
                            haptics.light()
                            if (closer != null) closer() else onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Light.X, "Close", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SheetSpacer(height: androidx.compose.ui.unit.Dp = 16.dp) {
    Spacer(Modifier.height(height))
}

@Composable
fun rememberSheetCloser(fallback: () -> Unit): SheetCloser {
    val provided = LocalSheetCloser.current
    val latest by rememberUpdatedState(fallback)
    return provided ?: remember { SheetCloser { after -> latest(); after() } }
}
