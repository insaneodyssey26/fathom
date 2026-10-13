import SwiftUI
import SharedLogic

public struct SanctuaryHubSheet: View {
    public let colors: ThemeColors
    public let streakDays: Int
    public let notesCount: Int
    public let bookmarksCount: Int
    public let onOpenSettings: () -> Void
    @Environment(\.dismiss) private var dismiss

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("THE SANCTUARY")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.4)

                            Text("Your Contemplative Space")
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
                    .padding(.top, 24)

                    HStack(spacing: 12) {
                        statCard(
                            title: "Streak",
                            value: "\(streakDays)",
                            unit: "days",
                            icon: "flame.fill",
                            accent: Color(red: 0.98, green: 0.52, blue: 0.18)
                        )

                        statCard(
                            title: "Reflections",
                            value: "\(notesCount)",
                            unit: "saved",
                            icon: "pencil.and.outline",
                            accent: Color(red: 0.22, green: 0.78, blue: 0.42)
                        )

                        statCard(
                            title: "Bookmarks",
                            value: "\(bookmarksCount)",
                            unit: "held",
                            icon: "bookmark.fill",
                            accent: Color(red: 0.88, green: 0.75, blue: 0.45)
                        )
                    }

                    VStack(alignment: .leading, spacing: 14) {
                        Text("SACRED INTENTION")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.2)

                        VStack(alignment: .leading, spacing: 10) {
                            Text("“Silence is not the absence of sound, but the stillness of the mind.”")
                                .font(.system(size: 16, weight: .medium, design: .serif))
                                .foregroundColor(colors.textPrimary)
                                .italic()
                                .lineSpacing(4)

                            Text("A private temple on your device. Zero analytics, zero cloud syncing, 100% sacred privacy.")
                                .font(.system(size: 13))
                                .foregroundColor(colors.textSecondary)
                                .lineSpacing(3)
                        }
                        .padding(18)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(colors.surface)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(colors.hairline, lineWidth: 1)
                        )
                    }

                    VStack(alignment: .leading, spacing: 14) {
                        Text("SACRED TOOLS")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.2)

                        Button(action: {
                            dismiss()
                            onOpenSettings()
                        }) {
                            HStack(spacing: 14) {
                                Image(systemName: "slider.horizontal.3")
                                    .font(.system(size: 16))
                                    .foregroundColor(TraditionTones.hue(for: nil))

                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Reading Preferences & Themes")
                                        .font(.system(size: 15, weight: .medium, design: .serif))
                                        .foregroundColor(colors.textPrimary)

                                    Text("Switch between Nocturne, Vellum, Abyss, and Moss")
                                        .font(.system(size: 12))
                                        .foregroundColor(colors.textTertiary)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(colors.textTertiary)
                            }
                            .padding(16)
                            .background(
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(colors.surface)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(colors.hairline, lineWidth: 1)
                            )
                        }
                        .buttonStyle(PlainButtonStyle())
                    }

                    Spacer(minLength: 20)
                }
                .padding(.horizontal, 24)
            }
        }
        .presentationDetents([.fraction(0.75), .large])
        .presentationDragIndicator(.visible)
    }

    private func statCard(title: String, value: String, unit: String, icon: String, accent: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundColor(accent)

            Text(value)
                .font(.system(size: 24, weight: .bold, design: .serif))
                .foregroundColor(colors.textPrimary)

            Text("\(title) · \(unit)")
                .font(.system(size: 11, design: .monospaced))
                .foregroundColor(colors.textTertiary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(colors.surface)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(colors.hairline, lineWidth: 1)
        )
    }
}
