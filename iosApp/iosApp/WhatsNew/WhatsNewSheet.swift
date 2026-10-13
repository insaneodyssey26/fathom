import SwiftUI
import SharedLogic

public struct ReleaseNoteItem: Identifiable {
    public let id: String
    public let version: String
    public let title: String
    public let isCurrent: Bool
    public let highlights: [String]

    public init(version: String, title: String, isCurrent: Bool, highlights: [String]) {
        self.id = version
        self.version = version
        self.title = title
        self.isCurrent = isCurrent
        self.highlights = highlights
    }
}

public struct WhatsNewSheet: View {
    public let colors: ThemeColors
    public let accentColor: Color
    @Environment(\.dismiss) private var dismiss

    private let releases: [ReleaseNoteItem] = [
        ReleaseNoteItem(
            version: "v1.6.0",
            title: "Sanctuary Soundscapes, Liquid Design & Native iOS Engine",
            isCurrent: true,
            highlights: [
                "Sacred Ambient Soundscapes: 6 contemplative sound loops (Temple Rain, Distant Storm, Sanctuary Night, Sacred River, Singing Bowl, Brown Noise) with loop control and volume mixing.",
                "First-Launch Sacred Onboarding: A 3-step contemplative ceremony introducing the 7 canonical traditions and personalizing your sacred reading feed.",
                "Liquid Glass & OLED Nocturne: Frosted translucent chrome with smooth backdrop blur and four distinct reading themes (Nocturne, Vellum, Abyss, and Moss).",
                "Sanctuary Dashboard Hub: Private offline streak counter, bookmark repository, and zero-tracking privacy assurance."
            ]
        ),
        ReleaseNoteItem(
            version: "v1.5.0",
            title: "Canonical Depth, Cross-Tradition Pathways & Full Search",
            isCurrent: false,
            highlights: [
                "Full-Text Scripture Search: Search across all 15,163 verses instantly with offline SQLite FTS indexing.",
                "Cross-Tradition Pathways: Guided paths grouping timeless chapters across Buddhism, Stoicism, Hinduism, Taoism, Christianity, and Islam.",
                "Reflections Journal: Save personal contemplations tied to specific verses with full privacy."
            ]
        )
    ]

    public init(colors: ThemeColors, accentColor: Color) {
        self.colors = colors
        self.accentColor = accentColor
    }

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("SACRED CHRONICLES")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.4)

                        Text("What’s New in Fathom")
                            .font(.system(size: 22, weight: .bold, design: .serif))
                            .foregroundColor(colors.textPrimary)
                    }

                    Spacer()

                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(colors.textTertiary)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)
                .padding(.bottom, 16)

                ScrollView {
                    VStack(spacing: 24) {
                        ForEach(releases) { release in
                            releaseCard(release: release)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
                }

                VStack {
                    Button(action: {
                        FathomHaptics.light()
                        dismiss()
                    }) {
                        Text("Continue reading")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(colors.isDark ? .black : .white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(accentColor)
                            )
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 20)
            }
        }
        .presentationDetents([.fraction(0.80), .large])
        .presentationDragIndicator(.visible)
    }

    private func releaseCard(release: ReleaseNoteItem) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 10) {
                Text(release.version)
                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                    .foregroundColor(release.isCurrent ? accentColor : colors.textTertiary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        RoundedRectangle(cornerRadius: 6)
                            .fill(release.isCurrent ? accentColor.opacity(0.12) : colors.surfaceElevated)
                    )

                if release.isCurrent {
                    Text("CURRENT RELEASE")
                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                        .foregroundColor(accentColor)
                        .tracking(1.0)
                }

                Spacer()
            }

            Text(release.title)
                .font(.system(size: 17, weight: .semibold, design: .serif))
                .foregroundColor(colors.textPrimary)

            VStack(alignment: .leading, spacing: 10) {
                ForEach(release.highlights, id: \.self) { highlight in
                    HStack(alignment: .top, spacing: 10) {
                        Circle()
                            .fill(release.isCurrent ? accentColor.opacity(0.8) : colors.textTertiary.opacity(0.5))
                            .frame(width: 5, height: 5)
                            .padding(.top, 6)

                        Text(highlight)
                            .font(.system(size: 14))
                            .foregroundColor(colors.textSecondary)
                            .lineSpacing(3)
                    }
                }
            }
        }
        .padding(18)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(colors.surface)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(release.isCurrent ? accentColor.opacity(0.3) : colors.hairline, lineWidth: 1)
        )
    }
}
