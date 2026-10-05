package com.masum.fathom.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import com.masum.fathom.core.model.TraditionId

val LocalNavTradition = compositionLocalOf<MutableState<TraditionId?>?> { null }

@Composable
fun NavToneEffect(traditionId: TraditionId?) {
    val sink = LocalNavTradition.current
    LaunchedEffect(traditionId) { sink?.value = traditionId }
}
