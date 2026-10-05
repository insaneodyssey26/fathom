# Release Notes & Changelog

All notable changes to the Fathom project are documented in this file.

## [1.4.0] - 2026-10-05

### Added
- **Daily verse notification.** One verse a day at a time you choose, picked from the bundled texts. Short verses are preferred so they read well on a lock screen. You can draw from every tradition or only your own. The notification has Save and Share buttons, and tapping it opens that verse.
- **Update alerts.** Fathom checks Google Play about twice a day and posts one quiet notification for each new version. It can be switched off in Settings.
- **Notification settings.** Time presets plus hour and minute steppers, verse source, a lock-screen privacy option, a preview button, and a clear message when Android is blocking notifications.
- **Verse jog knob.** A small control at the bottom right of the Read screen. Push it down for the next verse and up for the previous one. The further you push, the faster it moves.
- **More menu.** Opened from the Read screen header. It links to soundscapes, Settings, what's new, the Privacy Policy and the Terms of Use.
- **Privacy Policy and Terms of Use** inside the app, with the Journal and Settings linking to them. Settings also has version info, send feedback and rate.
- **Predictive back.** Back gestures follow your finger and show the screen underneath.
- **Side rail** for tablets, foldables and landscape phones.

### Changed
- **Full redesign.** New typography (Fraunces for headings, Newsreader for scripture, Geist for the interface, Geist Mono for citations), a colour for each tradition that tints the screen you are on, and a background that shifts as you move between traditions.
- **Themes** are now Nocturne, Vellum, Abyss and Moss. The saved choice carries over.
- **Navigation.** A docked bottom bar with a tick-mark ruler replaces the floating bar. The whole app now sits on Navigation 3, so rotation and process death keep your place.
- **Read.** Full-bleed verse pages, a verse number ruler, long original text collapsed behind "Show full original", and a smaller action bar.
- **Paths.** A timeline with a progress dial, a rebuilt chapter reader with a jump-to-verse grid, and a new completion screen. The header stays put while the list scrolls under it.
- **Explore.** Search highlights matches, tradition chips, a mosaic of the eight topics, a scripture list, and chapter tabs in the book reader.
- **Journal** (previously You). Greeting, streak with a week chart, reading tallies, progress per tradition, milestones, saved passages, reflections and history.
- **Share cards** render the exact image shown in the preview, in four palettes and three sizes.
- **Sheets and Settings.** Bottom sheets fit short screens and landscape and slide away when dismissed. Settings is a page rather than a pop-up.
- **Widget** follows your theme and the colour of the verse's tradition.
- **Mini player, soundscapes and update banner** redrawn.

### Fixed
- Reading streaks, the weekly chart and history grouping never counted, because every read was saved with a placeholder date in 1970. New reads are saved correctly. Reads from older versions keep their old dates.
- A verse opened from the widget could be replaced by a random one when the app started cold.
- A white flash between screens.
- A crash when switching a toggle in Settings.
- The daily reminder did not reliably appear: the notification permission was never requested and the alarm was lost after a restart.

---

## [1.3.0] - 2026-09-30

### Added
- **High-Fidelity Ambient Soundscapes Suite**: 6 curated contemplative background soundscapes (Tibetan Singing Bowl, Himalayan Monks Chanting, Gentle Rain on Monastery, Temple Bells & Stream, Binaural 432Hz Solfeggio Tone, Deep Om Drone) with seamless looping and independent background audio mixing.
- **Contemplative Sleep Timer**: Dedicated audio sleep timer with quick-action presets (15m, 30m, 45m, 60m, custom), gentle audio fade-out, and a pulsing status pill in the top app bar.
- **Cross-Tradition Semantic Topical Explorer**: Curated life themes and existential topics (Peace & Anxiety, Courage & Strength, Forgiveness & Grace, Grief & Solace, Wisdom & Purpose, Gratitude & Joy, Surrender & Trust, Stillness & Presence).
- **Search Bento Grid Directory**: Interactive visual directory in the Search screen with quick filter pills, high-density multi-term keyword search across all 7 canonical traditions, and dedicated passage views.
- **Responsive Sacred Road Milestone Nodes**: Redesigned checkpoint nodes with minimal glowing tokens (Temple, Gateway, Sanctuary, Disciple) and adaptive non-overlapping lateral labels tailored for both compact and large displays.

---

## [1.2.0] - 2026-09-27

### Added
- **Sacred Daily Wisdom Home Screen Widget**: An Android home screen widget with automatic midnight passage refreshes, tactile reload controls, and direct deep-linking into verse reader.
- **Interactive Sacred Pathways**: Node-based chapter reading trees with visual progress milestones, dedicated chapter study reader, and celebratory completion modals.
- **Contemplative Journey Analytics**: Daily reading streaks, interactive activity heatmap calendar, and reading milestone badges in the Sanctuary profile hub.
- **Tactile Haptic Engine**: Universal tactile feedback integrated across scripture card paging, navigation tabs, bookmarking, and audio controls.
- **Personalized Sanctuary Onboarding**: New onboarding flow allowing readers to choose spiritual tradition focus, establish reading intentions, and automatically tailor the daily feed.
- **What's New Experience**: Interactive modal displaying release highlights on app updates and accessible anytime on-demand from the Sanctuary profile.
- **Android Baseline Profiles**: Pre-compiled AOT profiles for instantaneous app cold starts and smooth 120fps scrolling transitions.

### Changed
- Refined Top Feed Header: Streamlined minimal capsule pill with dynamic tradition focus and direct book selector.
- Christianity Tradition Labeling: Standardized scripture references under Christianity to "The Holy Bible".
- Profile Hub Layout: Optimized bento-grid view in the 'You' screen with independent sub-views for bookmarks, reflections, and traditions.

---

## [1.1.0] - 2026-09-25

### Added
- **4 Sacred Reading Themes**:
  - *Obsidian Gold*: Pure OLED black paired with warm temple gold.
  - *Parchment Cream*: Soft linen background with deep umber typography.
  - *Midnight Slate*: Deep oceanic navy with celestial blue accents.
  - *Forest Zen*: Earthy moss and sage for quiet contemplation.
- **Upgraded Sacred Audio Player**: Interactive seekbar with smooth timeline scrubbing, granular speed adjustment (0.5x to 2.5x in 0.1x steps), and persistent background playback.
- **Daily Contemplation Reminders**: Local notification scheduling for morning and evening reflection habits.
- **Google Play In-App Updates**: Flexible in-app update checks for testers in closed testing tracks.
- **Sanctuary Data Vault**: Database metrics dashboard across 15,163 verses and one-tap JSON backup and restore for annotations.
- **Studio Narration (BYOK)**: On-device API key integration for custom neural text-to-speech providers (ElevenLabs / OpenAI).

---

## [0.1.0-alpha] - 2026-09-23

### Added
- **Complete Offline Canonical Corpus**: 15,163 verses spanning 7 spiritual and philosophical traditions:
  - *Sanatana Dharma / Hinduism*: Bhagavad Gita and Principal Upanishads (Isha, Katha, Kena).
  - *Islam*: The Holy Quran (Arabic Uthmani script with Sahih International English translation).
  - *Christianity*: The Holy Bible (Gospels of Matthew and John, Proverbs, Ecclesiastes).
  - *Judaism*: The Book of Genesis and Psalms.
  - *Buddhism*: Canonical Dhammapada.
  - *Taoism*: Tao Te Ching.
  - *Stoicism*: Marcus Aurelius' *Meditations* and Epictetus' *Enchiridion*.
- **Sacred Card Feed**: Vertical paginated reader with original language typography, transliteration, English translations, and exegesis commentaries.
- **Scripture Selector**: Hierarchical accordion navigation to isolate individual books and traditions.
- **Offline Search Engine**: Multi-mode search with tokenized indexing and tradition filters.
- **Local-First Architecture**: 100% private, on-device SQLite database via SQLDelight with zero network telemetry or tracking.
