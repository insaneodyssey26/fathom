package com.masum.fathom.core.sanctuary

import com.masum.fathom.R

enum class AppIcon(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val isPatronOnly: Boolean,
    val previewResId: Int,
    val activityAliasName: String
) {
    DEFAULT(
        id = "default",
        displayName = "Vellum",
        subtitle = "Parchment & ink",
        isPatronOnly = false,
        previewResId = R.drawable.ic_preview_default,
        activityAliasName = "com.masum.fathom.MainActivityDefault"
    ),
    OBSIDIAN(
        id = "obsidian",
        displayName = "Obsidian",
        subtitle = "Pitch black & gold",
        isPatronOnly = true,
        previewResId = R.drawable.ic_preview_obsidian,
        activityAliasName = "com.masum.fathom.MainActivityObsidian"
    ),
    ABYSS(
        id = "abyss",
        displayName = "Abyss",
        subtitle = "Midnight & celestial",
        isPatronOnly = true,
        previewResId = R.drawable.ic_preview_abyss,
        activityAliasName = "com.masum.fathom.MainActivityAbyss"
    ),
    SAHARA(
        id = "sahara",
        displayName = "Sahara",
        subtitle = "Desert & terracotta",
        isPatronOnly = true,
        previewResId = R.drawable.ic_preview_sahara,
        activityAliasName = "com.masum.fathom.MainActivitySahara"
    ),
    FOREST(
        id = "forest",
        displayName = "Moss",
        subtitle = "Deep wood & jade",
        isPatronOnly = true,
        previewResId = R.drawable.ic_preview_forest,
        activityAliasName = "com.masum.fathom.MainActivityForest"
    ),
    CINNABAR(
        id = "cinnabar",
        displayName = "Cinnabar",
        subtitle = "Imperial lacquer & ruby",
        isPatronOnly = true,
        previewResId = R.drawable.ic_preview_cinnabar,
        activityAliasName = "com.masum.fathom.MainActivityCinnabar"
    );

    companion object {
        fun fromId(id: String): AppIcon = entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}
