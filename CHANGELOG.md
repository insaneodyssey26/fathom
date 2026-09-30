# Release Notes & Changelog

All notable changes to the Fathom project are documented in this file.

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
