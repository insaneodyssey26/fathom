package com.masum.fathom.core.model

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Drop
import com.adamglin.phosphoricons.regular.Feather
import com.adamglin.phosphoricons.regular.FlowerLotus
import com.adamglin.phosphoricons.regular.HandHeart
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class SacredTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val keywords: List<String>,
    val searchTerms: List<String>
) {
    PEACE_ANXIETY(
        id = "peace_anxiety",
        title = "Peace & Anxiety",
        subtitle = "Calm in the midst of turmoil and fear",
        keywords = listOf("peace", "anxious", "fear", "calm", "rest", "tranquil", "worry", "afraid", "quiet", "stillness"),
        searchTerms = listOf("peace", "anxious", "fear", "calm", "worry", "rest")
    ),
    COURAGE_STRENGTH(
        id = "courage_strength",
        title = "Courage & Strength",
        subtitle = "Fortitude, bravery, and steadfastness",
        keywords = listOf("courage", "strong", "strength", "fear not", "brave", "power", "conquer", "steadfast", "mighty"),
        searchTerms = listOf("courage", "strength", "strong", "fear not", "brave")
    ),
    FORGIVENESS_GRACE(
        id = "forgiveness_grace",
        title = "Forgiveness & Grace",
        subtitle = "Mercy, pardoning, and unbounded love",
        keywords = listOf("forgive", "mercy", "grace", "pardon", "compassion", "kindness", "love", "reconcile", "gentle"),
        searchTerms = listOf("forgive", "mercy", "grace", "compassion", "love")
    ),
    GRIEF_SOLACE(
        id = "grief_solace",
        title = "Grief & Solace",
        subtitle = "Healing broken hearts and deep loss",
        keywords = listOf("grief", "sorrow", "mourn", "comfort", "weep", "tears", "heartbroken", "pain", "solace", "heal"),
        searchTerms = listOf("comfort", "mourn", "grief", "sorrow", "tears", "heal")
    ),
    WISDOM_PURPOSE(
        id = "wisdom_purpose",
        title = "Wisdom & Purpose",
        subtitle = "Discernment, truth, and righteous action",
        keywords = listOf("wisdom", "understanding", "knowledge", "discern", "purpose", "truth", "guide", "light"),
        searchTerms = listOf("wisdom", "understanding", "purpose", "truth", "path")
    ),
    GRATITUDE_JOY(
        id = "gratitude_joy",
        title = "Gratitude & Joy",
        subtitle = "Thanksgiving, celebration, and gladness",
        keywords = listOf("rejoice", "glad", "gratitude", "thank", "praise", "joy", "blessed", "sing", "delight"),
        searchTerms = listOf("joy", "rejoice", "thank", "praise", "blessed", "gratitude")
    ),
    SURRENDER_TRUST(
        id = "surrender_trust",
        title = "Surrender & Trust",
        subtitle = "Letting go into the Divine Will",
        keywords = listOf("trust", "surrender", "faith", "refuge", "shelter", "rely", "deliver", "hope", "abide"),
        searchTerms = listOf("trust", "surrender", "faith", "refuge", "hope")
    ),
    STILLNESS_PRESENCE(
        id = "stillness_presence",
        title = "Stillness & Presence",
        subtitle = "Mindfulness, deep meditation, and silence",
        keywords = listOf("meditate", "mind", "still", "silent", "silence", "breath", "contemplate", "dwell"),
        searchTerms = listOf("stillness", "silent", "meditate", "mind", "contemplate")
    );

    val icon: ImageVector
        get() = when (this) {
            PEACE_ANXIETY -> PhosphorIcons.Regular.FlowerLotus
            COURAGE_STRENGTH -> PhosphorIcons.Regular.ShieldCheck
            FORGIVENESS_GRACE -> PhosphorIcons.Regular.HandHeart
            GRIEF_SOLACE -> PhosphorIcons.Regular.Drop
            WISDOM_PURPOSE -> PhosphorIcons.Regular.Compass
            GRATITUDE_JOY -> PhosphorIcons.Regular.Sun
            SURRENDER_TRUST -> PhosphorIcons.Regular.Heart
            STILLNESS_PRESENCE -> PhosphorIcons.Regular.Feather
        }

    companion object {
        fun fromIdOrNull(id: String): SacredTopic? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}
