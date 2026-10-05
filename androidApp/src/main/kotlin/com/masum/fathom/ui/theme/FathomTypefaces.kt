package com.masum.fathom.ui.theme

import android.content.Context
import android.graphics.Typeface
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object FathomTypefaces {
    private val cache = ConcurrentHashMap<String, Typeface>()

    fun load(context: Context, resId: Int, settings: String, fallback: Typeface = Typeface.DEFAULT): Typeface {
        val key = "$resId|$settings"
        cache[key]?.let { return it }
        val typeface = runCatching {
            val name = context.resources.getResourceEntryName(resId)
            val directory = File(context.cacheDir, "fonts").apply { mkdirs() }
            val file = File(directory, "$name.ttf")
            if (!file.exists() || file.length() == 0L) {
                context.resources.openRawResource(resId).use { input -> file.outputStream().use { input.copyTo(it) } }
            }
            Typeface.Builder(file).setFontVariationSettings(settings).build()
        }.getOrNull() ?: fallback
        cache[key] = typeface
        return typeface
    }
}
