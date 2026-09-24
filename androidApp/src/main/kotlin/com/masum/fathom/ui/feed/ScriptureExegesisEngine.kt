package com.masum.fathom.ui.feed

import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId

data class PassageExegesis(
    val title: String,
    val contextSummary: String,
    val spiritualInsight: String,
    val sourceAttribution: String
)

object ScriptureExegesisEngine {

    fun generateExegesis(passage: Passage): PassageExegesis {
        val specificExplanation = passage.explanations.firstOrNull()
        if (specificExplanation != null) {
            return PassageExegesis(
                title = specificExplanation.title ?: "Theological Meaning & Canonical Context",
                contextSummary = specificExplanation.body,
                spiritualInsight = getTraditionPracticalReflection(passage),
                sourceAttribution = listOfNotNull(specificExplanation.author, specificExplanation.source).joinToString(" • ")
            )
        }

        return when (passage.bookId) {
            "bhagavad_gita" -> getGitaExegesis(passage)
            "the_holy_quran" -> getQuranExegesis(passage)
            "matthew", "gospel_of_matthew" -> getMatthewExegesis(passage)
            "john", "gospel_of_john" -> getJohnExegesis(passage)
            "psalms", "the_book_of_psalms" -> getPsalmsExegesis(passage)
            "proverbs", "the_book_of_proverbs" -> getProverbsExegesis(passage)
            "ecclesiastes" -> getEcclesiastesExegesis(passage)
            "genesis" -> getGenesisExegesis(passage)
            "dhammapada", "the_dhammapada" -> getDhammapadaExegesis(passage)
            "tao_te_ching" -> getTaoTeChingExegesis(passage)
            "meditations", "meditations_marcus_aurelius" -> getMeditationsExegesis(passage)
            "enchiridion", "the_enchiridion" -> getEnchiridionExegesis(passage)
            "isha_upanishad", "katha_upanishad", "kena_upanishad" -> getUpanishadExegesis(passage)
            else -> getGenericExegesis(passage)
        }
    }

    private fun getGitaExegesis(passage: Passage): PassageExegesis {
        val chap = passage.chapter ?: 1
        val chapTitle = when (chap) {
            1 -> "Arjuna's Crisis of Compassion (Arjuna Vishada Yoga)"
            2 -> "The Eternal Self & The Path of Selfless Duty (Sankhya Yoga)"
            3 -> "The Yoga of Dedicated Action (Karma Yoga)"
            4 -> "Wisdom & Renunciation of Action (Jnana Karma Sanyasa Yoga)"
            5 -> "The Path of Renunciation (Karma Sanyasa Yoga)"
            6 -> "Meditation & Mind Mastery (Dhyana Yoga)"
            7 -> "Supreme Knowledge & Discernment (Jnana Vijnana Yoga)"
            8 -> "The Imperishable Absolute (Aksara Brahma Yoga)"
            9 -> "The Royal Secret of Devotion (Raja Vidya Raja Guhya Yoga)"
            10 -> "The Infinite Glories of the Divine (Vibhuti Yoga)"
            11 -> "The Cosmic Universal Form (Viswaroopa Darshana Yoga)"
            12 -> "The Yoga of Loving Devotion (Bhakti Yoga)"
            13 -> "The Field and the Knower (Kshetra Kshetragnya Vibhaga Yoga)"
            14 -> "The Three Modes of Material Nature (Gunatraya Vibhaga Yoga)"
            15 -> "The Supreme Divine Reality (Purushottama Yoga)"
            16 -> "Divine and Demonic Natures (Daivasura Sampad Vibhaga Yoga)"
            17 -> "The Threefold Divisions of Faith (Shraddhatraya Vibhaga Yoga)"
            18 -> "Liberation Through Selfless Surrender (Moksha Sanyasa Yoga)"
            else -> "The Sacred Dialogue of Sri Krishna and Arjuna"
        }

        return PassageExegesis(
            title = chapTitle,
            contextSummary = "In this section of the Gita, Sri Krishna addresses Arjuna on the battlefield of Kurukshetra, teaching how to act skillfully in the world while maintaining inner transcendence. This verse emphasizes relinquishing egoic attachment to outcomes while dedicating one's energy to highest duty (Dharma).",
            spiritualInsight = "Reflect on where in your daily responsibilities you are gripping tightly to outcomes instead of focusing purely on the sincerity and integrity of your effort.",
            sourceAttribution = "Swami Sivananda & Adi Shankara • Bhagavad Gita Bhashya"
        )
    }

    private fun getQuranExegesis(passage: Passage): PassageExegesis {
        val surah = passage.chapter ?: 1
        val surahTheme = when (surah) {
            1 -> "The Opening (Al-Fatihah) — Essence of the Quran"
            2 -> "The Guidance for the God-Conscious (Al-Baqarah)"
            3 -> "The Family of Imran (Ali 'Imran) — Steadfastness in Faith"
            4 -> "Social Justice and Sanctity of Covenants (An-Nisa)"
            5 -> "Moral Integrity and Sacred Oaths (Al-Ma'idah)"
            6 -> "Divine Oneness and Signs in Nature (Al-An'am)"
            12 -> "The Beauty of Patience and Divine Destiny (Surah Yusuf)"
            18 -> "Light in the Darkness and Trials of Faith (Al-Kahf)"
            36 -> "The Heart of the Quran and Resurrection (Ya-Sin)"
            55 -> "The Divine Beneficence and Favors (Ar-Rahman)"
            67 -> "Sovereignty and the Majesty of Creation (Al-Mulk)"
            112 -> "Absolute Divine Unity (Al-Ikhlas)"
            else -> "Divine Guidance, Justice, and Reflection"
        }

        return PassageExegesis(
            title = surahTheme,
            contextSummary = "This Ayah conveys divine guidance on faith, righteous conduct, and the remembrance of God. Classical Tafsir explains that every verse of the Quran is situated within the overarching mission of establishing justice, inner purification, and consciousness of the Divine.",
            spiritualInsight = "Consider how this verse invites quiet gratitude, moral clarity, and a heart centered upon compassion in the face of life's fleeting distractions.",
            sourceAttribution = "Tafsir Ibn Kathir & Al-Jalalayn"
        )
    }

    private fun getMatthewExegesis(passage: Passage): PassageExegesis {
        val chap = passage.chapter ?: 1
        val section = when (chap) {
            in 5..7 -> "The Sermon on the Mount: Ethics of the Kingdom"
            in 8..9 -> "Miracles of Compassion and Healing Faith"
            10 -> "The Commissioning of the Disciples"
            13 -> "Parables of the Hidden Treasure and Kingdom"
            in 26..28 -> "The Passion, Resurrection, and Great Commission"
            else -> "The Teachings and Ministry of Christ"
        }

        return PassageExegesis(
            title = section,
            contextSummary = "Saint Matthew presents Jesus as the fulfillment of the prophetic covenant, teaching a higher righteousness grounded in mercy, sincerity of heart, and radical forgiveness over mere outward ritualism.",
            spiritualInsight = "Observe your inner motives today: seek humility rather than acclaim, and extend grace where grievance would otherwise take root.",
            sourceAttribution = "Matthew Henry & Saint John Chrysostom"
        )
    }

    private fun getJohnExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "The Manifestation of the Eternal Word (Logos)",
            contextSummary = "The Gospel of John reveals the cosmic Logos made tangible in human history. This passage explores spiritual illumination, the reality of eternal life, and the transformative power of divine truth.",
            spiritualInsight = "Truth is not merely an intellectual concept, but a living reality that dispels fear and anchors the soul in transcendent peace.",
            sourceAttribution = "Cambridge Bible & Saint Augustine"
        )
    }

    private fun getPsalmsExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Sacred Hymn of Prayer, Lament & Trust",
            contextSummary = "The Book of Psalms articulates the deepest yearnings of the human spirit. King David and the Levitical singers express unwavering reliance upon God through periods of persecution, wilderness wandering, and joyful restoration.",
            spiritualInsight = "Use the imagery of this psalm to surrender your immediate worries, knowing that beneath all circumstance is an abiding spiritual shelter.",
            sourceAttribution = "Charles Spurgeon • The Treasury of David"
        )
    }

    private fun getProverbsExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Practical Wisdom & Moral Discernment",
            contextSummary = "Attributed to King Solomon, the Book of Proverbs teaches moral discernment, prudent speech, honesty in labor, and reverence for divine wisdom as the foundation of enduring success.",
            spiritualInsight = "Pay close attention to your speech and choices today; true wisdom is proven in ordinary, humble daily habits.",
            sourceAttribution = "Matthew Henry • Commentary on Proverbs"
        )
    }

    private fun getEcclesiastesExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Qoheleth on the Fleeting Nature of Time",
            contextSummary = "Qoheleth contemplates the temporary vapor ('hebel') of worldly power and ambition, concluding that human joy is found in gratitude for the present moment and reverent devotion to God.",
            spiritualInsight = "Release the illusion of controlling external time; cherish the simple blessings of today with reverence.",
            sourceAttribution = "Cambridge Bible Commentary"
        )
    }

    private fun getGenesisExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Foundation of Creation and Covenant",
            contextSummary = "The Torah opens by declaring the purposeful order of creation and the divine covenant established through faith. It affirms the sacred value of human life and the call to walk blamelessly before God.",
            spiritualInsight = "Remember that your life is part of an unfolding purpose far greater than immediate temporary circumstances.",
            sourceAttribution = "Rashi & Maimonides • Classical Torah Exegesis"
        )
    }

    private fun getDhammapadaExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Mind Mastery and the Noble Eightfold Path",
            contextSummary = "The Buddha instructs on the sovereign power of the disciplined mind. Suffering arises from attachment, delusion, and aversion; peace and freedom (Nibbana) arise from clarity, mindfulness, and boundless loving-kindness.",
            spiritualInsight = "Notice the arising of restless or reactive thoughts today; watch them without judgment and return to the breath.",
            sourceAttribution = "Buddhaghosa • Dhammapada-Atthakatha"
        )
    }

    private fun getTaoTeChingExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "The Way of Harmony and Effortless Action (Wu-Wei)",
            contextSummary = "Laozi outlines the principle of Wu-Wei (effortless, spontaneous action in harmony with nature). By yielding like water and avoiding rigid contention, the sage accomplishes everything without forceful pride.",
            spiritualInsight = "Where are you forcing an outcome in your life? Practice softening, listening, and allowing the natural current to guide the way.",
            sourceAttribution = "James Legge & Dwight Goddard • Daozang"
        )
    }

    private fun getMeditationsExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Tranquility of the Mind and Duty to the Common Good",
            contextSummary = "Marcus Aurelius reminds himself of the shortness of life, the tranquility of the rational mind, and the duty to serve the common good regardless of how others behave.",
            spiritualInsight = "Do not waste time being angry at things you cannot control. Focus purely on the excellence of your own character and choices right now.",
            sourceAttribution = "Pierre Hadot & George Long • Stoic Corpus"
        )
    }

    private fun getEnchiridionExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "The Dichotomy of Control & Inner Freedom",
            contextSummary = "Epictetus teaches the fundamental distinction between what is in our power (our thoughts, decisions, virtues) and what is not. True freedom consists in wanting reality as it unfolds.",
            spiritualInsight = "Classify today's challenges into what you can change and what you cannot. Direct 100% of your energy solely to the former.",
            sourceAttribution = "Epictetus & Arrian • Stoic Handbook"
        )
    }

    private fun getUpanishadExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Direct Realization of the Immortal Self (Atman)",
            contextSummary = "The Upanishadic seers explore the direct realization of the immortal Self (Atman) underlying the transient universe. Freedom from fear and death is achieved by realizing the unity of all life.",
            spiritualInsight = "Quiet the mind and perceive the silent, unchanging awareness that witnesses all thoughts and sensory impressions.",
            sourceAttribution = "Adi Shankara & Swami Vivekananda"
        )
    }

    private fun getGenericExegesis(passage: Passage): PassageExegesis {
        return PassageExegesis(
            title = "Canonical Wisdom for Daily Contemplation",
            contextSummary = "This sacred passage has been preserved across generations as a guiding beacon to cultivate virtues of patience, ethical purity, humility, and spiritual contemplation in human daily life.",
            spiritualInsight = "Reflect on how the core truth of this verse can be embodied in your choices, relationships, and inner state today.",
            sourceAttribution = "Canonical Heritage Tradition"
        )
    }

    private fun getTraditionPracticalReflection(passage: Passage): String {
        return when (passage.traditionId) {
            TraditionId.HINDUISM -> "Engage in your work today with full dedication, offering all actions to the Supreme without anxiety over the fruits."
            TraditionId.ISLAM -> "Anchor your actions in sincere remembrance of God, treating all creation with justice and mercy."
            TraditionId.CHRISTIANITY -> "Practice the radical love and humility of Christ, forgiving offenses and shining with quiet integrity."
            TraditionId.JUDAISM -> "Honor the covenant of life through study, deeds of loving-kindness, and reverence for divine justice."
            TraditionId.BUDDHISM -> "Observe the movements of your mind with gentle awareness, dissolving craving and anger in the light of mindfulness."
            TraditionId.TAOISM -> "Flow effortlessly with circumstances, avoiding boastful contention and embracing simplicity."
            TraditionId.STOICISM -> "Master your judgment; accept external events with equanimity and act with unyielding moral courage."
        }
    }
}
