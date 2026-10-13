import SwiftUI
import SharedLogic

public struct SettingsView: View {
    @Binding public var currentTheme: ReadingThemeId
    public let colors: ThemeColors
    @State private var showWhatsNew: Bool = false
    @Environment(\.dismiss) private var dismiss

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Text("settings")
                        .font(.system(size: 26, weight: .bold, design: .serif))
                        .foregroundColor(colors.textPrimary)

                    Spacer()

                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(colors.textTertiary)
                    }
                }
                .padding(.horizontal, 22)
                .padding(.top, 20)
                .padding(.bottom, 24)

                ScrollView {
                    VStack(alignment: .leading, spacing: 28) {
                        VStack(alignment: .leading, spacing: 14) {
                            Text("READING THEME")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.2)

                            VStack(spacing: 10) {
                                themeRow(title: "Nocturne", subtitle: "OLED Pitch Dark · Gold Accents", theme: .nocturne, previewColor: Color(red: 0.04, green: 0.04, blue: 0.05))
                                themeRow(title: "Vellum", subtitle: "Warm Parchment · Sepia Tone", theme: .vellum, previewColor: Color(red: 0.96, green: 0.94, blue: 0.89))
                                themeRow(title: "Abyss", subtitle: "Deep Midnight Indigo · Slate Cyan", theme: .abyss, previewColor: Color(red: 0.04, green: 0.06, blue: 0.12))
                                themeRow(title: "Moss", subtitle: "Forest Emerald · Calming Green", theme: .moss, previewColor: Color(red: 0.04, green: 0.08, blue: 0.06))
                            }
                        }

                        VStack(alignment: .leading, spacing: 14) {
                            Text("ABOUT FATHOM")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.2)

                            VStack(alignment: .leading, spacing: 8) {
                                Text("15,163 Canonical Verses")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(colors.textPrimary)

                                Text("Spanning 7 ancient wisdom traditions: Hinduism, Buddhism, Taoism, Christianity, Islam, Judaism, and Stoicism.")
                                    .font(.system(size: 14))
                                    .foregroundColor(colors.textSecondary)
                                    .lineSpacing(4)
                            }
                            .padding(18)
                            .liquidGlass(cornerRadius: 18)
                        }

                        VStack(alignment: .leading, spacing: 14) {
                            Text("OFFLINE-FIRST ARCHITECTURE")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.2)

                            VStack(alignment: .leading, spacing: 8) {
                                Text("Zero Cloud Telemetry")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(colors.textPrimary)

                                Text("All database queries, streak computations, and search indexing happen 100% locally on your device with native SQLite performance.")
                                    .font(.system(size: 13))
                                    .foregroundColor(colors.textSecondary)
                                    .lineSpacing(4)
                            }
                            .padding(18)
                            .liquidGlass(cornerRadius: 18)
                        }

                        VStack(alignment: .leading, spacing: 14) {
                            Text("CHRONICLES")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.2)

                            Button(action: {
                                FathomHaptics.light()
                                showWhatsNew = true
                            }) {
                                HStack(spacing: 14) {
                                    Image(systemName: "sparkles")
                                        .font(.system(size: 16))
                                        .foregroundColor(TraditionTones.hue(for: nil))

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("What’s New in Fathom")
                                            .font(.system(size: 15, weight: .medium, design: .serif))
                                            .foregroundColor(colors.textPrimary)

                                        Text("v1.6.0 · Release notes & additions")
                                            .font(.system(size: 12))
                                            .foregroundColor(colors.textTertiary)
                                    }

                                    Spacer()

                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 12, weight: .semibold))
                                        .foregroundColor(colors.textTertiary)
                                }
                                .padding(16)
                                .liquidGlass(cornerRadius: 18)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(.horizontal, 22)
                    .padding(.bottom, 60)
                }
            }
        }
        .sheet(isPresented: $showWhatsNew) {
            WhatsNewSheet(colors: colors, accentColor: TraditionTones.hue(for: nil))
        }
    }

    private func themeRow(title: String, subtitle: String, theme: ReadingThemeId, previewColor: Color) -> some View {
        let isSelected = currentTheme == theme

        return Button(action: {
            FathomHaptics.medium()
            currentTheme = theme
        }) {
            HStack(spacing: 16) {
                RoundedRectangle(cornerRadius: 10)
                    .fill(previewColor)
                    .frame(width: 38, height: 38)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .strokeBorder(isSelected ? TraditionTones.hue(for: nil) : colors.hairline, lineWidth: isSelected ? 2 : 1)
                    )

                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .font(.system(size: 16, weight: isSelected ? .semibold : .medium))
                        .foregroundColor(colors.textPrimary)
                    Text(subtitle)
                        .font(.system(size: 12))
                        .foregroundColor(colors.textSecondary)
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(TraditionTones.hue(for: nil))
                        .font(.system(size: 20))
                }
            }
            .padding(16)
            .liquidGlass(cornerRadius: 18)
        }
    }
}
