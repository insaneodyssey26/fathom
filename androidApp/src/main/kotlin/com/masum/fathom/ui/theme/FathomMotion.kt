package com.masum.fathom.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object FathomMotion {
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedIn = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedOut = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    const val Quick = 160
    const val Standard = 320
    const val Slow = 650
    const val Linger = 1400

    val Settle = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    fun <T> press() = spring<T>(dampingRatio = 0.62f, stiffness = 900f)
    fun <T> bounce() = spring<T>(dampingRatio = 0.55f, stiffness = 380f)
    fun <T> glide() = spring<T>(dampingRatio = 0.82f, stiffness = 260f)
    fun <T> enter(duration: Int = 320, delay: Int = 0) = tween<T>(duration, delay, Emphasized)
    fun <T> exit(duration: Int = 150) = tween<T>(duration, 0, EmphasizedOut)
    fun <T> tone() = tween<T>(450, 0, Settle)
}
