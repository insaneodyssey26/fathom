package com.masum.fathom.ui.shell

enum class LegalDoc(val title: String, val overline: String, val updated: String) {
    PRIVACY("Privacy Policy", "Your data", "5 October 2026"),
    TERMS("Terms of Use", "The ground rules", "5 October 2026")
}

data class LegalSection(val heading: String, val paragraphs: List<String>)

object LegalDocuments {

    const val CONTACT_EMAIL = "skmasumali.dev@gmail.com"

    fun sections(doc: LegalDoc): List<LegalSection> = when (doc) {
        LegalDoc.PRIVACY -> privacy
        LegalDoc.TERMS -> terms
    }

    private val privacy = listOf(
        LegalSection(
            "The short version",
            listOf(
                "Fathom is built to work entirely on your phone. It has no accounts, no analytics, no advertising and no tracking. We do not collect, store on a server, sell or share your personal information, because we never receive it."
            )
        ),
        LegalSection(
            "What stays on your device",
            listOf(
                "Your name and chosen spiritual path, reading theme, reminder settings, saved passages and tags, reflections you write, and your reading history and streaks are stored in local databases and preferences on your phone.",
                "Deleting the app deletes this data. If Android's device backup is switched on in your phone settings, Android may include app data in your Google backup. That is controlled by your phone and Google, not by Fathom."
            )
        ),
        LegalSection(
            "Notifications",
            listOf(
                "The daily verse is scheduled and shown by your phone. Nothing is sent to a server to deliver it. You can turn it off in Settings or in your phone's notification settings at any time."
            )
        ),
        LegalSection(
            "Update checks",
            listOf(
                "Fathom asks Google Play whether a newer version is available, in the app and in the background about twice a day if update alerts are on. This request is made to Google Play and is covered by Google's privacy policy. You can turn update alerts off in Settings."
            )
        ),
        LegalSection(
            "Sharing and exporting",
            listOf(
                "When you share a verse card or export a backup, you choose where it goes through your phone's share sheet. Fathom does not see or control the destination. A backup file contains your saved passages, tags and reflections, so keep it somewhere you trust."
            )
        ),
        LegalSection(
            "Links to the web",
            listOf(
                "Some screens offer a shortcut to search the web for a verse's commentary. Tapping it opens your browser, and the search engine's own privacy policy then applies."
            )
        ),
        LegalSection(
            "Optional narration key",
            listOf(
                "Settings includes a field where you can store a voice provider key. It is kept only on your device and Fathom does not currently send it anywhere. Recitation uses your phone's built-in offline voice."
            )
        ),
        LegalSection(
            "Purchases",
            listOf(
                "During closed testing nothing is charged. If paid features are introduced, payment will be handled by Google Play or the named payment provider, and Fathom will not receive your card details."
            )
        ),
        LegalSection(
            "Children",
            listOf(
                "Because Fathom collects no personal information, it collects none from children either."
            )
        ),
        LegalSection(
            "Changes and contact",
            listOf(
                "If this policy changes, the new version will appear here with a new date. Questions or requests can be sent to $CONTACT_EMAIL."
            )
        )
    )

    private val terms = listOf(
        LegalSection(
            "Using Fathom",
            listOf(
                "By using Fathom you agree to these terms. Fathom is a reading companion for sacred and philosophical texts. It is offered for personal, non-commercial reflection and study."
            )
        ),
        LegalSection(
            "The texts",
            listOf(
                "Scripture and philosophical texts in Fathom come from publicly available translations and editions, and from the commentators credited beside each entry. Their copyright and licences remain with their respective owners.",
                "Translations are not authoritative. Verse numbering, wording and spelling can differ between editions and may contain errors. For devotional, scholarly or legal purposes, please consult a trusted edition or teacher."
            )
        ),
        LegalSection(
            "Commentary and context",
            listOf(
                "Context, summaries and reflections shown in the app are written to aid contemplation. They are not religious rulings, professional advice, or a substitute for teachers in any tradition. Fathom shows texts from seven traditions with equal respect and takes no position on which is true."
            )
        ),
        LegalSection(
            "Your content",
            listOf(
                "Reflections, tags and saved passages belong to you and stay on your device. You are responsible for backing them up with the export option in Settings. Uninstalling the app removes them."
            )
        ),
        LegalSection(
            "Acceptable use",
            listOf(
                "Please do not copy, decompile, resell or republish the app or its design, or use it to harass or demean anyone's beliefs. Verse cards you create are yours to share, with their source credited."
            )
        ),
        LegalSection(
            "Closed testing and availability",
            listOf(
                "Fathom is currently in closed testing. Features may change, break or be removed, and subscription screens show preview pricing that does not charge you. The app is provided as is, without warranties of any kind, and to the extent the law allows the developer is not liable for losses arising from its use."
            )
        ),
        LegalSection(
            "Ownership",
            listOf(
                "The app's code, interface design and original writing are owned by Masum Ali. Third-party fonts and libraries remain under their own licences."
            )
        ),
        LegalSection(
            "Changes and contact",
            listOf(
                "These terms may be updated as the app grows, and continued use means you accept the current version. Questions can be sent to $CONTACT_EMAIL."
            )
        )
    )
}
